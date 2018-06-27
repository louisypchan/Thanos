package com.thanos.socket.impl;

import com.thanos.common.lock.ILockManager;
import com.thanos.common.redis.StringRedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

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
public class SocketSessionRegistry{

    private final Logger logger = LoggerFactory.getLogger(SocketSessionRegistry.class);

    private StringRedis stringRedis;

    private ILockManager lockManager;

    public SocketSessionRegistry(StringRedisTemplate stringRedisTemplate, ILockManager lockManager) {
        stringRedis = StringRedis.build(stringRedisTemplate);
        this.lockManager = lockManager;
    }

    public Set<String> getSessionIds(String user) {
        return stringRedis.members(user);
    }

    public void registerSessionId(String user, String sessionId) {
        logger.debug("registerSessionId", user, sessionId);
        Assert.notNull(user, "User must not be null");
        Assert.notNull(sessionId, "Session ID must not be null");
        lockManager.runLock(user, () -> stringRedis.add(user, sessionId));

    }


    public void unregisterSessionId(String user, String sessionId) {
        logger.debug("unregisterSessionId", user, sessionId);
        Assert.notNull(user, "User Name must not be null");
        Assert.notNull(sessionId, "Session ID must not be null");
        stringRedis.remove(user, sessionId);
    }
}
