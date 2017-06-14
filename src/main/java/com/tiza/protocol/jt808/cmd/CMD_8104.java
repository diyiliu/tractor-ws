package com.tiza.protocol.jt808.cmd;

import com.tiza.protocol.jt808.Jt808DataProcess;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_8104
 * Author: DIYILIU
 * Update: 2017-05-25 15:25
 */

@Service
public class CMD_8104 extends Jt808DataProcess {

    public CMD_8104(){
        this.cmdId = 0x8104;
    }
}
