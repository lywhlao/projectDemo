package com.td.demo.distributeId.service.Impl;

import com.google.common.base.Preconditions;
import com.td.demo.distributeId.service.IDistrIdService;
import com.td.demo.distributeId.service.ILocalTagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class SnowFlakeService implements IDistrIdService {


    private String localTag;

    @Autowired
    ILocalTagService localTagService;

    @Autowired
    ZKService zkService;

    IdGenService idGenService;

    private String workId;

    @PostConstruct
    public void init(){
        initLocalTag();
        initWorkerId();
        idGenService=new IdGenService(Long.valueOf(workId));

    }

    private void initLocalTag() {
        localTag=localTagService.getLocalTag();
    }

    /**
     * get workId
     */
    private void initWorkerId() {
        workId = zkService.createOrGetWorkId(localTag);
        log.info("tag -->{},workId-->:{}",localTag,workId);
        Preconditions.checkArgument(!StringUtils.isEmpty(workId));
        Preconditions.checkArgument(NumberUtils.isCreatable(workId));
    }

    public long getId() throws InterruptedException {
        long l = idGenService.nextId();
        log.info("tag-->{},workId-->{},distId:{}",localTag,workId,l);
        return l;
    }



}
