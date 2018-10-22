package com.td.demo.distributeId.service.Impl.localtag;

import com.td.demo.distributeId.service.ILocalTagService;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MockLocalTagService implements ILocalTagService {

    @Value("${snowFlake.localTag}")
    String localTag;


    @Override
    public String getLocalTag() {
        return localTag;
    }
}