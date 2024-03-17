package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.request.ConfirmRequestDTO;
import com.example.authenticationservice.dto.request.LoginRequestDTO;
import com.example.authenticationservice.dto.request.RegisterRequestDTO;
import com.example.authenticationservice.entity.User;
import com.example.authenticationservice.exception.ConfirmEmailException;
import com.example.authenticationservice.exception.ConstraintViolationException;
import com.example.authenticationservice.jwt.JwtService;
import com.example.authenticationservice.repository.UserRepository;
import com.example.authenticationservice.util.UserUtils;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.example.authenticationservice.config.KafkaTopicConfig.REQUEST_TOPIC_NAME;

@Service
@AllArgsConstructor
public class AuthenticationService {
    public static final String EMAIL_EXIST = "User with this email already exist.";
    public static final String NOT_CONFIRM = "Wrong email or confirmation code.";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserUtils userUtils;
    private final UserService userService;
    private final CheckEmailService checkEmailService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public User register(RegisterRequestDTO request) {
        User user;
        try{
            user = userRepository.save(userUtils.toUser(request));
        }
        catch (Exception e) {
            if (e.getMessage().contains("users_unique_email_idx")) throw new ConstraintViolationException(EMAIL_EXIST);
            throw new RuntimeException(e);
        }
        sendMessage(user.getEmail());
        return user;
    }

    public String login(LoginRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        return jwtService.generateToken(userService.getByEmail(request.getEmail()));
    }

    public void sendMessage(String email) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(REQUEST_TOPIC_NAME, email);

        future.whenComplete((result, error) -> {
            if (error != null) System.err.println();
            System.out.println(result);
        });
    }

    public String confirmEmail(ConfirmRequestDTO request) {
        if (checkEmailService.confirmEmail(request.getEmail(), request.getCode()))
            return jwtService.generateToken(userService.getByEmail(request.getEmail()));
        throw new ConfirmEmailException(NOT_CONFIRM);
    }
}
