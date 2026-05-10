package com.mro.orchestrator.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.url:redis://127.0.0.1:6379}")
    private String redisUrl;

    @Bean
    public RedissonClient  redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisUrl)
                .setConnectTimeout(5000)      // Wait 5s to connect
                .setRetryAttempts(3)          // Retry 3 times
                .setRetryInterval(1500);      // Wait 1.5s between retries

        return Redisson.create(config);
    }
}
