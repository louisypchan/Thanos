package com.thanos.common.lock.impl;

import com.thanos.common.lock.ILock;
import com.thanos.common.lock.ILockCallBack;
import com.thanos.common.lock.ILockManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

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
public class LuaLockRedisLockManager implements ILockManager {

    private final StringRedisTemplate redisTemplate;

    private final ILock distributeLock; // 分布锁

    public LuaLockRedisLockManager(StringRedisTemplate redisTemplate, ILock distributeLock) {
        this.redisTemplate = redisTemplate;
        this.distributeLock = distributeLock;
    }

    @Override
    public <T> T runLock(String lockKey, ILockCallBack<T> callBack) {
        Assert.notNull("lockKey","lockKey must not be null");
        Assert.notNull("callBack","callBack must not be null");
        try{
            distributeLock.lock(lockKey);
            return callBack.execute();
        }finally {
            //
            distributeLock.unlock(lockKey);
        }
    }

    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public ILock getDistributeLock() {
        return distributeLock;
    }
}
