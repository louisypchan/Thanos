package com.thanos.common.redis;

import org.springframework.data.redis.core.*;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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
public class StringRedis {

    private StringRedisTemplate stringRedisTemplate;

    private StringRedis(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static StringRedis _instance = null;

    public static StringRedis build(StringRedisTemplate stringRedisTemplate){
        Assert.notNull(stringRedisTemplate, "stringRedisTemplate must not be null");
        if(_instance == null){
            _instance = new StringRedis(stringRedisTemplate);
        }
        return _instance;
    }

    private ValueOperations<String, String> value(){
        return stringRedisTemplate.opsForValue();
    }

    private SetOperations<String, String> set(){
        return stringRedisTemplate.opsForSet();
    }

    public Long add(String key, String ...values){
        return set().add(key, values);
    }

    public Long remove(String key, String ...values){
        return set().remove(key, values);
    }

    public Set<String> members(String key){
        return set().members(key);
    }

    public boolean isMember(String key, String value){
        return set().isMember(key, value);
    }

    public String pop(String key){
        return set().pop(key);
    }

    public void set(String key, String value) {
        value().set(key, value);
    }

    public String get(String key){
        return value().get(key);
    }

    public void del(String key){
        stringRedisTemplate.delete(key);
    }

    public Boolean expire(String key, long timeout){
        return stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }
}
