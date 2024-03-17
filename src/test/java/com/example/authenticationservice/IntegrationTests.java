package com.example.authenticationservice;

import com.example.authenticationservice.dto.request.ConfirmRequestDTO;
import com.example.authenticationservice.dto.request.LoginRequestDTO;
import com.example.authenticationservice.dto.request.RegisterRequestDTO;
import com.example.authenticationservice.dto.response.UserResponseDTO;
import com.example.authenticationservice.repository.UserRepository;
import com.example.authenticationservice.service.CheckEmailService;
import com.example.authenticationservice.util.UserUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static com.example.authenticationservice.config.SecurityConfig.ACCESS_DENIED;
import static com.example.authenticationservice.service.AuthenticationService.EMAIL_EXIST;
import static com.example.authenticationservice.service.AuthenticationService.NOT_CONFIRM;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@Import(TestConfig.class)
@AutoConfigureMockMvc
public class IntegrationTests {
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mvc;

    @Autowired
    Flyway flyway;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserUtils userUtils;

    @MockBean
    CheckEmailService checkEmailService;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void registerOk(CapturedOutput output) throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Ivan")
                .email("ivan@ya.ru")
                .password("123")
                .build();

        UserResponseDTO response = UserResponseDTO.builder()
                .id(1)
                .name("Ivan")
                .email("ivan@ya.ru")
                .build();

        MvcResult result = mvc.perform(post("/register")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String exp = mapper.writeValueAsString(response);
        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).isEqualTo(exp);

        Thread.sleep(500);
        assertTrue(output.getOut().contains("value=ivan@ya.ru"));
    }

    @Test
    public void registerNotUniqueEmail() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Ivan")
                .email("ivan@ya.ru")
                .password("123")
                .build();

        userRepository.save(userUtils.toUser(request));

        mvc.perform(post("/register")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(EMAIL_EXIST));
    }

    @Test
    public void loginOk() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Ivan")
                .email("ivan@ya.ru")
                .password("123")
                .build();

        userRepository.save(userUtils.toUser(request));

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email("ivan@ya.ru")
                .password("123")
                .build();

        mvc.perform(post("/login")
                        .content(mapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void loginBadPassword() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Ivan")
                .email("ivan@ya.ru")
                .password("123")
                .build();

        userRepository.save(userUtils.toUser(request));

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email("ivan@ya.ru")
                .password("1234")
                .build();

        mvc.perform(post("/login")
                        .content(mapper.writeValueAsString(loginRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    public void confirmEmailOk() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Ivan")
                .email("ivan@ya.ru")
                .password("123")
                .build();

        userRepository.save(userUtils.toUser(request));

        when(checkEmailService.confirmEmail(anyString(), anyString())).thenReturn(Boolean.TRUE);

        ConfirmRequestDTO confirmRequest = ConfirmRequestDTO.builder()
                .email("ivan@ya.ru")
                .code("123456")
                .build();

        mvc.perform(post("/confirm")
                        .content(mapper.writeValueAsString(confirmRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void confirmEmailBadCode() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Ivan")
                .email("ivan@ya.ru")
                .password("123")
                .build();

        userRepository.save(userUtils.toUser(request));

        when(checkEmailService.confirmEmail(anyString(), anyString())).thenReturn(Boolean.FALSE);

        ConfirmRequestDTO confirmRequest = ConfirmRequestDTO.builder()
                .email("ivan@ya.ru")
                .code("123456")
                .build();

        mvc.perform(post("/confirm")
                        .content(mapper.writeValueAsString(confirmRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(NOT_CONFIRM));
    }

    @Test
    public void testOk() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .name("Ivan")
                .email("ivan@ya.ru")
                .password("123")
                .build();

        userRepository.save(userUtils.toUser(request));

        mvc.perform(get("/test")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpdmFuQHlhLnJ1IiwiaWF0IjoxNzEwNjc2Mjg1LCJleHAiOjE3MTE1NDAyODV9.hhLJXBojlZzft4XeDIVJq9kNrdTz9VXN1ZqiGrjCduYeYGba_j7SgfNHT2qOqI5dLzl-VnRJGW9JiLmKFeoCfg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ok"));
    }

    @Test
    public void testWithoutToken() throws Exception {
        mvc.perform(get("/test"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }
}
