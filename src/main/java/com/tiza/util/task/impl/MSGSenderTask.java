package com.tiza.util.task.impl;

import cn.com.tiza.tstar.datainterface.client.TStarStandardClient;
import cn.com.tiza.tstar.datainterface.client.entity.ClientCmdSendResult;
import cn.com.tiza.tstar.datainterface.service.ServerException;
import com.tiza.model.SendMSG;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import com.tiza.util.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Description: MSGSenderTask
 * Author: DIYILIU
 * Update: 2016-03-21 13:55
 */

public class MSGSenderTask implements ITask {

    private static ConcurrentLinkedQueue<SendMSG> msgPool = new ConcurrentLinkedQueue<SendMSG>();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache monitorCacheProvider;

    @Resource
    private ICache waitRespCacheProvider;

    @Resource
    private TStarStandardClient tStarClient;

    @Override
    public void execute() {

        while (!msgPool.isEmpty()) {

            SendMSG msg = msgPool.poll();

            String terminalId = msg.getTerminalId();
            int cmd = msg.getCmd();
            byte[] content = msg.getContent();

            // 重点监控
            //if (monitorCacheProvider.containsKey(terminalId)) {
                logger.info("下发消息，终端[{}], 命令[{}], 原始数据[{}]", terminalId, CommonUtil.toHex(cmd), CommonUtil.bytesToString(content));
            //}

            int status;
            try {
                // TStar 指令下发
                ClientCmdSendResult sendResult = tStarClient.cmdSend(msg.getTerminalType(), msg.getTerminalId(),
                        cmd, msg.getSerial(), content, 1);

                // 等待处理指令返回结果
                msg.setSendTime(new Date());
                msg.setSendResult(sendResult);

                // 指令下发成功
                if (sendResult.getIsSuccess()){
                    status = 6;
                    waitRespCacheProvider.put(msg.getId(), msg);
                }else {
                    status = 2;
                    int errorCode = sendResult.getErrorCode();
                    logger.error("指令下发失败，错误代码[{}]", errorCode);
                }
            } catch (ServerException e) {
                status = 3;
                e.printStackTrace();
            }

            Map paramMap = new HashMap();
            paramMap.put("RESULTSTATUS", status);

            Map conditionMap = new HashMap();
            conditionMap.put("ID", msg.getId());

            // 持久化数据库
            CommonUtil.dealToDb(Constant.DBInfo.DB_TRACTOR_USER, Constant.DBInfo.DB_TRACTOR_INSTRUCTION,
                    paramMap, conditionMap, String.valueOf(msg.getId()));
        }

    }

    public static void send(SendMSG msg) {

        msgPool.add(msg);
    }
}
