package com.mrs.shakes.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
