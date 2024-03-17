package com.example.authenticationservice.jwt;

import com.example.authenticationservice.entity.User;
import com.example.authenticationservice.exception.EmailNotFoundException;
import com.example.authenticationservice.exception.TokenException;
import com.example.authenticationservice.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.example.authenticationservice.service.UserService.EMAIL_NOT_FOUND;

@Service
public class JwtService {

    @Value("${authenticationservice.duration}")
    private int duration;
    private final SecretKey secretKey;

    private final UserRepository userRepository;

    public JwtService(@Value("${authenticationservice.secret}") String secret, UserRepository userRepository) {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.userRepository = userRepository;
    }

    public String generateToken(User user){
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(duration, ChronoUnit.DAYS)))
                .signWith(secretKey)
                .compact();
    }

    public User validateAndGetUser(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return userRepository.findByEmail(claims.getSubject()).orElseThrow(() -> new EmailNotFoundException(EMAIL_NOT_FOUND));
        } catch (Exception e) {
            throw new TokenException(e.getMessage());
        }
    }
}
