package com.finalproject.hotelbooking.service;

import com.finalproject.hotelbooking.model.AppUser;
import com.finalproject.hotelbooking.model.Role;
import com.finalproject.hotelbooking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(AppUser user) {
        String normalizedEmail = user.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public AppUser getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User was not found."));
    }
}
