package com.iprody.payment.service.app;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

/*
@SpringBootTest - это супер аннотация, отвечающая за настройку контекста
приложения Spring для запуска тестов. Она считывает тестовую конфигурацию, создаёт beans и т.д.

@Testcontainers нужна для того, чтобы тестовые контейнеры создавались автоматически
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractPostgresIntegrationTest {

    // В статическое поле POSTGRES помещается экземпляр класса-контейнера, в котором будет запущена БД Postgres.
    // Т.к. поле статическое -> экземпляр БД будет один на все тесты.
    @Container
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("payment-db")
            .withUsername("test")
            .withPassword("test")
            // необходимо подождать пока Postgres будет доступен
            .waitingFor(new WaitAllStrategy()
                    .withStrategy(Wait.forListeningPort())
                    .withStrategy(Wait.forLogMessage(".*database system is ready to accept connections.*", 2)))
            .withStartupTimeout(Duration.ofSeconds(200));

    // Метод overrideProps() с аннотацией @DynamicPropertySource используется для
    // передачи в созданный для запуска тестов контекст Spring параметров подключения к
    // БД, запущенной в тестовом контейнере. Здесь же мы указываем ссылку на тестовый master log Liquibase.
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:/db.changelog/master-test-changelog.yaml");
    }
}
