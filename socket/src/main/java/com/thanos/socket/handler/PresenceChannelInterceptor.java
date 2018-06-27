package com.thanos.socket.handler;

import com.thanos.common.Utils;
import com.thanos.common.redis.StringRedis;
import com.thanos.socket.impl.SocketSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

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
@Component
public class PresenceChannelInterceptor extends ChannelInterceptorAdapter {

    private final Logger logger = LoggerFactory.getLogger(PresenceChannelInterceptor.class);

    @Autowired
    private SocketSessionRegistry socketSessionRegistry;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);
        switch(sha.getCommand()) {
            case CONNECT:
                logger.debug("Socket connect");
                connect(sha);
                break;
            case CONNECTED:
                break;
            case DISCONNECT:
                logger.debug("DISCONNECT");
                disConnect(sha);
                break;
            default:
                break;
        }
        super.postSend(message, channel, sent);
    }

    //connected
    private void connect(StompHeaderAccessor sha){
        String sd = sha.getLogin();
        if(sd != null){
            String sessionId = sha.getSessionId();
            logger.info("the user log in is " + sd);
            logger.info("session is " + sessionId);
            sha.getSessionAttributes().put("login", sd);
            socketSessionRegistry.registerSessionId(sd, sessionId);
        }
    }


    //disconnect
    private void disConnect(StompHeaderAccessor sha){
        StringRedis stringRedis = StringRedis.build(stringRedisTemplate);
        String sessionId = sha.getSessionId();
        socketSessionRegistry.unregisterSessionId((String) sha.getSessionAttributes().get("login"), sessionId);
        //TODO: 移除循环
        stringRedis.del(sessionId);
        logger.info("remove sessionId " + sessionId);
    }
}
