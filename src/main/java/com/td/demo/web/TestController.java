package com.td.demo.web;

import com.td.demo.distributeId.service.IDistrIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    IDistrIdService iDistrIdService;

    @RequestMapping("/ajax/distId.do")
    public String testId() throws InterruptedException {
        return String.valueOf(iDistrIdService.getId());
    }
}