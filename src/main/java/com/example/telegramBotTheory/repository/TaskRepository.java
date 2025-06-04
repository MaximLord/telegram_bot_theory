package com.example.telegramBotTheory.repository;

import com.example.telegramBotTheory.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT DISTINCT t.topic FROM Task t")
    List<String> findUniqueTopics();

    List<Task> findByTopic(String topic);


}
