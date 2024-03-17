package com.example.authenticationservice.controller;

import com.example.authenticationservice.dto.request.ConfirmRequestDTO;
import com.example.authenticationservice.dto.request.LoginRequestDTO;
import com.example.authenticationservice.dto.request.RegisterRequestDTO;
import com.example.authenticationservice.dto.response.MessageResponseDTO;
import com.example.authenticationservice.dto.response.TokenResponseDTO;
import com.example.authenticationservice.service.AuthenticationService;
import com.example.authenticationservice.util.UserUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserUtils userUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(userUtils.toDTO(authenticationService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(new TokenResponseDTO(authenticationService.login(request)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<TokenResponseDTO> confirmEmail(@RequestBody ConfirmRequestDTO request) {
        return ResponseEntity.ok(new TokenResponseDTO(authenticationService.confirmEmail(request)));
    }

    @GetMapping("/test")
    public ResponseEntity<MessageResponseDTO> test(){
        return ResponseEntity.ok(new MessageResponseDTO("Ok"));
    }
}
