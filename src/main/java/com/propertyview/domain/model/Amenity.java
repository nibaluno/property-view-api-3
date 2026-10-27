package com.propertyview.domain.model;

import com.propertyview.domain.exception.DomainValidationException;
import java.util.Locale;

/**
 * a value object representing a hotel amenity
 * it ensures string normalization and case-insensitive comparison
 */
public record Amenity(String name) {
    public static final int MAX_LENGTH = 255;

    public Amenity {
        name = normalize(name);
    }

    public String normalizedName() {
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Amenity other && normalizedName().equals(other.normalizedName());
    }

    @Override
    public int hashCode() {
        return normalizedName().hashCode();
    }

    private static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainValidationException("amenity name must not be blank");
        }
        String n = raw.strip().replaceAll("\\s+", " ");
        if (n.length() > MAX_LENGTH) {
            throw new DomainValidationException("amenity name is too long");
        }
        return n;
    }
}
