package com.propertyview.domain.model;

import com.propertyview.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmenityTest {

    @ParameterizedTest(name = "raw input \"{0}\" normalizes to display=\"{1}\" and key=\"{2}\"")
    @CsvSource({
        "'  Free   WiFi ', 'Free WiFi', 'free wifi'",
        "'Non-smoking\t\trooms', 'Non-smoking rooms', 'non-smoking rooms'",
        "'Concierge', 'Concierge', 'concierge'"
    })
    void normalizesWhitespaceAndKeepsOriginalCase(String raw, String expectedName, String expectedKey) {
        // Arrange & Act
        Amenity amenity = new Amenity(raw);

        // Assert
        assertThat(amenity.name()).isEqualTo(expectedName);
        assertThat(amenity.normalizedName()).isEqualTo(expectedKey);
    }

    @Test
    void equalityIgnoresCase() {
        // Arrange
        Amenity first = new Amenity("Free WiFi");
        Amenity second = new Amenity("free wifi");
        Amenity third = new Amenity("FREE  WIFI");

        // Act & Assert
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(third);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t\n"})
    void rejectsNullOrBlankAmenity(String invalidInput) {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new Amenity(invalidInput))
            .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsAmenityExceedingMaxLength() {
        // Arrange
        String tooLong = "A".repeat(Amenity.MAX_LENGTH + 1);

        // Act & Assert
        assertThatThrownBy(() -> new Amenity(tooLong))
            .isInstanceOf(DomainValidationException.class);
    }
}
