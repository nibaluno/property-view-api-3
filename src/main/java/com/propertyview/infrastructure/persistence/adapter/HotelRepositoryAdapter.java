package com.propertyview.infrastructure.persistence.adapter;

import com.propertyview.domain.exception.HotelNotFoundException;
import com.propertyview.domain.model.*;
import com.propertyview.domain.port.HotelRepository;
import com.propertyview.infrastructure.persistence.entity.*;
import com.propertyview.infrastructure.persistence.mapper.HotelPersistenceMapper;
import com.propertyview.infrastructure.persistence.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JPA implementation of the {@link HotelRepository} port
 * handles entity mapping, dynamic search, and amenity deduplication
 */
@Repository
@Transactional(readOnly = true)
public class HotelRepositoryAdapter implements HotelRepository {

    private static final Sort BY_ID = Sort.by("id");
    private final HotelJpaRepository hotels;
    private final AmenityJpaRepository amenities;
    private final HotelPersistenceMapper mapper;

    public HotelRepositoryAdapter(HotelJpaRepository hotels, AmenityJpaRepository amenities,
                                  HotelPersistenceMapper mapper) {
        this.hotels = hotels;
        this.amenities = amenities;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Hotel save(Hotel hotel) {
        HotelEntity entity = hotel.id() == null
            ? mapper.toNewEntity(hotel)
            : hotels.findByIdWithAmenities(hotel.id())
            .orElseThrow(() -> new HotelNotFoundException(hotel.id()));
        appendMissingAmenities(entity, hotel.amenities());
        return mapper.toDomain(hotels.save(entity));
    }

    @Override
    public Optional<Hotel> findById(long id) {
        return hotels.findByIdWithAmenities(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        if (!hotels.existsById(id)) {
            throw new HotelNotFoundException(id);
        }
        hotels.deleteById(id);
    }

    @Override
    public List<HotelOverview> findAllOverviews() {
        return hotels.findAll(BY_ID).stream().map(mapper::toOverview).toList();
    }

    @Override
    public List<HotelOverview> search(HotelSearchCriteria criteria) {
        return hotels.findAll(HotelSpecifications.from(criteria), BY_ID)
            .stream().map(mapper::toOverview).toList();
    }

    @Override
    public Map<String, Long> countByBrand() {
        return sorted(hotels.countByBrand());
    }

    @Override
    public Map<String, Long> countByCity() {
        return sorted(hotels.countByCity());
    }

    @Override
    public Map<String, Long> countByCountry() {
        return sorted(hotels.countByCountry());
    }

    @Override
    public Map<String, Long> countByAmenity() {
        return sorted(hotels.countByAmenity());
    }

    private void appendMissingAmenities(HotelEntity entity, Set<Amenity> wanted) {
        Set<String> present = entity.getAmenities().stream()
            .map(AmenityEntity::getNormalizedName).collect(Collectors.toSet());
        List<Amenity> missing = wanted.stream().filter(a -> !present.contains(a.normalizedName())).toList();
        if (missing.isEmpty()) {
            return;
        }

        Map<String, AmenityEntity> known = amenities
            .findByNormalizedNameIn(missing.stream().map(Amenity::normalizedName).toList())
            .stream().collect(Collectors.toMap(AmenityEntity::getNormalizedName, Function.identity()));

        for (Amenity amenity : missing) {
            AmenityEntity e = known.computeIfAbsent(amenity.normalizedName(),
                k -> amenities.save(mapper.toEntity(amenity)));
            entity.getAmenities().add(e);
        }
    }

    private static Map<String, Long> sorted(List<CountRow> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        rows.stream().sorted(Comparator.comparing(CountRow::getLabel))
            .forEach(r -> result.put(r.getLabel(), r.getTotal()));
        return result;
    }
}
