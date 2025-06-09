package com.example.telegramBotTheory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories("com.example.telegramBotTheory.repository") // Пакет репозитория
@EntityScan("com.example.telegramBotTheory.entity") // Пакет сущности
public class SpringProgramApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringProgramApplication.class, args);
    }

}




