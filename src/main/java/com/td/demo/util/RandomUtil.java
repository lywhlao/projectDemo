package com.td.demo.util;

import it.unimi.dsi.util.XorShift1024StarPhiRandom;

public class RandomUtil {
    public static XorShift1024StarPhiRandom random = new XorShift1024StarPhiRandom();

    public static long getRandom() {
        return random.nextLong();
    }

}