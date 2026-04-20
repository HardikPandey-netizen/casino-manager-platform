package com.hardik.casino.DTOs;

public record RegisterResponse(
        Long id,
        String username,
        String email,
        String role,
        String message
) {
}
