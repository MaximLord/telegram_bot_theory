package com.example.telegramBotTheory.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

@Entity(name = "task")
// Задача
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "topic")
    private String topic;       // Тема
    @Column(name = "question")
    private String question;    // Вопрос
    @Column(name = "answer")
    private String answer;      // Ответ

}
