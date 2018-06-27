package com.thanos.socket.config;

import com.thanos.common.Utils;
import com.thanos.common.lock.ILock;
import com.thanos.common.lock.ILockManager;
import com.thanos.common.lock.impl.LuaDistributeLock;
import com.thanos.common.lock.impl.LuaLockRedisLockManager;
import com.thanos.notification.socket.SocketNotification;
import com.thanos.socket.handler.PresenceChannelInterceptor;
import com.thanos.socket.handler.ReGenStompSessionHandler;
import com.thanos.socket.impl.SocketSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

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
@EnableWebSocketMessageBroker
public class SocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ServerProperties serverProperties;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(Utils.ENDPOINT_NAME).setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(Utils.BROADCAST_NAME);
        registry.setApplicationDestinationPrefixes(Utils.M);
    }

    @ConditionalOnMissingBean
    @Bean
    @Scope("singleton")
    public ILock distributeLock(StringRedisTemplate stringRedisTemplate){
        return new LuaDistributeLock(stringRedisTemplate, "THANOS_SOCKET_", 5);
    }

    @ConditionalOnMissingBean
    @Bean
    @Scope("singleton")
    public ILockManager lockManager(ILock distributeLock, StringRedisTemplate stringRedisTemplate){
        ILockManager lockManager =  new LuaLockRedisLockManager(stringRedisTemplate, distributeLock);
        return lockManager;
    }

    @Bean
    @Scope("singleton")
    SocketNotification<ReGenStompSessionHandler> socketNotification(){
        SocketNotification<ReGenStompSessionHandler> socketNotification = new SocketNotification();
        return socketNotification;
    }

    @Bean
    RedisMessageListenerContainer keyExpirationListenerContainer(StringRedisTemplate stringRedisTemplate
    , SocketNotification<ReGenStompSessionHandler> socketNotification){
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(stringRedisTemplate.getConnectionFactory());
        listenerContainer.addMessageListener((message, pattern) -> {
            ReGenStompSessionHandler reGenStompSessionHandler = new ReGenStompSessionHandler(socketNotification.getLatch(),
                    message.toString(), message.toString());
            socketNotification.setHandler(reGenStompSessionHandler);
            socketNotification.execute("ws://127.0.0.1:" + serverProperties.getPort() + "/thanos");
        }, Utils.KEYEVENT_EXPIRED_TOPIC);
        return listenerContainer;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(presenceChannelInterceptor());
    }

    @Bean
    @ConditionalOnMissingBean
    public SocketSessionRegistry socketSessionRegistry(StringRedisTemplate stringRedisTemplate, ILockManager lockManager){
        return new SocketSessionRegistry(stringRedisTemplate, lockManager);
    }

    @Bean
    public PresenceChannelInterceptor presenceChannelInterceptor(){
        return new PresenceChannelInterceptor();
    }

}
