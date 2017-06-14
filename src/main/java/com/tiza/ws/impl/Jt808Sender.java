package com.tiza.ws.impl;

import com.tiza.protocol.jt808.Jt808DataProcess;
import com.tiza.ws.IJt808Sender;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

/**
 * Description: Jt808Sender
 * Author: DIYILIU
 * Update: 2017-05-25 10:01
 */

@WebService
public class Jt808Sender implements IJt808Sender {

    private String address;

    @Resource
    private Jt808DataProcess jt808DataProcess;

    @Override
    public void setParam(int id, String terminalId, int paramId, Object... paramValue) {

        jt808DataProcess.send(0x8103, terminalId, id, paramId, paramValue);
    }

    @Override
    public void queryParam(int id, String terminalId, int paramId) {

        jt808DataProcess.send(0x8106, terminalId, id, paramId);
    }

    /**
     * 发布webService
     * (指令下发)
     */
    @Override
    @WebMethod(exclude = true)
    public void init() {

        Endpoint.publish(address, this);
    }

    @Override
    @WebMethod(exclude = true)
    public void setAddress(String address) {
        this.address = address;
    }
}
