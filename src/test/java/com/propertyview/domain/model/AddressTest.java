package com.propertyview.domain.model;

import com.propertyview.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    @Test
    void formatsSingleLineAsInTask() {
        // Arrange
        Address address = new Address(9, "Pobediteley Avenue", "Minsk", "Belarus", "220004");

        // Act
        String formatted = address.toSingleLine();

        // Assert
        assertThat(formatted).isEqualTo("9 Pobediteley Avenue, Minsk, 220004, Belarus");
    }

    @ParameterizedTest(name = "houseNumber={0} should be rejected")
    @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
    void rejectsNonPositiveHouseNumber(int invalidHouseNumber) {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new Address(invalidHouseNumber, "St", "Minsk", "Belarus", "220004"))
            .isInstanceOf(DomainValidationException.class)
            .hasMessageContaining("houseNumber");
    }

    @ParameterizedTest(name = "invalid address combination ({0}, {1}, {2}, {3}) is rejected")
    @CsvSource( value = {
        "' ', Minsk, Belarus, 220004",
        "Street, '   ', Belarus, 220004",
        "Street, Minsk, '', 220004",
        "Street, Minsk, Belarus, '  '"
    })
    void rejectsBlankTextFields(String street, String city, String country, String postCode) {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new Address(1, street, city, country, postCode))
            .isInstanceOf(DomainValidationException.class);
    }
}
