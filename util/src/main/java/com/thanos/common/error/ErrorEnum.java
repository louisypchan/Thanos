package com.thanos.common.error;

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
public enum ErrorEnum {

    SUCCESS(0, "SUCCESS"), // 成功

    FAIL(1, "FAIL"),

    NOT_FOUND(-1, "NOT_FOUND"), //没有记录

    APP_ID_NOT_FOUND(10001, "appid不能为空"),

    APP_ID_ILLEGAL(10002, "appid无效"),

    IP_ILLEGAL(10003, "IP不在有效的名单之内"),

    URL_INVALID(10004, "无效的URL"),

    REQUEST_ILLEGAL(10005, "非法请求"),

    REQUEST_TIME_OUT(10006, "请求已过期"),

    SIGNATURE_INVALID(10007, "签名无效"),

    CAST_ERROR(10008, "对象数据转化错误"),

    TIMESTAMP_NOT_FOUND(10009, "timestamp参数不能为空"),

    NO_SUCH_METHOD(90000, ""),

    CLASS_NOT_FOUND(90001, ""),

    PARAMETERS_INVALID(90002, ""),

    NONSENSE(99999, "NONSENSE");

    private int key = 0;
    private String value = "SUCCESS";

    ErrorEnum(int key, String value){
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
