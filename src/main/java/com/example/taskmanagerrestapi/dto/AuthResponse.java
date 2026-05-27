package com.example.taskmanagerrestapi.dto;

public record AuthResponse(
        String token,
        String username,
        String role
) {}
