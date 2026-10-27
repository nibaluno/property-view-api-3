package com.propertyview.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * web dto for user authentication requests
 */
public record AuthRequest(
    @NotBlank String username,
    @NotBlank String password) {}
