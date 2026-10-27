package com.propertyview.application.service;

import com.propertyview.application.dto.HotelDetails;
import com.propertyview.application.port.in.ManageHotelUseCase;
import com.propertyview.domain.exception.ForbiddenAccessException;
import com.propertyview.domain.exception.HotelNotFoundException;
import com.propertyview.domain.exception.UnauthorizedAccessException;
import com.propertyview.domain.model.Hotel;
import com.propertyview.domain.port.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * application service implementing authentication, hotel ownership, and IDOR-protected operations
 */
public class ManageHotelService implements ManageHotelUseCase {
    private static final Logger log = LoggerFactory.getLogger(ManageHotelService.class);
    private static final String TOKEN_PREFIX = "pv-token-";
    private static final String BEARER_PREFIX = "Bearer ";

    private final HotelRepository hotels;
    private final Map<Long, String> hotelOwners = new ConcurrentHashMap<>();

    public ManageHotelService(HotelRepository hotels) {
        this.hotels = hotels;
    }

    @Override
    public String authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.length() < 4) {
            log.warn("Authentication failed: invalid credentials for username='{}'", username);
            throw new UnauthorizedAccessException("Invalid username or password");
        }
        String cleanUser = username.trim();
        log.info("User '{}' authenticated successfully", cleanUser);
        return TOKEN_PREFIX + cleanUser;
    }

    @Override
    public void assignOwner(long hotelId, String authorizationHeader) {
        String principal = extractPrincipal(authorizationHeader);
        Hotel hotel = hotels.findById(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
        hotelOwners.put(hotel.id(), principal);
        log.info("Assigned ownership of hotel id={} to user '{}'", hotelId, principal);
    }

    @Override
    public HotelDetails getManagementDetails(long hotelId, String authorizationHeader) {
        String principal = extractPrincipal(authorizationHeader);
        Hotel hotel = hotels.findById(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
        verifyOwnership(hotelId, principal);
        log.info("Authorized management view access for hotel id={} by user '{}'", hotelId, principal);
        return HotelDetails.from(hotel);
    }

    @Override
    public void deleteHotel(long hotelId, String authorizationHeader) {
        String principal = extractPrincipal(authorizationHeader);
        hotels.findById(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
        verifyOwnership(hotelId, principal);
        hotels.deleteById(hotelId);
        hotelOwners.remove(hotelId);
        log.info("Deleted hotel id={} by authorized owner '{}'", hotelId, principal);
    }

    private void verifyOwnership(long hotelId, String principal) {
        String owner = hotelOwners.getOrDefault(hotelId, "owner-" + hotelId);
        if (!owner.equals(principal) && !"admin".equals(principal)) {
            log.warn("IDOR attempt detected: user '{}' tried to access hotel id={} owned by '{}'",
                principal, hotelId, owner);
            throw new ForbiddenAccessException(hotelId, principal);
        }
    }

    private static String extractPrincipal(String header) {
        if (header == null || header.isBlank()) {
            throw new UnauthorizedAccessException("Missing Authorization header");
        }
        String token = header.startsWith(BEARER_PREFIX)
            ? header.substring(BEARER_PREFIX.length()).trim()
            : header.trim();
        if (!token.startsWith(TOKEN_PREFIX) || token.length() <= TOKEN_PREFIX.length()) {
            throw new UnauthorizedAccessException("Invalid or expired authentication token");
        }
        return token.substring(TOKEN_PREFIX.length());
    }
}
