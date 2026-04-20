package com.hardik.casino.DTOs;

public record RegisterRequest(
        String username,
        String email,
        String password
) {
}
