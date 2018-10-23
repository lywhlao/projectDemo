package com.td.demo.distributeId.service.Impl;

import com.td.demo.exception.ProjectDemoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;

public class IdGenService {
    private static final Logger logger = LoggerFactory.getLogger(IdGenService.class);

    private final long workerId;
    private final static long twepoch = 1451577600000L;
    private long sequence = 0L;
    private final static long workerIdBits = 10L;
    private final static long maxWorkerId = -1L ^ -1L << workerIdBits;
    private final static long sequenceBits = 12L;

    private final static long workerIdShift = sequenceBits;
    private final static long timestampLeftShift = sequenceBits + workerIdBits;
    private final static long sequenceMask = -1L ^ -1L << sequenceBits;

    private long lastTimestamp = -1L;

    private final static long wokerMask = (-1L ^ -1L << workerIdBits) << sequenceBits;
    private final static long timeBits = Long.SIZE - timestampLeftShift;
    private final static long timeMask = (-1L ^ -1L << timeBits) << timestampLeftShift;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));// 设置zone
    }

    public IdGenService(final long workerId) {
        super();
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
    }

    public static long maxTimeStamp() {
        long l = 1l;
        return twepoch + (l << (timeBits - 1)) - 1;
    }

    /**
     * 返回值为 { time, workerId, seq }
     *
     * @param id
     * @return
     */
    public static long[] parse(long id) {
        long seq = sequenceMask & id;
        long workerId = (wokerMask & id) >> sequenceBits;
        long time = ((timeMask & id) >> timestampLeftShift) + twepoch;
        return new long[] { time, workerId, seq };
    }

    /**
     * @return
     * @throws InterruptedException
     */
    public synchronized long nextId() throws InterruptedException {
        long timestamp = this.timeGen();
        if (this.lastTimestamp == timestamp) {
            this.sequence = this.sequence + 1 & this.sequenceMask;
            if (this.sequence == 0) {
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = 0;
        }
        if (timestamp < this.lastTimestamp) {

            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    //时间偏差大小小于5ms，则等待两倍时间
                    wait(offset << 1);//wait
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        //还是小于，抛异常并上报
                        throw new ProjectDemoException("time back");
                    }
                } catch (InterruptedException e) {
                    logger.error("nextId wait interrupted",e);
                    throw  e;
                }
            } else {
                //throw
                throw new ProjectDemoException("time back");
            }
        }

        this.lastTimestamp = timestamp;
        long id = (timestamp - twepoch) << timestampLeftShift | this.workerId << workerIdShift | this.sequence;
        return id;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();// System.nanoTime() / 1000000;
    }

    public long getWokerId() {
        return workerId;
    }
}
