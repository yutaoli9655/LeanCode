package com.cscs.mail;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cscs.pojo.AtFile;
import com.cscs.pojo.Email;
import com.cscs.util.PropUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yutao.li
 * 邮件类
 */
public class SendMail {

    private static Logger logger = Logger.getLogger(SendMail.class);

    public static void main(String[] args) {
        String body = "正文";
        String subject = "主题";
        sendMail(body, subject, null);
    }

    /**
     * 邮件发送状态
     */
    private static final String SUCCESS = "success";
    private static final String SENDING = "sending";
    private static final String ERROR = "error";

    /**
     * 邮件参数
     */
    private static String url;
    private static String destAddress;
    private static String ccAddress;
    private static String bccAddress;
    private static String tokenUrl;
    private static String resultUrl;


    /**
     * 发送邮件接口
     */
    public static Map<String, Object> sendMail(String subject, String body, String token) {
        /**加载参数*/
        url = PropUtils.getString("mail.url");
        destAddress = PropUtils.getString("mail.destAddress");
        ccAddress = PropUtils.getString("mail.ccAddress");
        bccAddress = PropUtils.getString("mail.bccAddress");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        Email email = new Email();
        email.setDestAddress(destAddress);
        email.setBccAddress(bccAddress);
        email.setCcAddress(ccAddress);
        email.setBody(body);
        email.setSubject(subject);
        List<AtFile> atFiles = new ArrayList<>();
        email.setAttachs(atFiles);

        //将数据转json
        String s = JSON.toJSONString(email);
        httpPost.addHeader("Content-type", "application/json; charset=utf-8");
        httpPost.setHeader("X-Access-Token", token);
        HttpResponse response = null;
        Map<String, Object> map = new HashMap<>();
        try {
            StringEntity stringEntity = new StringEntity(s, "utf-8");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity());
                JSONObject parse = (JSONObject) JSON.parse(result);
                map.put("emailId", parse.getString("emailId"));

            } else {
                //发送邮件错误
                String result = EntityUtils.toString(response.getEntity());
                JSONObject parse = (JSONObject) JSON.parse(result);
                map.put("code", parse.getString("code"));
                map.put("message", parse.getString("message"));
            }
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return map;
    }

    /**
     * 获取token
     *
     * @return
     */
    public static Map<String, Object> getToken() {

        tokenUrl = PropUtils.getString("token.url");
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(tokenUrl);
        Map<String, Object> map = new HashMap<>();

        try {
            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity());
                //取出json里面的token
                JSONObject parse = (JSONObject) JSON.parse(result);
                String accessToken = parse.getString("accessToken");
                map.put("accessToken", accessToken);
            } else {
                //获取token出现错误
                String result = EntityUtils.toString(response.getEntity());
                JSONObject parse = (JSONObject) JSON.parse(result);
                map.put("code", parse.getString("code"));
                map.put("message", parse.getString("message"));
            }
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return map;
    }

    /**
     * 查询邮件是否发送成功
     *
     * @return
     */
    public static void queryMail(String emailId, String token) {
        resultUrl = PropUtils.getString("result.url");
        resultUrl = chkDsk(resultUrl) + emailId;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(resultUrl);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("X-Access-Token", token);
        Map<String, Object> map = new HashMap<>();
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity());
                //取出json里面的token
                JSONObject parse = (JSONObject) JSON.parse(result);
                String sendResult = parse.getString("sendResult");
                if (sendResult.equals(SUCCESS)) {
                    logger.info("邮件发送成功");
                } else if (sendResult.equals(SENDING)) {
                    logger.info("邮件发送中");
                } else if (sendResult.equals(ERROR)) {
                    logger.info("邮件发送失败");
                }
            } else {
                String result = EntityUtils.toString(response.getEntity());
                JSONObject parse = (JSONObject) JSON.parse(result);
                map.put("code", parse.getString("code"));
                map.put("message", parse.getString("message"));
                logger.info(map.toString());
            }
        } catch (IOException e) {
            logger.error(e.toString());
        }


    }

    /**
     * 判断url是否以"/"结尾
     *
     * @param directory
     * @return
     */
    private static String DSK = "/";

    public static String chkDsk(String directory) {
        if (!directory.endsWith(DSK)) {
            return directory + DSK;
        } else {
            return directory;
        }
    }
}
