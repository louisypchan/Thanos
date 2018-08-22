package com.thanos.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Channel {

    @Id
    private String channelId;

    private int status = 0; // 1 - up, 0 - down

    private String appid;

    private String secret;

    private String iPWhiteList = "0";


    private String name;


    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getiPWhiteList() {
        return iPWhiteList;
    }

    public void setiPWhiteList(String iPWhiteList) {
        this.iPWhiteList = iPWhiteList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
