package com.shiptrack.shiptrack_pro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    // Cache name constants used by @Cacheable annotations across the app.
    public static final String CUSTOMER_ANALYTICS_CACHE = "customerAnalytics";
    public static final String BUSINESS_ANALYTICS_CACHE = "businessAnalytics";
    public static final String ADMIN_ANALYTICS_CACHE = "adminAnalytics";

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return mapper;
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));

        // Per-endpoint expiration: customer/business data changes more often
        // than platform-wide admin aggregates, so admin can cache a bit longer.
        Map<String, RedisCacheConfiguration> perCacheConfig = new HashMap<>();
        perCacheConfig.put(CUSTOMER_ANALYTICS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(2)));
        perCacheConfig.put(BUSINESS_ANALYTICS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(2)));
        perCacheConfig.put(ADMIN_ANALYTICS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }

    // Cache key = the caller's email (or "platform" for the admin-wide view),
    // so each customer/business only ever hits their own cached entry.
    @Bean
    public KeyGenerator analyticsKeyGenerator() {
        return (target, method, params) -> {
            if (params.length == 0) return "platform";
            return params[0].toString();
        };
    }
}
