package com.example.authenticationservice.service;

import com.example.authenticationservice.entity.User;
import com.example.authenticationservice.exception.EmailNotFoundException;
import com.example.authenticationservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    public static final String EMAIL_NOT_FOUND = "User with this email not found.";

    private final UserRepository userRepository;

    public User getByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new EmailNotFoundException(EMAIL_NOT_FOUND));
    }
}
