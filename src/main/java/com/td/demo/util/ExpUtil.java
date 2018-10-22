package com.td.demo.util;

import com.td.demo.exception.ProjectDemoException;

public class ExpUtil {


    public static void throwException(String msg){
        throw new ProjectDemoException(msg);
    }

    public static void check(boolean exp,String msg){
        if(!exp){
            throw new ProjectDemoException(msg);
        }
    }
}
