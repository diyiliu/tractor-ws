package com.tiza.model.header;

/**
 * Description: Jt808Header
 * Author: DIYILIU
 * Update: 2017-05-25 14:08
 */
public class Jt808Header extends Header {

    private int cmd;
    private int length;
    private int encrypt;
    private byte split;
    private String terminalId;
    private int serial;
    private byte[] content = null;
    private byte check;
    private int packageCount;
    // 下行指令ID
    private int key;

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public byte getSplit() {
        return split;
    }

    public void setSplit(byte split) {
        this.split = split;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte getCheck() {
        return check;
    }

    public void setCheck(byte check) {
        this.check = check;
    }

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
