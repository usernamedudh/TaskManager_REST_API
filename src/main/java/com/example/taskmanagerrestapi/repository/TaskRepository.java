package com.example.taskmanagerrestapi.repository;

import com.example.taskmanagerrestapi.entity.Task;
import com.example.taskmanagerrestapi.entity.TaskStatus;
import com.example.taskmanagerrestapi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByUser(User user, Pageable pageable);
    Page<Task> findByUserAndStatus(User user, TaskStatus status, Pageable pageable);
    Optional<Task> findByIdAndUser(Long id, User user);
    long countByUserAndStatus(User user, TaskStatus status);
}
