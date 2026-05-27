package com.example.taskmanagerrestapi.dto;

import com.example.taskmanagerrestapi.entity.Task;
import com.example.taskmanagerrestapi.entity.TaskPriority;
import com.example.taskmanagerrestapi.entity.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String username
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getUser().getUsername()
        );
    }
}
