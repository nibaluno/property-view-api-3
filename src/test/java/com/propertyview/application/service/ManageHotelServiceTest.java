package com.propertyview.application.service;

import com.propertyview.application.dto.HotelDetails;
import com.propertyview.domain.exception.ForbiddenAccessException;
import com.propertyview.domain.exception.HotelNotFoundException;
import com.propertyview.domain.exception.UnauthorizedAccessException;
import com.propertyview.domain.model.Hotel;
import com.propertyview.domain.port.HotelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static com.propertyview.support.HotelFixtures.hotel;
import static com.propertyview.support.HotelFixtures.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageHotelServiceTest {

    @Mock
    HotelRepository hotels;

    @InjectMocks
    ManageHotelService service;

    @Test
    void authenticateIssuesTokenForValidCredentials() {
        // Arrange & Act
        String token = service.authenticate("alice", "secret123");

        // Assert
        assertThat(token).isEqualTo("pv-token-alice");
    }

    @ParameterizedTest(name = "invalid credentials ({0}, {1}) throw UnauthorizedAccessException")
    @CsvSource(value = {
        "' ', secret123",
        "alice, '123'",
        "alice, ''",
        "NULL, secret123",
        "alice, NULL"
    }, nullValues = "NULL")
    void authenticateRejectsInvalidCredentials(String username, String password) {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> service.authenticate(username, password))
            .isInstanceOf(UnauthorizedAccessException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Bearer ", "invalid-token", "Bearer pv-token-"})
    void rejectsMissingOrMalformedAuthorizationHeader(String invalidHeader) {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> service.getManagementDetails(1L, invalidHeader))
            .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void allowsAssignedOwnerToViewAndDeleteHotel() {
        // Arrange
        Hotel h = withId(1L, hotel("Hilton Minsk", "Hilton", "Minsk", "Belarus"));
        when(hotels.findById(1L)).thenReturn(Optional.of(h));
        String token = "Bearer " + service.authenticate("alice", "secret123");

        // Act
        service.assignOwner(1L, token);
        HotelDetails details = service.getManagementDetails(1L, token);
        service.deleteHotel(1L, token);

        // Assert
        assertThat(details.id()).isEqualTo(1L);
        verify(hotels).deleteById(1L);
    }

    @Test
    void blocksIdorWhenUserAccessesAnotherOwnersHotel() {
        // Arrange
        Hotel h1 = withId(1L, hotel("Hotel 1", "Hilton", "Minsk", "Belarus"));
        Hotel h2 = withId(2L, hotel("Hotel 2", "Marriott", "Moscow", "Russia"));
        when(hotels.findById(1L)).thenReturn(Optional.of(h1));
        when(hotels.findById(2L)).thenReturn(Optional.of(h2));

        String aliceHeader = "Bearer pv-token-alice";
        String bobHeader = "Bearer pv-token-bob";
        service.assignOwner(1L, aliceHeader);
        service.assignOwner(2L, bobHeader);

        // Act & Assert (Alice tries to read or delete Bob's hotel id=2)
        assertThatThrownBy(() -> service.getManagementDetails(2L, aliceHeader))
            .isInstanceOf(ForbiddenAccessException.class)
            .hasMessageContaining("hotel with id 2");

        assertThatThrownBy(() -> service.deleteHotel(2L, aliceHeader))
            .isInstanceOf(ForbiddenAccessException.class);
        verify(hotels, never()).deleteById(2L);
    }

    @Test
    void throwsHotelNotFoundWhenManagingNonExistentHotel() {
        // Arrange
        when(hotels.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.getManagementDetails(99L, "Bearer pv-token-alice"))
            .isInstanceOf(HotelNotFoundException.class);
    }
}
