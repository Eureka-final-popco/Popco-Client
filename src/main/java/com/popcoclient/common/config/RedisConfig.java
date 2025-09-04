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

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(eventRedisConnectionFactory()); // redisConnectionFactory

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

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

        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);

        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setBlockWhenExhausted(true);

        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        poolConfig.setMinEvictableIdleTimeMillis(60000);
        poolConfig.setNumTestsPerEvictionRun(3);

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

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public MessageListenerAdapter notificationRedisMessageListenerAdapter() {
        return new MessageListenerAdapter(notificationSubscriber);
    }

    @Bean("notificationRedisMessageListenerContainer")
    public RedisMessageListenerContainer notificationRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter notificationRedisMessageListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(notificationRedisMessageListenerAdapter, new ChannelTopic("notifications"));
        container.setTaskExecutor(Executors.newFixedThreadPool(10));

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
                .entryTtl(Duration.ofMinutes(30))
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
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
                .prefixCacheNameWith("quiz:")
                .disableCachingNullValues();
    }
}