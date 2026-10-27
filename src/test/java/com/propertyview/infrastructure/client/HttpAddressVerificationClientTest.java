package com.propertyview.infrastructure.client;

import com.propertyview.domain.exception.DomainValidationException;
import com.propertyview.domain.model.Address;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpAddressVerificationClientTest {

    private HttpServer stubServer;
    private HttpAddressVerificationClient client;
    private final AtomicInteger stubStatus = new AtomicInteger(200);
    private final AtomicReference<String> stubBody = new AtomicReference<>("{\"verified\":true}");
    private final AtomicReference<String> lastQuery = new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        stubServer = HttpServer.create(new InetSocketAddress(0), 0);
        stubServer.createContext("/verify", exchange -> {
            lastQuery.set(exchange.getRequestURI().getQuery());
            byte[] bytes = stubBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(stubStatus.get(), bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        stubServer.start();
        String baseUrl = "http://localhost:" + stubServer.getAddress().getPort();
        client = new HttpAddressVerificationClient(HttpClient.newHttpClient(), baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (stubServer != null) {
            stubServer.stop(0);
        }
    }

    @Test
    void returnsTrueWhenExternalHttpStubConfirmsAddress() {
        // Arrange
        stubStatus.set(200);
        stubBody.set("{\"verified\":true,\"confidence\":0.99}");
        Address address = new Address(9, "Pobediteley Avenue", "Minsk", "Belarus", "220004");

        // Act
        boolean verified = client.verifyAddress(address);

        // Assert
        assertThat(verified).isTrue();
        assertThat(lastQuery.get()).contains("city=Minsk", "country=Belarus", "postCode=220004");
    }

    @ParameterizedTest(name = "HTTP status {0} returns false without throwing")
    @ValueSource(ints = {404, 422})
    void returnsFalseWhenExternalHttpStubRejectsAddress(int status) {
        // Arrange
        stubStatus.set(status);
        stubBody.set("{\"verified\":false}");
        Address address = new Address(1, "Unknown", "Atlantis", "Ocean", "00000");

        // Act
        boolean verified = client.verifyAddress(address);

        // Assert
        assertThat(verified).isFalse();
    }

    @ParameterizedTest(name = "HTTP server error {0} throws IllegalStateException")
    @ValueSource(ints = {500, 502, 503})
    void throwsExceptionWhenExternalServiceFails(int errorStatus) {
        // Arrange
        stubStatus.set(errorStatus);
        stubBody.set("{\"error\":\"upstream error\"}");
        Address address = new Address(9, "Pobediteley Avenue", "Minsk", "Belarus", "220004");

        // Act & Assert
        assertThatThrownBy(() -> client.verifyAddress(address))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(String.valueOf(errorStatus));
    }

    @Test
    void rejectsNullAddressBeforeMakingHttpCall() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> client.verifyAddress(null))
            .isInstanceOf(DomainValidationException.class);
        assertThat(lastQuery.get()).isNull();
    }
}
