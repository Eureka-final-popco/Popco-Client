package com.popcoclient.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popcoclient.common.interceptor.QuizWebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final QuizWebSocketInterceptor quizWebSocketInterceptor;

    /**
     * STOMP 프로토콜 사용
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        config.enableSimpleBroker(
                "/topic",
                "/queue"
        );

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 클라이언트가 WebSocket 연결 엔드포인트 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-quiz")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 메시지 컨버터 설정
     */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {

        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        converter.setContentTypeResolver(resolver);
        
        messageConverters.add(converter);
        return false;
    }

    /**
     * 인바운드 채널 설정
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.taskExecutor()
                .corePoolSize(10)
                .maxPoolSize(50)
                .queueCapacity(1000)
                .keepAliveSeconds(60);

        registration.interceptors(quizWebSocketInterceptor);
    }

    /**
     * 아웃바운드 채널 설정
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {

        registration.taskExecutor()
                .corePoolSize(10)
                .maxPoolSize(50)
                .queueCapacity(1000)
                .keepAliveSeconds(60);
    }

    /**
     * WebSocket 전송 설정
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {

        registration.setMessageSizeLimit(64 * 1024);
        registration.setSendBufferSizeLimit(512 * 1024);
        registration.setSendTimeLimit(20 * 1000);
    }
}