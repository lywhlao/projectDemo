package com.td.demo;

import com.td.demo.distributeId.service.Impl.SnowFlakeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Autowired
	SnowFlakeService snowFlakeService;
	@Test
	public void contextLoads() throws SocketException {
//		long id = snowFlakeService.getId();
//		System.out.println(id);
//		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//		System.out.println(NetworkInterface.getNetworkInterfaces());

		System.out.println(4<<1);
	}

}
