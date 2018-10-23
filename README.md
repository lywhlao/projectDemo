### Project Demo

- 分布式Id生成组件


### 分布式id
#### 作用
- 提供集群唯一id生成服务，对于每个节点而言，id是单调递增的，如图1所示。
![](https://ws3.sinaimg.cn/large/006tNbRwly1fwhwnsjp7yj30xo0kaacr.jpg)
#### 原理
- 核心是算法是利用snowFlake算法生成分布式id，如图所示
![](https://ws2.sinaimg.cn/large/006tNbRwly1fwhwv1vqw2j311k08a765.jpg)
其中有10位是需要指定workerId，来表示该节点的标示。那么对于一个集群而言，每个节点的workerId都应该是不一样的（snowFlake算法没有提供这部分的功能），因此我**借助zookeeper来保证对于集群中的任意节点获取的workId是唯一的**，
原理就如图1所示，这里的tag需要保证在集群中是唯一的，可以用节点的网卡mac地址，或者其他特征。


#### 使用

- 配置
   > zk.server=localhost:2181 //zk服务地址

- 使用
``` java
@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Autowired
	SnowFlakeService snowFlakeService;

	@Test
	public void contextLoads() throws SocketException, InterruptedException {
		long id = snowFlakeService.getId();
		System.out.println(id);
	}
}

```