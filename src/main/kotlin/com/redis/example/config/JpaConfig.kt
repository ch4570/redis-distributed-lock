package com.redis.example.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@Configuration(proxyBeanMethods = false)
class JpaConfig {
}