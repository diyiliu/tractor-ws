package com.tiza.protocol.jt808.cmd;

import com.tiza.model.SendMSG;
import com.tiza.model.header.Header;
import com.tiza.model.header.Jt808Header;
import com.tiza.protocol.jt808.Jt808DataProcess;
import com.tiza.util.CommonUtil;
import com.tiza.util.config.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public void parse(byte[] content, Header header) {
        Jt808Header jt808Header = (Jt808Header) header;
        // 关键字
        int key = jt808Header.getKey();

        ByteBuf buf = Unpooled.copiedBuffer(content);

        int replySerial = buf.readUnsignedShort();
        int replyCmd = buf.readUnsignedShort();

        byte result = buf.readByte();

        logger.warn("应答流水号[{}]，应答结果[{}]", replySerial, result);

        if (waitRespCacheProvider.containsKey(key)) {
            SendMSG msg = (SendMSG) waitRespCacheProvider.get(key);
            waitRespCacheProvider.remove(key);

            int serial = msg.getSerial();
            int cmd = msg.getCmd();

            if (serial != replySerial || cmd != replyCmd){

                logger.warn("通用应答匹配错误。下行[命令ID:{},流水号:{}]；上行[命令ID:{},流水号:{}]",
                        cmd, serial, replyCmd, replySerial);
                return;
            }

            int value = 5;
            // 执行成功
            if (result == 0){
                value = 1;
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
