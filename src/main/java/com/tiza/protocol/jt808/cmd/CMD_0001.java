package com.tiza.protocol.jt808.cmd;

import com.tiza.protocol.jt808.Jt808DataProcess;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_0001
 * Author: DIYILIU
 * Update: 2017-05-25 15:24
 */

@Service
public class CMD_0001 extends Jt808DataProcess{

    public CMD_0001(){
        this.cmdId = 0x0001;
    }
}
