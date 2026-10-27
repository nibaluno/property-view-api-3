package com.propertyview.application.port.in;

import com.propertyview.application.dto.HotelDetails;

/**
 * input port for authentication, ownership management, and protected hotel operations
 */
public interface ManageHotelUseCase {
    String authenticate(String username, String password);
    void assignOwner(long hotelId, String authorizationHeader);
    HotelDetails getManagementDetails(long hotelId, String authorizationHeader);
    void deleteHotel(long hotelId, String authorizationHeader);
}
