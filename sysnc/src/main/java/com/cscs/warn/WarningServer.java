package com.cscs.warn;

import com.cscs.mail.SendMail;
import com.cscs.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WarningServer {


    private static final Logger logger = LoggerFactory.getLogger(WarningServer.class);

    /**
     * 通用异常预警
     * @param subject
     * @param body
     */
    public static void commonUseWarning(String subject,String body){

        Map<String, Object> map = SendMail.getToken();

        String token = (String) map.get("accessToken");
        if (StringUtils.isEmpty(token)) {
            logger.error(map.toString());
        } else {
            logger.info("获取token成功:"+token);
            Map<String, Object> map2 = SendMail.sendMail(subject, body, token);
            String emailId = (String) map2.get("emailId");
            if (StringUtils.isEmpty(emailId)) {
                logger.error(map2.toString());
            } else {
                SendMail.queryMail(emailId, token);
            }
        }

    }


    public static void dataSyncFailWarning(StringBuffer body){
        String subject = "数据同步失败";
        Map<String, Object> map = SendMail.getToken();
        String token = (String) map.get("accessToken");
        if (token == null || token == "") {
            logger.error(map.toString());
        } else {
            String bt = "本次同步失败的表数据列表和错误信息:" + "\n";
            String zw = bt + body.toString();
            Map<String, Object> map2 = SendMail.sendMail(subject, zw, token);
            String emailId = (String) map2.get("emailId");
            if (emailId == null || emailId == "") {
                logger.error(map2.toString());
            } else {
                SendMail.queryMail(emailId, token);
            }
        }
    }



}
