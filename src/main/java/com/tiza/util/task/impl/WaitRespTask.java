package com.tiza.util.task.impl;

import cn.com.tiza.tstar.datainterface.client.TStarStandardClient;
import cn.com.tiza.tstar.datainterface.client.entity.ClientCmdCheckResult;
import cn.com.tiza.tstar.datainterface.client.entity.ClientCmdSendResult;
import cn.com.tiza.tstar.datainterface.service.ServerException;
import com.tiza.model.SendMSG;
import com.tiza.model.header.Jt808Header;
import com.tiza.protocol.IDataProcess;
import com.tiza.protocol.jt808.Jt808DataProcess;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import com.tiza.util.task.ITask;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: WaitRespTask
 * Author: DIYILIU
 * Update: 2016-03-22 9:17
 */
public class WaitRespTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${tstar.waitTime}")
    private int waitTime = 3;

    @Value("${tstar.waitCount}")
    private int waitCount = 5;

    @Resource
    private ICache monitorCacheProvider;

    @Resource
    private ICache waitRespCacheProvider;

    @Resource
    private TStarStandardClient tStarClient;

    @Resource
    private IDataProcess jt808DataProcess;

    @Resource
    private ICache jt808CMDCacheProvider;

    @Override
    public void execute() {
        Date now = new Date();

        Set keys = waitRespCacheProvider.getKeys();
        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            // 下发ID
            Integer key = (Integer) iterator.next();

            SendMSG sendMSG = (SendMSG) waitRespCacheProvider.get(key);
            int count = sendMSG.getWaitCount() + 1;

            // 超时未响应
            if (count > waitCount){
                logger.warn("指令流水号[{}]，超时未响应！", sendMSG.getSerial());
                waitRespCacheProvider.remove(key);

                Map paramMap = new HashMap();
                paramMap.put("RESULTSTATUS", 4);
                Map conditionMap = new HashMap();
                conditionMap.put("ID", sendMSG.getId());

                // 持久化数据库
                CommonUtil.dealToDb(Constant.DBInfo.DB_TRACTOR_USER, Constant.DBInfo.DB_TRACTOR_INSTRUCTION,
                        paramMap, conditionMap, String.valueOf(sendMSG.getId()));
            }

            if ((now.getTime() - sendMSG.getSendTime().getTime()) > count * waitTime * 1000) {
                ClientCmdSendResult sendResult = sendMSG.getSendResult();
                sendMSG.setWaitCount(count);

                try {
                    String checkId = sendResult.getCmdCheckId();
                    ClientCmdCheckResult checkResult = tStarClient.cmdCheck(checkId);

                    boolean respIsSuccess = checkResult.getIsSuccess();
                    if (respIsSuccess) {
                        String replyBody = checkResult.getCmdReplyBody();

                        if (CommonUtil.isEmpty(replyBody)) {
                            if (waitCount > count) {

                                logger.info("指令流水号[{}]，还剩下[{}]秒响应时间...", sendMSG.getSerial(), (waitCount - count) * waitTime);
                            }
                        } else {
                            // 返回数据
                            byte[] content = Base64.decodeBase64(replyBody);

                            Jt808Header jt808Header = (Jt808Header) jt808DataProcess.dealHeader(content);
                            jt808Header.setKey(sendMSG.getId());
                            if (jt808Header != null) {
                                // 重点监控
                                //if (monitorCacheProvider.containsKey(terminalId)) {
                                logger.info("接收消息，终端[{}], 命令[{}], 原始数据[{}]", jt808Header.getTerminalId(), CommonUtil.toHex(jt808Header.getCmd()), CommonUtil.bytesToString(content));
                                //}

                                jt808DataProcess = (IDataProcess) jt808CMDCacheProvider.get(jt808Header.getCmd());
                                jt808DataProcess.parse(jt808Header.getContent(), jt808Header);
                            }
                        }
                    }
                } catch (ServerException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 应答超时
     *
     * @param id
     * @param cmd
     */
    public void toDB(int id, int cmd) {

        Map valueMap = new HashMap() {
            {
                this.put("ResponseStatus", 10);
            }
        };

        Map whereMap = new HashMap();
        whereMap.put("Id", id);

//        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER, Constant.DBInfo.DB_CLOUD_INSTRUCTION, valueMap, whereMap);
    }
}
