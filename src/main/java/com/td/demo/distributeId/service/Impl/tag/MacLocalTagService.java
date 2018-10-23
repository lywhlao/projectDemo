package com.td.demo.distributeId.service.Impl.tag;

import com.td.demo.distributeId.service.ILocalTagService;
import com.td.demo.exception.ProjectDemoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;

@Slf4j
@Service
public class MacLocalTagService implements ILocalTagService {

    @Override
    public String getLocalTag() {

        InetAddress ip;
        try {

            ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("getLocalTag error ",e);
        }

        throw new ProjectDemoException("get mac adresss error");
    }


}