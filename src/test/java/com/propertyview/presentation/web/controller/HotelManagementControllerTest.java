package com.propertyview.presentation.web.controller;

import com.propertyview.application.dto.HotelDetails;
import com.propertyview.application.port.in.ManageHotelUseCase;
import com.propertyview.domain.exception.ForbiddenAccessException;
import com.propertyview.domain.exception.UnauthorizedAccessException;
import com.propertyview.domain.model.Address;
import com.propertyview.domain.model.ArrivalTime;
import com.propertyview.domain.model.Contacts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HotelManagementController.class)
class HotelManagementControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ManageHotelUseCase manageHotel;

    @Test
    void loginReturnsTokenAndBearerType() throws Exception {
        // Arrange
        when(manageHotel.authenticate("alice", "secret123")).thenReturn("pv-token-alice");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"secret123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("pv-token-alice"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void managementViewReturns401WhenUnauthenticated() throws Exception {
        // Arrange
        when(manageHotel.getManagementDetails(1L, null))
            .thenThrow(new UnauthorizedAccessException("Missing Authorization header"));

        // Act & Assert
        mockMvc.perform(get("/hotels/1/management"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void managementViewReturns403OnIdorViolation() throws Exception {
        // Arrange
        when(manageHotel.getManagementDetails(2L, "Bearer pv-token-alice"))
            .thenThrow(new ForbiddenAccessException(2L, "alice"));

        // Act & Assert
        mockMvc.perform(get("/hotels/2/management")
                .header(HttpHeaders.AUTHORIZATION, "Bearer pv-token-alice"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void managementViewReturns200ForOwner() throws Exception {
        // Arrange
        HotelDetails details = new HotelDetails(1L, "Hilton Minsk", "desc", "Hilton",
            new Address(9, "Pobediteley", "Minsk", "Belarus", "220004"),
            new Contacts("+375 17 309-80-00", "info@hilton.com"),
            new ArrivalTime(LocalTime.of(14, 0), LocalTime.of(12, 0)),
            List.of("Free WiFi"));
        when(manageHotel.getManagementDetails(1L, "Bearer pv-token-alice")).thenReturn(details);

        // Act & Assert
        mockMvc.perform(get("/hotels/1/management")
                .header(HttpHeaders.AUTHORIZATION, "Bearer pv-token-alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Hilton Minsk"));
    }

    @Test
    void claimAndDeleteReturn204NoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/hotels/1/claim")
                .header(HttpHeaders.AUTHORIZATION, "Bearer pv-token-alice"))
            .andExpect(status().isNoContent());
        verify(manageHotel).assignOwner(1L, "Bearer pv-token-alice");

        mockMvc.perform(delete("/hotels/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer pv-token-alice"))
            .andExpect(status().isNoContent());
        verify(manageHotel).deleteHotel(1L, "Bearer pv-token-alice");
    }
}
