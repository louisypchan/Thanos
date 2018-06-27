package com.thanos.common.lock.impl;

import com.thanos.common.lock.ILock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
public class LuaDistributeLock implements ILock {

    // 单位s，一个线程持有锁的最大时间
    private static final int LOCK_MAX_EXIST_TIME = 5;

    // 作为锁的key的前缀
    private static final String LOCK_PREX = "lock_";

    private StringRedisTemplate redisTemplate;

    // 做为锁key的前缀
    private String lockPrex;

    // 单位s，一个线程持有锁的最大时间
    private int lockMaxExistTime;

    //lock script
    private DefaultRedisScript<Long> lockScript;

    //unlock script
    private DefaultRedisScript<Long> unlockScript;

    //thread var
    private ThreadLocal<String> threadKeyId = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());


    public LuaDistributeLock(StringRedisTemplate redisTemplate) {
        this(redisTemplate, LOCK_PREX, LOCK_MAX_EXIST_TIME);
    }

    public LuaDistributeLock(StringRedisTemplate redisTemplate, String lockPrex, int lockMaxExistTime) {
        this.redisTemplate = redisTemplate;
        this.lockPrex = lockPrex;
        this.lockMaxExistTime = lockMaxExistTime;
        this.init();
    }

    private void init(){
        //lock script
        lockScript = new DefaultRedisScript();
        lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/lock.lua")));
        lockScript.setResultType(Long.class);
        //unlock script
        unlockScript = new DefaultRedisScript();
        unlockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/unlock.lua")));
        unlockScript.setResultType(Long.class);
    }

    @Override
    public void lock(String lock) {
        Assert.notNull(lock, "lock must not be null");
        String lockKey = getLockKey(lock);
        while (true){
            List<String> keyList = new ArrayList();
            keyList.add(lockKey);
            keyList.add(threadKeyId.get());
            if(redisTemplate.execute(lockScript, keyList, String.valueOf(lockMaxExistTime * 1000)) > 0){
                break;
            }else{
                try {
                    // 短暂休眠，nano避免出现活锁
                    Thread.sleep(10, (int)(Math.random() * 500));
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    @Override
    public void unlock(String lock) {
        String lockKey = getLockKey(lock);
        List<String> keyList = new ArrayList();
        keyList.add(lockKey);
        keyList.add(threadKeyId.get());
        redisTemplate.execute(unlockScript, keyList);
    }


    /**
     * generate lock key
     * @param lock
     * @return
     */
    private String getLockKey(String lock){
        StringBuilder sb = new StringBuilder();
        sb.append(lockPrex).append(lock);
        return sb.toString();
    }
}
