package com.example.authenticationservice.util;

import com.example.authenticationservice.dto.request.RegisterRequestDTO;
import com.example.authenticationservice.dto.response.UserResponseDTO;
import com.example.authenticationservice.entity.Role;
import com.example.authenticationservice.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserUtils {

    private final PasswordEncoder passwordEncoder;

    public User toUser(RegisterRequestDTO request) {
        return new User(request.getName(), request.getEmail(), passwordEncoder.encode(request.getPassword()), Role.USER);
    }

    public UserResponseDTO toDTO(User user){
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}
