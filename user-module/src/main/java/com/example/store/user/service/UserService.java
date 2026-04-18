package com.example.store.user.service;

import com.example.store.user.domain.User;

import java.util.UUID;

public interface UserService {

    User validateUser(UUID userId);
}
