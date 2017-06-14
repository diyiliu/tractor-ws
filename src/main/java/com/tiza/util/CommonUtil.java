package com.tiza.util;

import com.tiza.util.bean.SqlBody;
import com.tiza.util.cache.ICache;
import com.tiza.util.client.impl.DBClient;
import com.tiza.util.config.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: CommonUtil
 * Author: DIYILIU
 * Update: 2015-09-17 9:15
 */
public class CommonUtil {

    public static boolean isEmpty(String str) {

        if (str == null || str.trim().length() < 1) {
            return true;
        }

        return false;
    }

    public static byte[] ipToBytes(String host) {

        String[] array = host.split("\\.");

        byte[] bytes = new byte[array.length];

        for (int i = 0; i < array.length; i++) {

            bytes[i] = (byte) Integer.parseInt(array[i]);
        }

        return bytes;
    }

    public static String bytesToIp(byte[] bytes) {

        if (bytes.length == 4) {

            StringBuilder builder = new StringBuilder();

            for (byte b : bytes) {

                builder.append((int) b & 0xff).append(".");
            }

            return builder.substring(0, builder.length() - 1);
        }

        return null;
    }

    public static byte[] dateToBytes(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR) - 2000;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        return new byte[]{(byte) year, (byte) month, (byte) day, (byte) hour, (byte) minute, (byte) second};
    }

    public static Date bytesToDate(byte[] bytes) {

        if (bytes.length < 3) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, 2000 + bytes[0]);
        calendar.set(Calendar.MONTH, bytes[1] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, bytes[2]);

        if (bytes.length == 6) {

            calendar.set(Calendar.HOUR_OF_DAY, bytes[3]);
            calendar.set(Calendar.MINUTE, bytes[4]);
            calendar.set(Calendar.SECOND, bytes[5]);

        } else if (bytes.length == 3) {

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else {

            return null;
        }

        return calendar.getTime();
    }

    public static long bytesToLong(byte[] bytes) {

        long l = 0;
        for (int i = 0; i < bytes.length; i++) {
            l += (long) ((bytes[i] & 0xff) * Math.pow(256, bytes.length - i - 1));
        }
        return l;
    }


    public static byte[] longToBytes(long number, int length) {

        long temp = number;

        byte[] bytes = new byte[length];

        for (int i = bytes.length - 1; i > -1; i--) {

            bytes[i] = new Long(temp & 0xff).byteValue();

            temp = temp >> 8;

        }

        return bytes;
    }

    public static String bytesToStr(byte[] bytes) {
        StringBuffer buf = new StringBuffer();
        for (byte a : bytes) {
            buf.append(String.format("%02X", getNoSin(a)));
        }

        return buf.toString();
    }

    public static String bytesToString(byte[] bytes) {
        StringBuffer buf = new StringBuffer();
        for (byte a : bytes) {
            buf.append(String.format("%02X", getNoSin(a))).append(" ");
        }

        return buf.substring(0, buf.length() - 1);
    }

    public static byte[] hexStringToBytes(String hex) {

        char[] charArray = hex.toCharArray();

        if (charArray.length % 2 != 0) {
            // 无法转义
            return null;
        }

        int length = charArray.length / 2;
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {

            String b = new String(new char[]{charArray[i * 2], charArray[i * 2 + 1]});
            bytes[i] = (byte) Integer.parseInt(b, 16);
        }

        return bytes;
    }


    public static String toHex(int i) {

        return String.format("%02X", i);
    }

    public static int getNoSin(byte b) {
        if (b >= 0) {
            return b;
        } else {
            return 256 + b;
        }
    }

    public static double keepDecimal(double d, int digit) {

        BigDecimal decimal = new BigDecimal(d);
        decimal = decimal.setScale(digit, RoundingMode.HALF_UP);

        return decimal.doubleValue();
    }

    public static String parseBytes(byte[] array, int offset, int lenght) {

        ByteBuf buf = Unpooled.copiedBuffer(array, offset, lenght);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        return new String(bytes);
    }

    public boolean isInnerIp(String address) {
        String ip = address.substring(0, address.indexOf(":"));
        String reg = "(127[.]0[.]0[.]1)|(localhost)|(10[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3})|(172[.]((1[6-9])|(2\\d)|(3[01]))[.]\\d{1,3}[.]\\d{1,3})|(192[.]168[.]\\d{1,3}[.]\\d{1,3})";
        Pattern pat = Pattern.compile(reg);
        Matcher mat = pat.matcher(ip);

        return mat.find();
    }

    public static String parseVIN(byte[] array, int offset) {

        ByteBuf buf = Unpooled.copiedBuffer(array);
        buf.readBytes(new byte[offset]);

        int len = buf.readByte();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);

        return new String(bytes);
    }

    public static byte[] restoreBinary(String content) {

        String[] array = content.split(" ");

        byte[] bytes = new byte[array.length];

        for (int i = 0; i < array.length; i++) {

            bytes[i] = Integer.valueOf(array[i], 16).byteValue();
        }

        return bytes;
    }

    public static String parseSIM(byte[] bytes) {

        Long sim = 0l;
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            sim += (long) (bytes[i] & 0xff) << ((len - i - 1) * 8);
        }

        return sim.toString();
    }

    public static byte[] packSIM(String sim, int len) {

        byte[] array = new byte[len];
        Long simL = Long.parseLong(sim);

        for (int i = 0; i < array.length; i++) {
            Long l = (simL >> (i * 8)) & 0xff;
            array[array.length - 1 - i] = l.byteValue();
        }
        return array;
    }

    public static byte[] packBCD(Long sim, int len) {
        String str = String.format("%0" + len * 2 + "d", sim);

        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            int num = Integer.parseInt(str.substring(i * 2, (i + 1) * 2), 16);
            bytes[i] = (byte) num;
        }

        return bytes;
    }


    public static String parseIMEI(byte[] bytes) {

        String imei = bytesToStr(bytes);

        return imei.substring(0, 15);
    }

    public static byte[] packIMEI(String imei) {

        if (imei.length() == 15) {
            imei += 0;
        }

        return hexStringToBytes(imei);
    }


    public static byte getCheck(byte[] bytes) {
        byte b = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            b ^= bytes[i];
        }

        return b;
    }

    public static int renderHeight(byte[] bytes) {

        int plus = bytes[0] & 0x80;

        bytes[0] &= 0x7F;

        if (plus == 0) {
            return (int) bytesToLong(bytes);
        }

        return 0 - (int) bytesToLong(bytes);
    }

    public static void dealToDb(String user, String table, Map values) {

        CreateSqlUtil sqlUtil = new CreateSqlUtil();
        sqlUtil.setSqlType(1);
        sqlUtil.setTable(table);
        sqlUtil.setUser(user);
        sqlUtil.setValues(values);
        sqlUtil.createSql();

//        DBPClient.sendSQL(sqlUtil.getSql());
    }

    public static void dealToDb(String user, String table, Map values, Map whereCase, String key) {
        CreateSqlUtil sqlUtil = new CreateSqlUtil();
        sqlUtil.setSqlType(2);
        sqlUtil.setTable(table);
        sqlUtil.setUser(user);
        sqlUtil.setValues(values);
        sqlUtil.setWhereCase(whereCase);
        sqlUtil.createSql();

        SqlBody sqlBody = new SqlBody(Constant.DBInfo.DB_OP_UPDATE, table, sqlUtil.getSql(), key);
        ICache sqlCache = SpringUtil.getBean("updateSqlCacheProvider");

        joinSqlPool(sqlBody, sqlCache);
    }

    public static void joinSqlPool(SqlBody sqlBody, ICache sqlCache){
        String key = sqlBody.getKey() + "-" + sqlBody.getTable() + "-" + sqlBody.getType();
        String sql = sqlBody.getSql();

        sqlCache.put(key, sql);
    }

    public static void dealToDb(String sql) {

//        DBPClient.sendSQL(sql);
    }

    public static void toRawData(String terminal, int cmd, int flow, byte[] content) {

        Map map = new HashMap();
        map.put("DeviceId", terminal);
        map.put("ReceiveTime", new Date());
        map.put("DataFlow", flow);
        map.put("Instruction", CommonUtil.toHex(cmd));
        map.put("RawData", CommonUtil.bytesToStr(content));

        dealToDb(Constant.DBInfo.DB_TRACTOR_USER, Constant.DBInfo.DB_TRACTOR_INSTRUCTION, map);
    }


    public static String monthTable(String table, Date date) {

        return table + DateUtil.dateToString(date, "%1$tY%1$tm");
    }


    public static int getBits(int val, int start, int len) {
        int left = 31 - start;
        int right = 31 - len + 1;
        return (val << left) >>> right;
    }

    public static byte[] byteToByte(byte[] workParamBytes, int start, int len, String endian) {
        byte[] tempBytes = new byte[len];
        int totalLen = start + len - 1;

        if (endian.equalsIgnoreCase("little")) {
            int tempI = 0;
            for (int j = totalLen; j >= start; j--) {// 小端模式
                tempBytes[tempI] = workParamBytes[j];
                tempI++;
            }
        } else {
            int tempI = 0;
            for (int j = start; j <= totalLen; j++) {// 大端模式
                tempBytes[tempI] = workParamBytes[j];
                tempI++;
            }
        }
        return tempBytes;
    }

    public static int byte2int(byte[] array) {

        if (array.length < 4) {
            return byte2short(array);
        }

        int r = 0;
        for (int i = 0; i < array.length; i++) {
            r <<= 8;
            r |= array[i] & 0xFF;
        }

        return r;
    }

    public static short byte2short(byte[] array) {

        short r = 0;
        for (int i = 0; i < array.length; i++) {
            r <<= 8;
            r |= array[i] & 0xFF;
        }

        return r;
    }

    /**
     * 解析算数表达式
     *
     * @param exp
     * @return
     */
    public static String parseExp(int val, String exp, String type) throws ScriptException {

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        String retVal = "";
        if (type.equalsIgnoreCase("hex")) {
            retVal = String.format("%02X", val);
        } else if (type.equalsIgnoreCase("decimal")) {
            retVal = engine.eval(val + exp).toString();
        } else {
            //表达式解析会出现类型问题
            retVal = engine.eval(val + exp).toString();
        }

        return retVal;
    }

    public static void main(String[] args) {


        /**
         byte[] array = new byte[]{0x03, 0x3D, 0x55, 0x7A, 0x39};
         String sim = null;
         sim = parseSIM(array);

         System.out.println(sim);
         array =  packSIM(sim);
         sim = parseSIM(array);

         System.out.println(sim);
         */

        // System.out.println(keepDecimal(12, 3));

        System.out.println(toHex(4353));
    }


}
