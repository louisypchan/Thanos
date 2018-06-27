package com.thanos.webflux.filter;

import com.alibaba.fastjson.JSON;
import com.thanos.common.Utils;
import com.thanos.common.error.ErrorEnum;
import com.thanos.common.redis.StringRedis;
import com.thanos.webflux.Util;
import com.thanos.webflux.config.OAuthPatternProperties;
import com.thanos.webflux.impl.DefaultAuthenticationEntryPoint;
import com.thanos.webflux.interf.AuthenticationEntryPoint;
import com.thanos.webflux.svc.Channel;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import javax.xml.bind.ValidationException;
import java.util.Arrays;

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
public class WebfluxWebFilter implements WebFilter {

    private final OAuthPatternProperties oAuthPatternProperties;

    private final StringRedis stringRedis;

    private PathPatternParser pathPatternParser = new PathPatternParser();

    private AuthenticationEntryPoint entryPoint = new DefaultAuthenticationEntryPoint();


    public WebfluxWebFilter(OAuthPatternProperties oAuthPatternProperties, StringRedis stringRedis) {
        this.oAuthPatternProperties = oAuthPatternProperties;
        this.stringRedis = stringRedis;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if(isOAuth(request.getPath())){
            //为第三方OAuth2.0所开放的
            return Mono.justOrEmpty(request.getQueryParams().getFirst(Utils.APP_ID))
                    .switchIfEmpty(Util.error(ErrorEnum.APP_ID_NOT_FOUND.getKey(), ErrorEnum.APP_ID_NOT_FOUND.getValue()))
                    .flatMap(s -> Mono.justOrEmpty(stringRedis.get(Util.CHANNEL_CACHE_NAMESPACE + s))
                            .switchIfEmpty(Util.error(ErrorEnum.APP_ID_ILLEGAL.getKey(), ErrorEnum.APP_ID_ILLEGAL.getValue()))
                            .flatMap(s1 -> {
                                Channel channel = JSON.parseObject(s1, Channel.class);
                                return channel.getIpWhiteList() == null || "0".equals(channel.getIpWhiteList()) ?
                                        chain.filter(exchange) :
                                        (Arrays.stream(channel.getIpWhiteList().split(",")).anyMatch(str -> str.equals(Util.getIpAddr(request))) ? chain.filter(exchange) : Mono.empty());
                            }))
                    .switchIfEmpty(Util.error(ErrorEnum.IP_ILLEGAL.getKey(), ErrorEnum.IP_ILLEGAL.getValue()))
                    .onErrorResume(ValidationException.class, e -> entryPoint.commence(exchange, e));

        }else{
            boolean excluded = false;
            if(oAuthPatternProperties.getExculdes() != null){
                for(String ex : oAuthPatternProperties.getExculdes()){
                    if(checkMatch(request.getPath(), ex)){
                        excluded = true;
                        break;
                    }
                }
            }
            if(excluded) return chain.filter(exchange);
            return matches(request.getPath())
                    .switchIfEmpty(Util.error(ErrorEnum.URL_INVALID.getKey(), ErrorEnum.URL_INVALID.getValue()))
                    .flatMap(aBoolean -> Mono.justOrEmpty(request.getHeaders().getFirst(Util.THANOS_SESSION))
                            .flatMap(s -> Mono.justOrEmpty(stringRedis.get(s)))
                            .flatMap(s -> Mono.justOrEmpty(request.getHeaders().getFirst(Util.NONCE))
                                    .flatMap(nonce -> {
                                        String key = s + ":" + Util.NONCE;
                                        if(stringRedis.isMember(key, nonce)){
                                            return Util.error(ErrorEnum.REQUEST_ILLEGAL.getKey(), ErrorEnum.REQUEST_ILLEGAL.getValue());
                                        }
                                        stringRedis.add(key, nonce);
                                        stringRedis.expire(key, Util.TIMESTAMP_EXPIRED);
                                        return chain.filter(exchange);
                                    })))
                    .onErrorResume(ValidationException.class, e -> entryPoint.commence(exchange, e));
        }
    }


    private boolean isOAuth(RequestPath requestPath){
        return checkMatch(requestPath, oAuthPatternProperties.getAuthPattern());
    }


    private boolean checkMatch(RequestPath requestPath, String pattern){
        if(pattern == null) return false;
        PathPattern pathPattern = pathPatternParser.parse(pattern);
        return pathPattern.matches(requestPath);
    }

    private Mono<Boolean> matches(RequestPath requestPath){
        if(oAuthPatternProperties.getPatterns() == null) return Mono.empty();
        boolean match = false;
        for (String pattern : oAuthPatternProperties.getPatterns()){
            PathPattern pathPattern = pathPatternParser.parse(pattern);
            match = pathPattern.matches(requestPath);
            if (match){
                break;
            }
        }
        return Mono.just(match);
    }

}
