package com.td.demo.distributeId.service.Impl.tag;

import com.td.demo.distributeId.service.ILocalTagService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//@Service
public class MockLocalTagService implements ILocalTagService {

    @Value("${snowFlake.localTag}")
    String localTag;


    @Override
    public String getLocalTag() {
        return localTag;
    }
}