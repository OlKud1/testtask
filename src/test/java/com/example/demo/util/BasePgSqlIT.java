package com.example.demo.util;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface BasePgSqlIT {

    @Container
    PostgreSQLContainer<?> PG_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void setPgSqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PG_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", PG_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", PG_SQL_CONTAINER::getPassword);
    }
}
