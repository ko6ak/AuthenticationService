package com.example.authenticationservice.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmRequestDTO {
    private String email;
    private String code;
}
