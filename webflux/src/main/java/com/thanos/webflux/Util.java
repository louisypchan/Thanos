package com.thanos.webflux;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import javax.xml.bind.ValidationException;

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
public class Util {

    public final static String CHANNEL_CACHE_NAMESPACE = "signofcloud:channel:channelcache:";

    public final static String THANOS_SESSION = "T-S-L";

    public final static String NONCE = "NONCE";

    public final static String TIMESTAMP = "timestamp";

    public final static int TIMESTAMP_EXPIRED = 60; //30 seconds

    public final static String SIGNATURE = "sign";

    public static <T> Mono<T> error(int code, String msg){
        return Mono.error(new ValidationException(msg, String.valueOf(code)));
    }

    public static String getIpAddr(ServerHttpRequest request){
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if(StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if(StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeaders().getFirst("HTTP_CLIENT_IP");
        }
        if(StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        }
        if(StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }

        if(StringUtils.isNotBlank(ip) && ip.length() > 15){
            if(ip.indexOf(",")>0){
                ip = ip.substring(0,ip.indexOf(","));
            }
        }
        return ip;
    }
}
