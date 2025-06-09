package com.example.telegramBotTheory.repository;

import com.example.telegramBotTheory.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
//
//    @Query("SELECT DISTINCT t.topic FROM Task t")
//    List<String> findUniqueTopics();
//
//    List<Task> findByTopic(String topic);


    @Query("SELECT DISTINCT t.topic FROM Task t")
        // Использует имя поля из сущности
    List<String> findUniqueTopics();


    @Query(value = "SELECT t from Task t WHERE t.topic = :topic")
    List<Task> findByTopic(String topic);


//    @Query(value = "SELECT t from Task t WHERE t.question = :question")
//    Optional<Task> findByQuestion(String question);

}
