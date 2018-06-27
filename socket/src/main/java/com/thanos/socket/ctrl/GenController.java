package com.thanos.socket.ctrl;

import com.thanos.common.Utils;
import com.thanos.common.lock.ILockManager;
import com.thanos.common.redis.StringRedis;
import com.thanos.socket.handler.PresenceChannelInterceptor;
import com.thanos.socket.impl.SocketSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
@Controller
public class GenController {

    private final Logger logger = LoggerFactory.getLogger(GenController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SocketSessionRegistry socketSessionRegistry;

    @Autowired
    private ILockManager lockManager;


    @MessageMapping("/init")
    public void qrGenerate(String ds){
        Assert.notNull(ds, "ds must not be null");
        StringRedis stringRedis = StringRedis.build(stringRedisTemplate);
        lockManager.runLock(ds, () -> {
            Set<String> sets = socketSessionRegistry.getSessionIds(ds);
            sets.stream().collect(Collectors.toList())
                    .forEach(sid -> {
                        logger.info("try to dispatch session is " + sid);
                        socketSessionRegistry.unregisterSessionId(ds, sid);
                        messagingTemplate.convertAndSendToUser(sid, "/topic/qr", generateTaskId(sid),
                                createHeaders(sid));
                        // cache sid at init qr
                        stringRedis.set(sid, sid);
                    });
            return "DONE";
        });
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    @MessageMapping("/reGenerate/{taskId}")
    public void reGenerate(@DestinationVariable String taskId, String message){
        StringRedis stringRedis = StringRedis.build(stringRedisTemplate);
        String sid = stringRedis.get(taskId + Utils.SESSIONID);
        if(!StringUtils.isEmpty(stringRedis.get(sid))){
            messagingTemplate.convertAndSendToUser(sid, "/topic/qr", generateTaskId(sid),
                    createHeaders(sid));
        }
        stringRedis.del(taskId + Utils.SESSIONID);
    }

    private String generateTaskId(String sessionId){
        Assert.notNull(sessionId, "sessionId must not be null");
        String str = Utils.rand(Utils.GENERATE_ID_LENGTH, false);
        DefaultRedisScript<Long> qrScript = new DefaultRedisScript();
        qrScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/qr.lua")));
        qrScript.setResultType(Long.class);
        List<String> keyList = new ArrayList();
        keyList.add(Utils.QRCODE);
        keyList.add(Utils.SESSIONID);
        Long result = stringRedisTemplate.execute(qrScript, keyList, str, Utils.QRCODE_EXPIRED, sessionId);
        while(result == 0){
            str = Utils.rand(Utils.GENERATE_ID_LENGTH, false);
            result = stringRedisTemplate.execute(qrScript, keyList, str, Utils.QRCODE_EXPIRED, sessionId);
        }
        return str;
    }
}
