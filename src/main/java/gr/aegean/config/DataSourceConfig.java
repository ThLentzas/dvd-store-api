package gr.aegean.config;

import com.zaxxer.hikari.HikariDataSource;

import gr.aegean.model.dvd.Dvd;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;

import redis.clients.jedis.JedisPoolConfig;


/**
 * Configuration class for creating and configuring the Redis cache and database connection pool.
 */
@Setter
@EnableCaching
@RequiredArgsConstructor
@Configuration
public class DataSourceConfig {
    private final RedisProperties redisProperties;

    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration cacheConfiguration = redisCacheConfiguration();
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(jedisConnectionFactory())
                .cacheDefaults(cacheConfiguration)
                .transactionAware();

        return builder.build();
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.
                        fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxTotal(10);
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMinIdle(1);

        return jedisPoolConfig;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();

        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());

        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder()
                .usePooling()
                .poolConfig(jedisPoolConfig())
                .build();

        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        Jackson2JsonRedisSerializer<Dvd> dvdSerializer = new Jackson2JsonRedisSerializer<>(Dvd.class);

        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(dvdSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(dvdSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(HikariDataSource hikariDataSource) {
        return new JdbcTemplate(hikariDataSource);
    }
}