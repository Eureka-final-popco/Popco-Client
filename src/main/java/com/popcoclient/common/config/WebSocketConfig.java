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
     * 🔧 메시지 브로커 설정
     * 
     * STOMP 프로토콜을 사용하여 클라이언트와 서버 간 실시간 통신 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        
        // 📡 Simple Broker 활성화 (메모리 기반)
        config.enableSimpleBroker(
                "/topic",    // 브로드캐스트용 (1:N)
                "/queue"     // 개인 메시지용 (1:1)
        );
        
        // 📤 클라이언트에서 서버로 메시지 보낼 때 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // 👤 개인 메시지 prefix 설정
        config.setUserDestinationPrefix("/user");
        
        log.info("WebSocket Message Broker configured - topic: /topic, queue: /queue, app: /app");
    }

    /**
     * 🔌 WebSocket 엔드포인트 등록
     * 
     * 클라이언트가 WebSocket 연결을 맺을 수 있는 엔드포인트 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        
        // 🌐 WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws-quiz")
                .setAllowedOriginPatterns("*")  // CORS 설정 (개발용, 운영에서는 구체적으로 설정)
                .withSockJS();                  // SockJS 폴백 지원 (WebSocket 미지원 브라우저 대응)
        
        log.info("WebSocket endpoints registered - /ws-quiz");
    }

    /**
     * 📨 메시지 컨버터 설정 (JSON 직렬화)
     */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        
        // JSON 메시지 컨버터 등록
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        converter.setContentTypeResolver(resolver);
        
        messageConverters.add(converter);
        
        log.info("JSON message converter configured");
        return false; // 기본 컨버터도 유지
    }

    /**
     * 🔐 인바운드 채널 설정 (보안 및 인터셉터)
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        
        // 🧵 스레드 풀 설정 (동시성 처리)
        registration.taskExecutor()
                .corePoolSize(10)           // 기본 스레드 수
                .maxPoolSize(50)            // 최대 스레드 수  
                .queueCapacity(1000)        // 대기열 크기
                .keepAliveSeconds(60);      // 유휴 스레드 유지 시간
        
        // 🔍 메시지 인터셉터 등록 (인증, 로깅 등)
        registration.interceptors(quizWebSocketInterceptor);
        
        log.info("Client inbound channel configured - poolSize: 10-50, queue: 1000");
    }

    /**
     * 📤 아웃바운드 채널 설정
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        
        // 📡 브로드캐스트용 스레드 풀 설정  
        registration.taskExecutor()
                .corePoolSize(10)
                .maxPoolSize(50)
                .queueCapacity(1000)
                .keepAliveSeconds(60);
        
        log.info("Client outbound channel configured");
    }

    /**
     * 🔧 WebSocket 전송 설정
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        
        // 📊 메시지 크기 제한
        registration.setMessageSizeLimit(64 * 1024);      // 64KB
        registration.setSendBufferSizeLimit(512 * 1024);   // 512KB
        registration.setSendTimeLimit(20 * 1000);          // 20초
        
        log.info("WebSocket transport configured - messageSize: 64KB, bufferSize: 512KB");
    }
}