package com.example.taskmanagerrestapi.controller;

import com.example.taskmanagerrestapi.dto.TaskRequest;
import com.example.taskmanagerrestapi.dto.TaskResponse;
import com.example.taskmanagerrestapi.entity.TaskStatus;
import com.example.taskmanagerrestapi.entity.User;
import com.example.taskmanagerrestapi.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tasks", description = "Task CRUD operations")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Paginated list, optionally filtered by status")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getTasks(user, status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getTask(id, user));
    }

    @PostMapping
    @Operation(summary = "Create task")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request, user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.updateTask(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        taskService.deleteTask(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get task statistics", description = "Count of tasks per status")
    public ResponseEntity<Map<String, Long>> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getStats(user));
    }
}
