package com.propertyview.presentation.web.dto;

/**
 * web dto for returning issued authentication token
 */
public record AuthResponse(
    String token,
    String tokenType,
    String username) {}
