package com.example.telegramBotTheory.repository;

import com.example.telegramBotTheory.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
