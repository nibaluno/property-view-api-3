package com.propertyview.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotelSearchCriteriaTest {

    @Test
    void blankValuesBecomeNullAndAmenitiesAreDeduplicated() {
        // Arrange & Act
        HotelSearchCriteria criteria = HotelSearchCriteria.of(
            " ", null, " Minsk ", "", Arrays.asList("Free WiFi", "free wifi", " ", null));

        // Assert
        assertThat(criteria.name()).isNull();
        assertThat(criteria.city()).isEqualTo("Minsk");
        assertThat(criteria.amenities()).hasSize(1);
        assertThat(criteria.isEmpty()).isFalse();
        assertThat(HotelSearchCriteria.of(null, null, null, null, null).isEmpty()).isTrue();
    }

    @ParameterizedTest(name = "filter ({0}, {1}, {2}, {3}) -> isEmpty={4}")
    @CsvSource({
        "'Hilton', '', '', '', false",
        "'', 'Hilton', '', '', false",
        "'', '', 'Minsk', '', false",
        "'', '', '', 'Belarus', false",
        "'   ', '  ', '', ' ', true"
    })
    void evaluatesEmptyCriteriaCorrectly(String name, String brand, String city, String country, boolean expectedEmpty) {
        // Arrange & Act
        HotelSearchCriteria criteria = HotelSearchCriteria.of(name, brand, city, country, List.of());

        // Assert
        assertThat(criteria.isEmpty()).isEqualTo(expectedEmpty);
    }
}
