package com.propertyview.application.service;

import com.propertyview.application.dto.HotelDetails;
import com.propertyview.application.dto.HotelSummary;
import com.propertyview.application.port.in.GetHotelUseCase;
import com.propertyview.application.port.in.GetHotelsUseCase;
import com.propertyview.domain.exception.HotelNotFoundException;
import com.propertyview.domain.port.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * an application service that implements two input ports simultaneously {@link GetHotelsUseCase} and {@link GetHotelUseCase}
 * it is responsible for data retrieval operations:
 * fetching a concise list of all hotels or
 * obtaining detailed information for a specific hotel by its ID.
 */
public class HotelQueryService implements GetHotelsUseCase, GetHotelUseCase {
    private static final Logger log = LoggerFactory.getLogger(HotelQueryService.class);
    private final HotelRepository hotels;

    public HotelQueryService(HotelRepository hotels) {
        this.hotels = hotels;
    }

    @Override
    public List<HotelSummary> getAll() {
        List<HotelSummary> result = hotels.findAllOverviews().stream().map(HotelSummary::from).toList();
        log.debug("Fetched {} hotel summaries", result.size());
        return result;
    }

    @Override
    public HotelDetails getById(long id) {
        return hotels.findById(id).map(HotelDetails::from)
            .orElseThrow(() -> {
                log.warn("Hotel not found for id={}", id);
                return new HotelNotFoundException(id);
            });
    }
}
