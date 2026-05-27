package com.example.taskmanagerrestapi.service;

import com.example.taskmanagerrestapi.dto.AuthRequest;
import com.example.taskmanagerrestapi.dto.AuthResponse;
import com.example.taskmanagerrestapi.dto.RegisterRequest;
import com.example.taskmanagerrestapi.entity.Role;
import com.example.taskmanagerrestapi.entity.User;
import com.example.taskmanagerrestapi.repository.UserRepository;
import com.example.taskmanagerrestapi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(user), user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.username()));
        return new AuthResponse(jwtUtil.generateToken(user), user.getUsername(), user.getRole().name());
    }
}
