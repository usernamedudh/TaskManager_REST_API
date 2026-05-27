package com.example.taskmanagerrestapi.service;

import com.example.taskmanagerrestapi.dto.TaskRequest;
import com.example.taskmanagerrestapi.dto.TaskResponse;
import com.example.taskmanagerrestapi.entity.Task;
import com.example.taskmanagerrestapi.entity.TaskStatus;
import com.example.taskmanagerrestapi.entity.User;
import com.example.taskmanagerrestapi.exception.ResourceNotFoundException;
import com.example.taskmanagerrestapi.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    public Page<TaskResponse> getTasks(User user, TaskStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> tasks = (status != null)
                ? taskRepository.findByUserAndStatus(user, status, pageable)
                : taskRepository.findByUser(user, pageable);
        return tasks.map(TaskResponse::from);
    }

    public TaskResponse getTask(Long id, User user) {
        Task task = findByIdAndUser(id, user);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, User user) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .user(user)
                .build();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, User user) {
        Task task = findByIdAndUser(id, user);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id, User user) {
        Task task = findByIdAndUser(id, user);
        taskRepository.delete(task);
    }

    public Map<String, Long> getStats(User user) {
        return Map.of(
                "todo",       taskRepository.countByUserAndStatus(user, TaskStatus.TODO),
                "inProgress", taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS),
                "done",       taskRepository.countByUserAndStatus(user, TaskStatus.DONE)
        );
    }

    // ---- helpers ----

    private Task findByIdAndUser(Long id, User user) {
        return taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }
}
