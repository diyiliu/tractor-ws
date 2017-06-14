package com.tiza.protocol.jt808.cmd;

import com.tiza.model.header.Header;
import com.tiza.model.header.Jt808Header;
import com.tiza.protocol.jt808.Jt808DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_8106
 * Author: DIYILIU
 * Update: 2017-05-25 15:25
 */

@Service
public class CMD_8106 extends Jt808DataProcess {

    public CMD_8106(){
        this.cmdId = 0x8106;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        Jt808Header jt808Header = (Jt808Header) header;

        Object[] params = argus;

        ByteBuf buf = Unpooled.buffer(1 + 4 * params.length);
        buf.writeByte(params.length);
        for (Object o: params){
            buf.writeInt((Integer) o);
        }
        byte[] content = buf.array();

        return headerToSendBytes(content, this.cmdId, jt808Header);
    }
}
