package com.socialnetwork.config.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String jwt;
    private Long userId;
    private String username;
}
