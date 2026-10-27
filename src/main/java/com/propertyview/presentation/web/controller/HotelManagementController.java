package com.propertyview.presentation.web.controller;

import com.propertyview.application.port.in.ManageHotelUseCase;
import com.propertyview.presentation.web.dto.AuthRequest;
import com.propertyview.presentation.web.dto.AuthResponse;
import com.propertyview.presentation.web.dto.HotelDetailsResponse;
import com.propertyview.presentation.web.mapper.HotelWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication and owner-protected hotel management operations
 */
@Tag(name = "Management & Auth", description = "Authentication and owner-protected hotel endpoints")
@RestController
public class HotelManagementController {
    private final ManageHotelUseCase manageHotel;

    public HotelManagementController(ManageHotelUseCase manageHotel) {
        this.manageHotel = manageHotel;
    }

    @Operation(summary = "Authenticate and receive Bearer token")
    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        String token = manageHotel.authenticate(request.username(), request.password());
        return new AuthResponse(token, "Bearer", request.username().trim());
    }

    @Operation(summary = "Claim ownership of a hotel for the authenticated user")
    @PostMapping("/hotels/{id}/claim")
    public ResponseEntity<Void> claim(
        @PathVariable("id") long id,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        manageHotel.assignOwner(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Protected management view for a specific hotel (requires owner authentication)")
    @GetMapping("/hotels/{id}/management")
    public HotelDetailsResponse getManagementView(
        @PathVariable("id") long id,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return HotelWebMapper.toResponse(manageHotel.getManagementDetails(id, authHeader));
    }

    @Operation(summary = "Delete a hotel (transitions Hotel to Deleted state, requires owner authentication)")
    @DeleteMapping("/hotels/{id}")
    public ResponseEntity<Void> deleteHotel(
        @PathVariable("id") long id,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        manageHotel.deleteHotel(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}
