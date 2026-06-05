package in.bushansirgur.moneymanager.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisCacheConfig implements CachingConfigurer {
    public static final String DASHBOARD_CACHE = "dashboard";
    public static final String MONEY_PLAN_CACHE = "moneyPlan";
    public static final String CATEGORIES_CACHE = "categories";

    @Value("${app.cache.dashboard-ttl-minutes:5}")
    private long dashboardTtlMinutes;
    @Value("${app.cache.money-plan-ttl-minutes:5}")
    private long moneyPlanTtlMinutes;
    @Value("${app.cache.categories-ttl-minutes:30}")
    private long categoriesTtlMinutes;

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("java.util")
                        .allowIfSubType("java.math")
                        .allowIfSubType("java.time")
                        .allowIfSubType("in.bushansirgur.moneymanager")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(RedisCacheConfiguration defaultConfig) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(DASHBOARD_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(dashboardTtlMinutes)));
        cacheConfigurations.put(MONEY_PLAN_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(moneyPlanTtlMinutes)));
        cacheConfigurations.put(CATEGORIES_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(categoriesTtlMinutes)));
        return builder -> builder.withInitialCacheConfigurations(cacheConfigurations);
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> method.getName() + ":" + Arrays.deepToString(params);
    }
}
