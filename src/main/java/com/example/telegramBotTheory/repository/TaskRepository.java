package com.example.telegramBotTheory.repository;

import com.example.telegramBotTheory.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = "SELECT t from Task t WHERE t.question = :question")
    Optional<Task> findByQuestion(String question);
}
