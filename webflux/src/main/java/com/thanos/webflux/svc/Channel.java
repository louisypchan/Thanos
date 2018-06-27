package com.thanos.webflux.svc;

/****************************************************************************
 Copyright (c) 2017 Louis Y P Chen.
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
public class Channel {

    private Integer channelStatus;//渠道状态 1正常 2停用
    private String channelCode;//渠道码
    private String channelName;//渠道名
    private String channelAuthCode;//渠道授权码
    private Long adminId;//管理员id
    private String adminName;//管理员名字
    private Integer channel;//渠道
    private Long companyId;//企业id
    private String appId;//应用id
    private String channelSecret;//渠道密钥
    private Integer channelType;//渠道类型 0全部
    private String packageName;//android应用包名
    private String bundleId;//ios应用bundleId
    private String funcPermission;//功能权限
    private String ipWhiteList;//白名单:逗号分隔(最多10个ip)
    private Integer billingMethod;//计费方式 0:免费 1:按签署次数收费 2:按签署文件收费
    private String companyName;//所属企业名

    /**
     * 使用类型
     */
    private Integer useType;
    /**
     * 有效期开始时间
     */
    private String effectiveStart;
    /**
     * 有效期结束时间
     */
    private String effectiveEnd;
    /**
     * 身份证识别的类型,0:骏聿,1:face++,2:免费face++
     */
    private Integer ocrType;


    public Integer getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(Integer channelStatus) {
        this.channelStatus = channelStatus;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelAuthCode() {
        return channelAuthCode;
    }

    public void setChannelAuthCode(String channelAuthCode) {
        this.channelAuthCode = channelAuthCode;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getChannelSecret() {
        return channelSecret;
    }

    public void setChannelSecret(String channelSecret) {
        this.channelSecret = channelSecret;
    }

    public Integer getChannelType() {
        return channelType;
    }

    public void setChannelType(Integer channelType) {
        this.channelType = channelType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getFuncPermission() {
        return funcPermission;
    }

    public void setFuncPermission(String funcPermission) {
        this.funcPermission = funcPermission;
    }

    public String getIpWhiteList() {
        return ipWhiteList;
    }

    public void setIpWhiteList(String ipWhiteList) {
        this.ipWhiteList = ipWhiteList;
    }

    public Integer getBillingMethod() {
        return billingMethod;
    }

    public void setBillingMethod(Integer billingMethod) {
        this.billingMethod = billingMethod;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getUseType() {
        return useType;
    }

    public void setUseType(Integer useType) {
        this.useType = useType;
    }

    public String getEffectiveStart() {
        return effectiveStart;
    }

    public void setEffectiveStart(String effectiveStart) {
        this.effectiveStart = effectiveStart;
    }

    public String getEffectiveEnd() {
        return effectiveEnd;
    }

    public void setEffectiveEnd(String effectiveEnd) {
        this.effectiveEnd = effectiveEnd;
    }

    public Integer getOcrType() {
        return ocrType;
    }

    public void setOcrType(Integer ocrType) {
        this.ocrType = ocrType;
    }
}
