package com.propertyview.application.service;

import com.propertyview.application.dto.AddAmenitiesCommand;
import com.propertyview.application.dto.HotelDetails;
import com.propertyview.application.port.in.AddAmenitiesUseCase;
import com.propertyview.domain.exception.HotelNotFoundException;
import com.propertyview.domain.model.Amenity;
import com.propertyview.domain.model.Hotel;
import com.propertyview.domain.port.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * an application service that implements the {@link AddAmenitiesUseCase} input port
 * it coordinates the scenario of adding new amenities to an existing hotel aggregate.
 */
public class AddAmenitiesService implements AddAmenitiesUseCase {
    private static final Logger log = LoggerFactory.getLogger(AddAmenitiesService.class);
    private final HotelRepository hotels;

    public AddAmenitiesService(HotelRepository hotels) {
        this.hotels = hotels;
    }

    @Override
    public HotelDetails addAmenities(AddAmenitiesCommand command) {
        Hotel hotel = hotels.findById(command.hotelId())
            .orElseThrow(() -> new HotelNotFoundException(command.hotelId()));
        List<Amenity> amenities = command.amenities().stream().map(Amenity::new).toList();
        boolean changed = hotel.addAmenities(amenities);
        Hotel saved = hotels.save(hotel);
        log.info("Updated amenities for hotel id={} (changed={}, totalAmenities={})",
            saved.id(), changed, saved.amenities().size());
        return HotelDetails.from(saved);
    }
}
