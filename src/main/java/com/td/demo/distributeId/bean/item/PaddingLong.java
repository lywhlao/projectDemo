package com.td.demo.distributeId.bean.item;

public class PaddingLong {
    private long p1, p2, p3, p4, p5, p6 = 7L;

    /**
     * 阻止jvm优化掉无用的字段
     */
    public long preventOptimisation() {
        return p1 + p2 + p3 + p4 + p5 + p6;
    }

}
