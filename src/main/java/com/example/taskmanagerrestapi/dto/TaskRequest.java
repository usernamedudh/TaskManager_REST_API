package com.example.taskmanagerrestapi.dto;

import com.example.taskmanagerrestapi.entity.TaskPriority;
import com.example.taskmanagerrestapi.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
}
