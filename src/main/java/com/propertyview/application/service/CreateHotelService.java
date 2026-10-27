package com.propertyview.application.service;

import com.propertyview.application.dto.CreateHotelCommand;
import com.propertyview.application.dto.HotelSummary;
import com.propertyview.application.port.in.CreateHotelUseCase;
import com.propertyview.domain.model.Hotel;
import com.propertyview.domain.port.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * an application service that implements the {@link CreateHotelUseCase} input port
 * it coordinates the scenario for creating a new hotel, utilizing a domain factory
 */
public class CreateHotelService implements CreateHotelUseCase {
    private static final Logger log = LoggerFactory.getLogger(CreateHotelService.class);
    private final HotelRepository hotels;

    public CreateHotelService(HotelRepository hotels) {
        this.hotels = hotels;
    }

    @Override
    public HotelSummary create(CreateHotelCommand c) {
        Hotel hotel = Hotel.create(c.name(), c.description(), c.brand(), c.address(), c.contacts(), c.arrivalTime());
        Hotel saved = hotels.save(hotel);
        log.info("Created hotel id={} name='{}' brand='{}'", saved.id(), saved.name(), saved.brand());
        return HotelSummary.from(saved);
    }
}
