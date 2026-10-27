package com.propertyview.domain.port;

import com.propertyview.domain.model.Hotel;
import com.propertyview.domain.model.HotelOverview;
import com.propertyview.domain.model.HotelSearchCriteria;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * outbound repository port defining persistence operations for hotels
 */
public interface HotelRepository {
    Hotel save(Hotel hotel);
    Optional<Hotel> findById(long id);
    List<HotelOverview> findAllOverviews();
    List<HotelOverview> search(HotelSearchCriteria criteria);
    Map<String, Long> countByBrand();
    Map<String, Long> countByCity();
    Map<String, Long> countByCountry();
    Map<String, Long> countByAmenity();
    default void deleteById(long id) {
        // optional default for backwards compatibility in mocks
    }
}
