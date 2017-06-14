package com.tiza.ws;

/**
 * Description: IJt808Sender
 * Author: DIYILIU
 * Update: 2017-05-25 10:03
 */

public interface IJt808Sender extends ISender{

    void setParam(int id, String terminalId, int paramId, Object... paramValue);

    void queryParam(int id, String terminalId, int paramId);
}
