package com.cscs.pojo;

import java.util.List;

/**
 * 邮件发送类
 */
public class Email {

    /**
     * 收件人
     */
    private String destAddress;
    /**
     * 抄送人
     */
    private String ccAddress;
    /**
     * 密送人
     */
    private String bccAddress;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 邮件正文
     */
    private String body;
    /**
     * 邮件附件对象数组
     */
    private List<AtFile> attachs;

    public String getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    public String getCcAddress() {
        return ccAddress;
    }

    public void setCcAddress(String ccAddress) {
        this.ccAddress = ccAddress;
    }

    public String getBccAddress() {
        return bccAddress;
    }

    public void setBccAddress(String bccAddress) {
        this.bccAddress = bccAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<AtFile> getAttachs() {
        return attachs;
    }

    public void setAttachs(List<AtFile> attachs) {
        this.attachs = attachs;
    }

    @Override
    public String toString() {
        return "Email{" +
                "destAddress='" + destAddress + '\'' +
                ", ccAddress='" + ccAddress + '\'' +
                ", bccAddress='" + bccAddress + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", attachs=" + attachs +
                '}';
    }
}
