package com.popcoclient.common.config;

import com.popcoclient.event.service.impl.NotificationSubscriber;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.Executors;

@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.timeout:2000}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle:0}")
    private int minIdle;

    @Value("${spring.redis.jedis.pool.max-wait:-1}")
    private long maxWait;

    private final NotificationSubscriber notificationSubscriber;

//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
//        config.setPassword(redisPassword);
//        return new LettuceConnectionFactory(config);
//    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(eventRedisConnectionFactory()); // redisConnectionFactory

        // 일반적인 key:value의 경우 시리얼라이저
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // Hash를 사용할 경우 시리얼라이저
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        // 모든 경우
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

    @Bean("eventRedisFactory")
    public LettuceConnectionFactory eventRedisConnectionFactory() {

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        redisConfig.setDatabase(database);

        if (!redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();

        // 풀 크기 설정
        poolConfig.setMaxTotal(maxActive);           // 최대 연결 수
        poolConfig.setMaxIdle(maxIdle);              // 최대 유휴 연결 수
        poolConfig.setMinIdle(minIdle);              // 최소 유휴 연결 수

        // 대기 및 타임아웃 설정
        poolConfig.setMaxWaitMillis(maxWait);        // 연결 대기 최대 시간
        poolConfig.setBlockWhenExhausted(true);      // 풀이 고갈되면 대기

        // 연결 검증 설정 (성능 vs 안정성 트레이드오프)
        poolConfig.setTestOnBorrow(true);            // 빌릴 때 검증
        poolConfig.setTestOnReturn(true);            // 반환할 때 검증
        poolConfig.setTestWhileIdle(true);           // 유휴 상태에서 주기적 검증

        // 유휴 연결 관리
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);  // 30초마다 유휴 연결 정리
        poolConfig.setMinEvictableIdleTimeMillis(60000);     // 60초 유휴 시 제거 대상
        poolConfig.setNumTestsPerEvictionRun(3);             // 한 번에 검사할 연결 수

        // JMX 모니터링 비활성화 (선택적)
        poolConfig.setJmxEnabled(false);

        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeout))
                .shutdownTimeout(Duration.ofMillis(100))
                .build();
        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    /**
     * 퀴즈 이벤트 전용 RedisTemplate (성능 최적화)
     */
    @Bean("eventRedisTemplate")
    public RedisTemplate<String, String> eventRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(eventRedisConnectionFactory());

        // String 직렬화 (성능 최적화)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis Pub/Sub용 MessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(eventRedisConnectionFactory());

        // 스레드 풀 설정 (동시성 처리를 위해)
        container.setTaskExecutor(Executors.newFixedThreadPool(10));

        return container;
    }

    @Bean
    public MessageListenerAdapter notificationRedisMessageListenerAdapter() {
        return new MessageListenerAdapter(notificationSubscriber);
    }

    @Bean("notificationRedisMessageListenerContainer")
    public RedisMessageListenerContainer notificationRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter notificationRedisMessageListenerContainer
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(notificationRedisMessageListenerContainer, new ChannelTopic("notifications"));
        return container;
    }

    /**
     * 분산 락을 위한 RedisTemplate
     */
    @Bean("lockRedisTemplate")
    public StringRedisTemplate lockRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(eventRedisConnectionFactory());
        return template;
    }

    /**
     * Redis 캐시 매니저 설정
     */
    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 기본 TTL 30분
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
                .disableCachingNullValues();

        return RedisCacheManager.builder(eventRedisConnectionFactory())
                .cacheDefaults(config)
                .build();
    }

    /**
     * 퀴즈 이벤트 관련 캐시 설정
     */
    @Bean
    public RedisCacheConfiguration quizCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2)) // 퀴즈 이벤트는 2시간 캐시
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
                .prefixCacheNameWith("quiz:")
                .disableCachingNullValues();
    }
}