package com.td.demo.distributeId.service.Impl.tag;

import com.td.demo.distributeId.service.ILocalTagService;
import com.td.demo.exception.ProjectDemoException;
import lombok.extern.slf4j.Slf4j;
import sun.net.util.IPAddressUtil;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Slf4j
public class MacLocalTagService implements ILocalTagService {

    @Override
    public String getLocalTag() {

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if(hardwareAddress!=null && hardwareAddress.length>0){
                    return new String(hardwareAddress);
                }
            }
        } catch (SocketException e) {
            log.error("get network error",e);
            throw new ProjectDemoException("network error");
        }
        throw new ProjectDemoException("can not get NetworkInterface");
    }
}