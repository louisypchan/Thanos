package com.thanos.entry.config;


import com.louis.ice.server.anno.EnableIceBox;
import com.louis.ice.server.config.InjectionBeans;
import com.thanos.common.http.anno.EnableHttpClient;
import com.thanos.common.redis.StringRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;

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
@Configuration
@EnableHttpClient
@EnableIceBox
public class EntryConfiguration {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Bean
    public InjectionBeans injectionBeans(StringRedisTemplate stringRedisTemplate){
        InjectionBeans injectionBeans = new InjectionBeans();
        StringRedis stringRedis = StringRedis.build(stringRedisTemplate);
        injectionBeans.injects = new HashMap(){{
            put("stringRedis", stringRedis);
        }};
        return injectionBeans;
    }
}
