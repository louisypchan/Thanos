package com.thanos.webflux.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanos.common.Utils;
import com.thanos.common.error.ErrorEnum;
import com.thanos.webflux.Util;
import com.thanos.webflux.impl.ValidatorUtils;
import com.thanos.webflux.svc.Response;
import com.thanos.webflux.svc.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

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
public class DefaultHandler {

    private final Logger logger = LoggerFactory.getLogger(DefaultHandler.class);

    @Autowired
    private ApplicationContext context;

    protected <T> Mono<T> bodyToMono(ServerRequest request, Class<T> clz){
        return request.bodyToMono(clz)
                .flatMap(t -> {
                    List<String> headers = request.headers().header(Util.THANOS_SESSION);
                    if(headers != null && headers.size() > 0){
                        Method method = null;
                        try {
                            method = t.getClass().getMethod("setToken", String.class);
                            method.invoke(t, headers.get(0));
                            method = t.getClass().getMethod("setNonce", String.class);
                            method.invoke(t, request.headers().header(Util.NONCE).get(0));
                            return validateRequest(t);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            //do nothing here
                            logger.error(e.getMessage(), e);
                        }
                    }
                    return validate(t);
                });
    }

    protected <T> Mono<T> pathVariables(ServerRequest request, Class<T> cls){
        Map<String, String> newMap = new HashMap<>();
        List<String> headers = request.headers().header(Util.THANOS_SESSION);
        Map<String, String> map = request.pathVariables();
        newMap.putAll(map);
        newMap.putAll(request.queryParams().toSingleValueMap());
        boolean needToValidate = false;
        if(headers != null && headers.size() > 0){
            newMap.put("token", headers.get(0));
            newMap.put("nonce", request.headers().header(Util.NONCE).get(0));
            needToValidate = true;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            T o = objectMapper.readValue(JSON.toJSONString(newMap), cls);
            return needToValidate ? validate(o) : validateRequest(o).flatMap(t -> validate(t));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
            return Util.error(ErrorEnum.CAST_ERROR.getKey(), ErrorEnum.CAST_ERROR.getValue());
        }
    }

    private <T> Mono<T> validateRequest(T o) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
        List<Field> fields = new ArrayList<>() ;
        Class clz = o.getClass();
        while(clz != null && !clz.getName().toLowerCase().equals("java.lang.object")){
            fields.addAll(Arrays.asList(clz .getDeclaredFields()));
            clz = clz.getSuperclass();
        }
        String timestamp = null, value = null, sign = null;
        int i = 0;
        String[] values = new String[fields.size()];
        for (Field field : fields){
            value = getFieldValueByName(field.getName(), o);
            if(field.getName().equals(Util.SIGNATURE)) {
                sign = value;
                continue;
            }
            if(field.getName().equals(Util.TIMESTAMP)){
                timestamp = value;
            }
            values[i++] = value;
        }
        //check sign
        //check whether the timestamp is expired or not
        if(System.currentTimeMillis() - Long.valueOf(timestamp) > Util.TIMESTAMP_EXPIRED * 1000){
            return Util.error(ErrorEnum.REQUEST_TIME_OUT.getKey(), ErrorEnum.REQUEST_TIME_OUT.getValue());
        }
        //check signature
        if(!Utils.sign(values).toLowerCase().equals(sign.toLowerCase())){
            return Util.error(ErrorEnum.SIGNATURE_INVALID.getKey(), ErrorEnum.SIGNATURE_INVALID.getValue());
        }
        return Mono.just(o);
    }

    private String getFieldValueByName(String fieldName, Object o) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
        String firstLetter = fieldName.substring(0, 1).toUpperCase();
        String getter = "get" + firstLetter + fieldName.substring(1);
        Method method = o.getClass().getMethod(getter, new Class[] {});
        Object value = method.invoke(o, new Object[] {});
        return (String) value;
    }

    protected <T> Mono<ServerResponse> body(Mono<T> func){
        return func.flatMap(r -> Response.render(Result.just(r)))
                .switchIfEmpty(Response.render(Result.notfound()))
                .onErrorResume(ValidationException.class, (ValidationException t) -> Response.render(new Result(t.getErrorCode(), t.getMessage(), null)));
    }

    public <T> Mono<T> validate(@Validated T body){
        String className = this.getClass().getName();
        String name = "validate";
        try {
            Class cls = Class.forName(className);
            return ValidatorUtils.get(context).validate(cls, name, body);
        } catch (ClassNotFoundException e) {
            return Util.error(ErrorEnum.CLASS_NOT_FOUND.getKey(), e.getMessage());
        }
    }
}
