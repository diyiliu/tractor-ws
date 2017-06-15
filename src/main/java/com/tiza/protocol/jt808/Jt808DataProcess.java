package com.tiza.protocol.jt808;

import com.tiza.model.SendMSG;
import com.tiza.model.header.Header;
import com.tiza.model.header.Jt808Header;
import com.tiza.protocol.IDataProcess;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.task.impl.MSGSenderTask;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description: Jt808DataProcess
 * Author: DIYILIU
 * Update: 2017-05-25 14:00
 */
@Service
public class Jt808DataProcess implements IDataProcess {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache jt808CMDCacheProvider;

    @Resource
    protected ICache waitRespCacheProvider;

    protected int cmdId = 0xFFFF;

    @Value("${terminalType}")
    protected String terminalType = "jt808";

    @Override
    public Header dealHeader(byte[] bytes) {
        byte[] content = decode(bytes);

        ByteBuf buf = Unpooled.copiedBuffer(content);
        // 读取消息头标识位
        buf.readByte();

        buf.markReaderIndex();
        byte[] checkArray = new byte[bytes.length - 3];
        buf.readBytes(checkArray);
        // 计算校验位
        byte checkReady = CommonUtil.getCheck(checkArray);
        buf.resetReaderIndex();


        short cmd = buf.readShort();
        int bodyProperty = buf.readUnsignedShort();
        Jt808Header jt808Header = decodeBodyProperty(bodyProperty);

        byte[] array = new byte[6];
        buf.readBytes(array);
        String terminalId = CommonUtil.bytesToStr(array);

        int serial = buf.readUnsignedShort();

        int length = jt808Header.getLength();
        byte[] bodyContent = new byte[length];
        buf.readBytes(bodyContent);

        byte check = buf.readByte();
        // 读取消息尾标识位
        buf.readByte();

        if (check != checkReady){

            logger.error("校验位验证失败，指令[{}]", CommonUtil.bytesToString(content));
            return null;
        }


        jt808Header.setCmd(cmd);
        jt808Header.setTerminalId(terminalId);
        jt808Header.setSerial(serial);
        jt808Header.setContent(bodyContent);
        jt808Header.setCheck(check);

        return jt808Header;
    }

    @Override
    public void parse(byte[] content, Header header) {

    }

    /**
     * 下发指令
     *
     * @param id    数据库ID
     * @param cmd   命令号
     * @param argus 下发参数
     */
    public void send(int cmd, String terminalId, int id, Object... argus) {

        Jt808Header header = new Jt808Header();
        header.setCmd(cmd);
        header.setTerminalId(terminalId);
        header.setSerial(getMsgSerial());
        // 是否 加密
        header.setEncrypt(0);
        // 是否 分包(长消息需要分包处理)
        header.setSplit((byte) 0);

        Jt808DataProcess process = (Jt808DataProcess) jt808CMDCacheProvider.get(cmd);
        byte[] content = process.pack(header, argus);

        // 加入下发指令队列
        put(terminalId, cmd, content, id, header.getSerial(), terminalType);
    }


    /**
     * 消息头（消息头 + 消息体 的校验位）
     * （无分包）
     *
     * @param header
     * @return
     */
    public byte[] headerToContent(Jt808Header header) {

        ByteBuf buf = Unpooled.buffer(header.getLength() + 12);
        buf.writeShort(header.getCmd());
        buf.writeBytes(encodeBodyProperty(header));
        buf.writeBytes(CommonUtil.packBCD(Long.parseLong(header.getTerminalId()), 6));
        buf.writeShort(header.getSerial());
        buf.writeBytes(header.getContent());

        return buf.array();
    }

    public byte[] headerToBytes(Jt808Header header) {

        // 加入 校验位
        byte[] content = Unpooled.copiedBuffer(headerToContent(header),
                new byte[]{header.getCheck()}).array();

        // 特殊字节 转义
        content = encode(content);

        ByteBuf buf = Unpooled.buffer(content.length + 2);
        buf.writeByte(0x7E);
        buf.writeBytes(content);
        buf.writeByte(0x7E);

        return buf.array();
    }

    // 下发数据
    public byte[] headerToSendBytes(byte[] content, int cmd, Jt808Header header) {

        header.setLength(content.length);
        header.setContent(content);
        header.setCmd(cmd);

        // 获取校验位
        byte[] bytes = headerToContent(header);
        byte check = CommonUtil.getCheck(bytes);
        header.setCheck(check);

        return headerToBytes(header);
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        return new byte[0];
    }

    @Override
    public void init() {
        jt808CMDCacheProvider.put(cmdId, this);
    }

    private static AtomicLong msgSerial = new AtomicLong(0);

    /**
     * 生成命令流水号
     *
     * @return
     */
    protected static int getMsgSerial() {
        Long serial = msgSerial.incrementAndGet();
        if (serial > 65535) {
            msgSerial.set(0);
            serial = msgSerial.incrementAndGet();
        }

        return serial.intValue();
    }

    /**
     * @param terminalId
     * @param cmd
     * @param content
     * @param id
     * @param serial
     * @param terminalType
     */
    protected void put(String terminalId, int cmd, byte[] content, int id, int serial, String terminalType) {

        SendMSG msg = new SendMSG(terminalId, cmd, content);
        msg.setId(id);
        msg.setSerial(serial);
        msg.setTerminalType(terminalType);
        msg.setWaitCount(0);

        MSGSenderTask.send(msg);
    }


    private byte[] encodeBodyProperty(Jt808Header header) {
        byte[] bytes = new byte[2];

        byte temp = (byte) (header.getSplit() & 0x01);
        temp = (byte) ((temp << 3) | (header.getEncrypt() & 0x01));
        bytes[0] = (byte) ((temp << 1) | ((header.getLength() >> 8) & 0x01));
        bytes[1] = (byte) (header.getLength() & 0xFF);

        return bytes;
    }

    private Jt808Header decodeBodyProperty(int bodyProperty){

        int split = (bodyProperty >> 13) & 0x01;
        int encrypt = (bodyProperty >> 10) & 0x07;
        int length = bodyProperty & 0x3FF;

        Jt808Header jt808Header = new Jt808Header();
        jt808Header.setSplit((byte) split);
        jt808Header.setEncrypt(encrypt);
        jt808Header.setLength(length);

        return jt808Header;
    }


    /**
     * transfer 编码
     * 0x7e ————> 0x7d 后紧跟一个0x02
     * 0x7d ————> 0x7d 后紧跟一个0x01
     *
     * @param bytes
     * @return
     */
    private byte[] encode(byte[] bytes) {

        String hex = CommonUtil.bytesToStr(bytes).toUpperCase();

        hex.replaceAll("7D", "7D01");
        hex.replaceAll("7E", "7D02");

        byte[] array = CommonUtil.hexStringToBytes(hex);

        if (array == null) {
            logger.error("封装0x7D,0x7E异常！[{}]", hex);
        }

        return array;
    }


    /**
     * transfer 解码
     * 0x7d0x02 ————> 0x7e
     * 0x7d0x01 ————> 0x7d
     *
     * @param bytes
     * @return
     */
    private byte[] decode(byte[] bytes) {

        String hex = CommonUtil.bytesToStr(bytes).toUpperCase();

        hex.replaceAll("7D01", "7D");
        hex.replaceAll("7D02", "7E");

        byte[] array = CommonUtil.hexStringToBytes(hex);

        if (array == null) {
            logger.error("解封装0x7D01,0x7D02异常！[{}]", hex);
        }

        return array;
    }
}
