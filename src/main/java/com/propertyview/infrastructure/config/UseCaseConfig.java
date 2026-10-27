package com.propertyview.infrastructure.config;

import com.propertyview.application.histogram.HistogramParam;
import com.propertyview.application.histogram.HistogramProvider;
import com.propertyview.application.histogram.RepositoryHistogramProvider;
import com.propertyview.application.port.in.AddAmenitiesUseCase;
import com.propertyview.application.port.in.CreateHotelUseCase;
import com.propertyview.application.port.in.GetHistogramUseCase;
import com.propertyview.application.port.in.ManageHotelUseCase;
import com.propertyview.application.port.in.SearchHotelsUseCase;
import com.propertyview.application.service.AddAmenitiesService;
import com.propertyview.application.service.CreateHotelService;
import com.propertyview.application.service.HistogramService;
import com.propertyview.application.service.HotelQueryService;
import com.propertyview.application.service.ManageHotelService;
import com.propertyview.application.service.SearchHotelsService;
import com.propertyview.domain.port.HotelRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * spring configuration class that wires pure application services and strategies as Spring beans
 */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

    @Bean
    CreateHotelUseCase createHotelUseCase(HotelRepository hotels) {
        return new CreateHotelService(hotels);
    }

    @Bean
    HotelQueryService hotelQueryService(HotelRepository hotels) {
        return new HotelQueryService(hotels);
    }

    @Bean
    AddAmenitiesUseCase addAmenitiesUseCase(HotelRepository hotels) {
        return new AddAmenitiesService(hotels);
    }

    @Bean
    SearchHotelsUseCase searchHotelsUseCase(HotelRepository hotels) {
        return new SearchHotelsService(hotels);
    }

    @Bean
    ManageHotelUseCase manageHotelUseCase(HotelRepository hotels) {
        return new ManageHotelService(hotels);
    }

    @Bean
    HistogramProvider brandHistogram(HotelRepository hotels) {
        return new RepositoryHistogramProvider(HistogramParam.BRAND, hotels::countByBrand);
    }

    @Bean
    HistogramProvider cityHistogram(HotelRepository hotels) {
        return new RepositoryHistogramProvider(HistogramParam.CITY, hotels::countByCity);
    }

    @Bean
    HistogramProvider countryHistogram(HotelRepository hotels) {
        return new RepositoryHistogramProvider(HistogramParam.COUNTRY, hotels::countByCountry);
    }

    @Bean
    HistogramProvider amenitiesHistogram(HotelRepository hotels) {
        return new RepositoryHistogramProvider(HistogramParam.AMENITIES, hotels::countByAmenity);
    }

    @Bean
    GetHistogramUseCase getHistogramUseCase(List<HistogramProvider> providers) {
        return new HistogramService(providers);
    }
}
