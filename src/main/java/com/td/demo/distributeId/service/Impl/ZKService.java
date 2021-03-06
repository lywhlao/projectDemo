package com.td.demo.distributeId.service.Impl;

import com.google.common.base.Preconditions;
import com.td.demo.distributeId.service.IZKService;
import com.td.demo.exception.ProjectDemoException;
import com.td.demo.util.ExpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Data
@Slf4j
public class ZKService implements IZKService {


    /**
     * work 持久目录
     */
    public static final String P_WORK_DIR = "/distId/P-Works";

    /**
     * work 临时目录，获取顺序workId
     */
    public static final String ES_WORK_DIR = "/distId/ES-Works";

    /**
     *
     */
    public static final String CURRENT_WORK_DIR = "/distId/Current-Works";

    /**
     *
     */
    public static final Set<String> INIT_DIR_SET =new HashSet<>();


    /**
     * fake node for creating dirs
     */
    public static final String INIT = "/init";

    /**
     * zk client
     */
    private volatile CuratorFramework mClient;

    /**
     * get client time
     *
     */
    private AtomicLong touchTime=new AtomicLong();

    /**
     * zk address
     */
    @Value("${zk.server}")
    private String zkServerAddress;


    private java.util.Timer watchDog = new Timer();


    static {
        INIT_DIR_SET.add(P_WORK_DIR);
        INIT_DIR_SET.add(ES_WORK_DIR);
        INIT_DIR_SET.add(CURRENT_WORK_DIR);
    }

    @PostConstruct
    public void initConnect() throws Exception {
        Preconditions.checkArgument(!StringUtils.isEmpty(zkServerAddress), "zkClient can not be empty");
        initDir();
        initZkClientWatchDog();
    }

    @PreDestroy
    public void destroy() {
        watchDog.cancel();
        if (mClient != null) {
            mClient.close();
        }
    }


    /**
     * init worker dirs
     */
    private void initDir() {
        try {
            Iterator<String> iterator = INIT_DIR_SET.iterator();
            while (iterator.hasNext()){
                getZKClient().checkExists().creatingParentContainersIfNeeded().forPath(iterator.next() + INIT);
            }
        } catch (Exception e) {
            log.error("initDir error", e);
        }
    }


    /**
     *
     * watch dog .close client if 5 minutes not use.
     */
    private void initZkClientWatchDog(){
        watchDog.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mClient == null) return;
                synchronized(ZKService.class) {
                    if (System.currentTimeMillis() - touchTime.get() > 5 * 60 * 1000) {
                        log.warn("ZKConfigCenter haven't been used for 5 minutes and will be closed soon");
                        mClient.close();
                        mClient = null;
                    }
                }
            }
        }, 10 * 1000, 60 * 1000);
    }

    /**
     * get zkClient
     * @return
     * @throws Exception
     */
    private CuratorFramework getZKClient() throws Exception {
        if (mClient == null) {
            synchronized (ZKService.class) {
                if (mClient == null) {
                    mClient = CuratorFrameworkFactory.newClient(zkServerAddress,
                                                                new ExponentialBackoffRetry(1000, 3));
                    mClient.start();
                    mClient.blockUntilConnected();
                }
            }
        }
        touchTime.set(System.currentTimeMillis());
        return mClient;
    }


    /**
     * 获取指定node的 workId
     *
     * @param node node标识
     * @return workId
     */
    public String createOrGetWorkId(String node) {
        if(checkNodeExists(node)) {
            String nData = getNode(P_WORK_DIR, node);
            if (!StringUtils.isEmpty(nData)) {
                return nData;
            }
        }
        // 设置临时工作节点
        String currentWorkNode = createNode(CURRENT_WORK_DIR, node, "", CreateMode.EPHEMERAL);
        if(StringUtils.isEmpty(currentWorkNode)){
            throw new ProjectDemoException("already set by others tag"+node);
        }

        // 创建持久工作节点
        String success = createNode(P_WORK_DIR, node, "", CreateMode.PERSISTENT);
        if (StringUtils.isEmpty(success)) {
            return getNode(P_WORK_DIR, node);
        }

        // 创建临时节点，获取序号
        String esNode = createNode(ES_WORK_DIR, "", "", CreateMode.EPHEMERAL_SEQUENTIAL);

        if (StringUtils.isEmpty(esNode)) {
            ExpUtil.throwException("create es nod error");
        }
        String workId = esNode.substring(getPath(ES_WORK_DIR, "").length() + 1);

        boolean setDataStatus = setNodeData(P_WORK_DIR, node, workId);

        Assert.isTrue(setDataStatus, "set data error");

        return workId;
    }

    private boolean checkNodeExists(String node) {
        try {
            Stat stat = mClient.checkExists().forPath(getPath(P_WORK_DIR, node));
            if(stat!=null && stat.getCtime()>0){
                // already register,so check if same tag is in work
                Stat currentStat = mClient.checkExists().forPath(getPath(CURRENT_WORK_DIR, node));
                if(currentStat!=null && stat.getCtime()>0){
                    throw new ProjectDemoException("find same tag:"+node);
                }
                return true;
            }
        } catch (Exception e) {
           log.error("checkNode error",e);
        }
        return false;
    }


    /**
     * set node data
     *
     * @param path
     * @param nodeName
     * @param data
     */
    public boolean setNodeData(String path, String nodeName, String data) {
        boolean flag = true;
        try {
            mClient.setData().forPath(getPath(path, nodeName), data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            flag = false;
            log.error("createNode error", e);
        }
        return flag;
    }


    /**
     * create node
     * @param path
     * @param nodeName
     * @param data
     * @param type
     * @return
     */
    public String createNode(String path, String nodeName, String data, CreateMode type) {
        String result = "";
        try {
            result = mClient.create().withMode(type).forPath(getPath(path, nodeName), data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("createNode error", e);
        }
        return result;
    }


    /**
     * get node by path and nodeName
     *
     * @param path
     * @param nodeName
     * @return
     */
    public String getNode(String path, String nodeName) {
        String result = "";
        try {
            result = new String(mClient.getData().forPath(getPath(path, nodeName)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("get node exception node:{},e:{}", path + nodeName, e);
        }
        return result;
    }

    /**
     * @param path
     * @return
     */
    public boolean getNodes(String path) {
        //设置目录child变化监听
        PathChildrenCache pcc = new PathChildrenCache(mClient, P_WORK_DIR, false);
        try {
            pcc.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            pcc.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    System.out.println("get event ==>" + event.getData());
                }
            });
        } catch (Exception e) {
            log.error("get nodes error path:{},e:{}", path, e);
        }

        return false;
    }


    public static String getPath(String path, String node) {
        return path + "/" + node;
    }


}
