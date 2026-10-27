package com.propertyview.application.histogram;

import com.propertyview.domain.exception.UnsupportedHistogramParamException;
import java.util.Arrays;
import java.util.List;

/**
 * an enum representing the allowed parameters for constructing hotel histograms
 * (brand, city, country, amenities)
 */
public enum HistogramParam {
    BRAND("brand"), CITY("city"), COUNTRY("country"), AMENITIES("amenities");

    private final String value;

    HistogramParam(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static List<String> supportedValues() {
        return Arrays.stream(values()).map(HistogramParam::value).toList();
    }

    public static HistogramParam fromValue(String raw) {
        if (raw != null) {
            for (HistogramParam p : values()) {
                if (p.value.equalsIgnoreCase(raw.strip())) {
                    return p;
                }
            }
        }
        throw new UnsupportedHistogramParamException(raw, supportedValues());
    }
}
