package com.tiza.protocol.jt808.cmd;

import com.tiza.model.header.Header;
import com.tiza.model.header.Jt808Header;
import com.tiza.protocol.jt808.Jt808DataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Description: CMD_8103
 * Author: DIYILIU
 * Update: 2017-05-25 15:24
 */

@Service
public class CMD_8103 extends Jt808DataProcess {

    public CMD_8103() {
        this.cmdId = 0x8103;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {

        Jt808Header jt808Header = (Jt808Header) header;

        int paramId = (int) argus[0];
        Object[] paramValue = (Object[]) argus[1];

        Integer[] group1 = new Integer[]{0x0001, 0x0002, 0x0003, 0x0004, 0x0005,
                0x0006, 0x0007, 0x0018, 0x0019, 0x0020,
                0x0021, 0x0027, 0x0028, 0x0029, 0x0093, 0x0095};

        Integer[] group2 = new Integer[]{0x0011, 0x0012, 0x0013, 0x0014, 0x0015,
                0x0016, 0x0017};

        Integer[] group3 = new Integer[]{0x0090, 0x0091, 0x0092, 0x0094, 0xF023, 0xF024};

        ByteBuf buf = Unpooled.buffer();
        int count = paramValue.length;

        int len = 1;
        buf.writeByte(count);
        for (int i = 0; i < count; i++) {
            len += 4;
            buf.writeInt(paramId);

            if (Arrays.asList(group1).contains(paramId)) {

                len += 5;
                buf.writeByte(4);
                buf.writeInt(Integer.parseInt(String.valueOf(paramValue[i])));
            } else if (Arrays.asList(group2).contains(paramId)) {

                String value = String.valueOf(paramValue[i]);
                byte[] bytes = value.getBytes(Charset.forName("GBK"));

                len += (bytes.length + 1);
                buf.writeByte(bytes.length);
                buf.writeBytes(bytes);
            } else if (Arrays.asList(group3).contains(paramId)) {

                len += 2;
                buf.writeByte(1);
                buf.writeByte(Integer.parseInt(String.valueOf(paramValue[i])));
            }
        }
        byte[] content = new byte[len];
        buf.readBytes(content);

        return headerToSendBytes(content, this.cmdId, jt808Header);
    }
}
