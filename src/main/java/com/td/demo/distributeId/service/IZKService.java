package com.td.demo.distributeId.service;

public interface IZKService {


    /**
     * get or create workId by node
     * @return workId
     * @param localTag
     */
    public String createOrGetWorkId(String localTag);

}
