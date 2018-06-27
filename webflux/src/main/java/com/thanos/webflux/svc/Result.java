package com.thanos.webflux.svc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thanos.common.error.ErrorEnum;

import java.lang.reflect.Method;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private String code;

    private String msg;

    private T data;


    public Result(){
        this.code = String.valueOf(ErrorEnum.SUCCESS.getKey());
        this.msg = ErrorEnum.SUCCESS.getValue();
    }

    public Result(String code, String msg){
        this.code = code;
        this.msg = msg;
    }

    private Result(T data) {
        this.code = String.valueOf(ErrorEnum.SUCCESS.getKey());
        this.msg = ErrorEnum.SUCCESS.getValue();
        this.data = data;
    }

    public Result(String code, String message, T data) {
        this.code = code;
        this.msg = message;
        this.data = data;
    }

    public static <T> Result just(T data){
        Result result = new Result(data);
        Object o = getObjectValue(data, "getCode");
        if(o != null && o instanceof String)
            result.setCode((String) o);
        o = getObjectValue(data, "getMsg");
        if(o != null && o instanceof String)
            result.setMsg((String) o);

        return result;
    }

    private static Object getObjectValue(Object object, String getMethod){
        try {
            Class c = object.getClass();
            Method m = c.getDeclaredMethod(getMethod);
            return m.invoke(object);
        } catch (Exception e) {

        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static Result notfound(){
        return new Result(String.valueOf(ErrorEnum.NOT_FOUND.getKey()), ErrorEnum.NOT_FOUND.getValue());
    }

    @Override
    public String toString() {
        return "Result [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }
}
