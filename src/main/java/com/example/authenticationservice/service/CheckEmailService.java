package com.example.authenticationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CheckEmailService {
    public static final String RESPONSE_TOPIC_NAME = "response-topic";

    private final Map<String, String> confirmationCodes = new HashMap<>();

    @KafkaListener(topics = RESPONSE_TOPIC_NAME)
    public void listener(String email) {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> map;
        try {
            map = mapper.readValue(email, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        confirmationCodes.putAll(map);
    }

    public boolean confirmEmail(String email, String confirmationCode){
        String code = confirmationCodes.get(email);
        if (code != null && code.equals(confirmationCode)) {
            confirmationCodes.remove(email);
            return true;
        }
        return false;
    }
}
