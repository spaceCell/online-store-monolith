package com.example.store.user.service.impl;

import com.example.store.user.domain.User;
import com.example.store.user.exception.UserNotFoundException;
import com.example.store.user.repository.UserRepository;
import com.example.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User validateUser(UUID userId) {
        return userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found or inactive: " + userId));
    }
}
