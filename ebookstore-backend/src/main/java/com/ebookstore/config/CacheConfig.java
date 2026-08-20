package com.ebookstore.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
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
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CATEGORIES = "categories";
    public static final String BOOK_LIST = "bookList";
    public static final String BOOK_DETAIL = "bookDetail";
    public static final String ANNOUNCEMENTS = "announcements";

    /** 随机偏移上限(秒),避免同缓存内多条 key 同时过期(防缓存雪崩) */
    private static final long TTL_JITTER_SECONDS = 60;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer()));
                // 默认缓存 null(防缓存穿透:不存在的资源也缓存,避免反复打库)

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        // 每个缓存的基础 TTL + 随机偏移,错开过期时间
        configs.put(CATEGORIES, ttl(base, Duration.ofHours(1)));
        configs.put(BOOK_LIST, ttl(base, Duration.ofMinutes(30)));
        configs.put(BOOK_DETAIL, ttl(base, Duration.ofMinutes(30)));
        configs.put(ANNOUNCEMENTS, ttl(base, Duration.ofMinutes(30)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(configs)
                .build();
    }

    // 基础 TTL + 0~60s 随机偏移
    private RedisCacheConfiguration ttl(RedisCacheConfiguration base, Duration baseTtl) {
        long jitter = ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS);
        return base.entryTtl(baseTtl.plusSeconds(jitter));
    }

    // DTO 含 LocalDate/LocalDateTime,需注册 jsr310 模块并保留类型信息以便反序列化
    private GenericJackson2JsonRedisSerializer valueSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
