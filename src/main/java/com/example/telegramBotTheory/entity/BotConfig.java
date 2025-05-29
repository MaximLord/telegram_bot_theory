package com.example.telegramBotTheory.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling //Аннотация активации планировщика задач
@PropertySource("application.properties") //Аннотация, которая ссылает класс на файл
@Data //Аннотация, которая автоматически создает конструкторы
public class BotConfig {

    @Value("${bot.name}") //Аннотация, которая ссылает поле на объект
    String botName;

    @Value("${bot.key}")
    String token;

}

