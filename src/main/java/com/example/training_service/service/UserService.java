package com.example.training_service.service;

import com.example.training_service.dto.UserDTO;
import com.example.training_service.event.UserCreatedEvent;
import com.example.training_service.exception.EntityNotFoundException;
import com.example.training_service.model.User;
import com.example.training_service.outbox.OutboxEvent;
import com.example.training_service.outbox.OutboxEventRepository;
import com.example.training_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public UserService(UserRepository userRepository,
                       OutboxEventRepository outboxEventRepository,
                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserDTO createUser(UserDTO dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .active(true)
                .build();
        User saved = userRepository.save(user);

        publishUserCreated(saved);

        return toDTO(saved);
    }

    private void publishUserCreated(User user) {
        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .userId(user.getId().toString())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .occurredAt(System.currentTimeMillis())
                    .build();

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setTopic("user.created");
            outboxEvent.setKey(user.getId().toString());
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize UserCreatedEvent", e);
        }
    }

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserDTO getById(UUID id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDTO);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public UserDTO update(UUID id, UserDTO dto) {
        User user = findOrThrow(id);
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setRole(dto.getRole());
        return toDTO(userRepository.save(user));
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void softDelete(UUID id) {
        User user = findOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void hardDelete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .active(user.isActive())
                .build();
    }
}