package gr.aegean.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis.jedis.pool")
public class JedisProperties {
    private Integer maxActive;
    private Integer maxIdle;
    private Integer minIdle;
}
