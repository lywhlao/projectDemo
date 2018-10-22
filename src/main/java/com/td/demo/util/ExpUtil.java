package com.td.demo.util;

import com.td.demo.exception.ProjectDemoException;

public class ExpUtil {


    public static void throwException(String msg){
        throw new ProjectDemoException(msg);
    }
}
