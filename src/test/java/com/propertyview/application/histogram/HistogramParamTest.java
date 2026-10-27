package com.propertyview.application.histogram;

import com.propertyview.domain.exception.UnsupportedHistogramParamException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HistogramParamTest {

    @ParameterizedTest(name = "input \"{0}\" resolves to {1}")
    @CsvSource({
        "'brand', BRAND",
        "' BRAND ', BRAND",
        "' City ', CITY",
        "'country', COUNTRY",
        "'AMENITIES', AMENITIES"
    })
    void parsesCaseInsensitively(String input, HistogramParam expected) {
        // Arrange & Act
        HistogramParam actual = HistogramParam.fromValue(input);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"stars", "rating", "price", "   "})
    void unknownValueListsSupported(String unsupported) {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> HistogramParam.fromValue(unsupported))
            .isInstanceOf(UnsupportedHistogramParamException.class)
            .hasMessageContaining("brand, city, country, amenities");
    }
}
