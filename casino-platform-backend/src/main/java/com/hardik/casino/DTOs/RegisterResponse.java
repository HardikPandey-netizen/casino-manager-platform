package com.hardik.casino.DTOs;

public record RegisterResponse(
        Long id,
        String username,
        String role,
        String message
) {
}
