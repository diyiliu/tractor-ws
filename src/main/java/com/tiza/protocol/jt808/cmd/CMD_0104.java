package com.tiza.protocol.jt808.cmd;

import com.tiza.model.SendMSG;
import com.tiza.model.header.Header;
import com.tiza.model.header.Jt808Header;
import com.tiza.protocol.jt808.Jt808DataProcess;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: CMD_0104
 * Author: DIYILIU
 * Update: 2017-05-25 15:25
 */

@Service
public class CMD_0104 extends Jt808DataProcess {

    public CMD_0104(){
        this.cmdId = 0x0104;
    }

    @Resource
    private ICache waitRespCacheProvider;

    @Override
    public void parse(byte[] content, Header header) {
        Jt808Header jt808Header = (Jt808Header) header;
        // 关键字
        int key = jt808Header.getKey();

        ByteBuf buf = Unpooled.copiedBuffer(content);

        int replySerial = buf.readUnsignedShort();

        // 仅有单个查询
        byte count = buf.readByte();

        int paramId = buf.readInt();
        byte length = buf.readByte();

        Integer[] group1 = new Integer[]{0x0001, 0x0002, 0x0003, 0x0004, 0x0005,
                0x0006, 0x0007, 0x0018, 0x0019, 0x0020,
                0x0021, 0x0027, 0x0028, 0x0029, 0x0093, 0x0095};

        Integer[] group2 = new Integer[]{0x0011, 0x0012, 0x0013, 0x0014, 0x0015,
                0x0016, 0x0017};

        Integer[] group3 = new Integer[]{0x0090, 0x0091, 0x0092, 0x0094, 0xF023, 0xF024};

        Object value = null;
        if (Arrays.asList(group1).contains(paramId) && length == 4) {

             value = buf.readInt();
        } else if (Arrays.asList(group2).contains(paramId)) {

            byte[] str = new byte[length];
            buf.readBytes(str);
            value = new String(str, Charset.forName("GBK"));
        } else if (Arrays.asList(group3).contains(paramId) && length == 1) {

            value = buf.readByte();
        }

        logger.info("查询结果:{}", value);

        if (waitRespCacheProvider.containsKey(key)){
            SendMSG msg = (SendMSG) waitRespCacheProvider.get(key);
            waitRespCacheProvider.remove(key);

            int serial = msg.getSerial();
            if (serial != replySerial){
                logger.warn("匹配应答流水号错误:[下行流水号{},应答流水号{}]", serial, replySerial);
                return;
            }

            Map paramMap = new HashMap();
            paramMap.put("RESULTSTATUS", 1);
            paramMap.put("RESPONSEDATA", value);
            paramMap.put("RESPONSETIME", new Date());

            Map conditionMap = new HashMap();
            conditionMap.put("ID", key);

            // 持久化数据库
            CommonUtil.dealToDb(Constant.DBInfo.DB_TRACTOR_USER, Constant.DBInfo.DB_TRACTOR_INSTRUCTION,
                    paramMap, conditionMap, String.valueOf(key));
        }
    }
}
