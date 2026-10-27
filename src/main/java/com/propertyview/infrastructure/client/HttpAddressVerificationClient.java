package com.propertyview.infrastructure.client;

import com.propertyview.domain.exception.DomainValidationException;
import com.propertyview.domain.model.Address;
import com.propertyview.domain.port.AddressVerificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * HTTP adapter implementing {@link AddressVerificationPort} to verify hotel addresses via an external service
 */
public class HttpAddressVerificationClient implements AddressVerificationPort {
    private static final Logger log = LoggerFactory.getLogger(HttpAddressVerificationClient.class);

    private final HttpClient httpClient;
    private final String baseUrl;

    public HttpAddressVerificationClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public boolean verifyAddress(Address address) {
        if (address == null) {
            throw new DomainValidationException("address must not be null");
        }
        String query = String.format("city=%s&country=%s&postCode=%s",
            encode(address.city()), encode(address.country()), encode(address.postCode()));
        URI uri = URI.create(baseUrl + "/verify?" + query);

        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(5))
            .header("Accept", "application/json")
            .GET()
            .build();

        try {
            log.info("Verifying address via external service: city={}, country={}", address.city(), address.country());
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 200) {
                return response.body() != null && response.body().contains("\"verified\":true");
            }
            if (status == 404 || status == 422) {
                log.warn("Address verification rejected by external service with status {}", status);
                return false;
            }
            log.error("External address service returned unexpected status {}", status);
            throw new IllegalStateException("External address verification service failed with status " + status);
        } catch (IOException e) {
            log.error("I/O error communicating with address verification service", e);
            throw new IllegalStateException("Unable to reach external address verification service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Address verification interrupted", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
