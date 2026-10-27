package com.propertyview.it;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:h2:mem:e2e;DB_CLOSE_DELAY=-1")
class HotelApiEndToEndTest {

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    private static final String DOUBLETREE = """
            {
              "name": "DoubleTree by Hilton Minsk",
              "description": "The DoubleTree by Hilton Hotel Minsk offers 193 luxurious rooms in the Belorussian capital",
              "brand": "Hilton",
              "address": {"houseNumber": 9, "street": "Pobediteley Avenue", "city": "Minsk",
                          "country": "Belarus", "postCode": "220004"},
              "contacts": {"phone": "+375 17 309-80-00", "email": "doubletreeminsk.info@hilton.com"},
              "arrivalTime": {"checkIn": "14:00", "checkOut": "12:00"}
            }
            """;

    private static final String MOSCOW_HILTON = """
            {
              "name": "Hilton Moscow Leningradskaya", "brand": "Hilton",
              "address": {"houseNumber": 21, "street": "Kalanchevskaya", "city": "Moscow",
                          "country": "Russia", "postCode": "107140"},
              "contacts": {"phone": "+7 495 627-55-50", "email": "moscow@hilton.com"},
              "arrivalTime": {"checkIn": "15:00"}
            }
            """;

    private static final List<String> TASK_AMENITIES = List.of(
        "Free parking", "Free WiFi", "Non-smoking rooms", "Concierge", "On-site restaurant",
        "Fitness center", "Pet-friendly rooms", "Room service", "Business center", "Meeting rooms");

    private String url(String path) {
        return "http://localhost:" + port + "/property-view" + path;
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url(path))).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getWithAuth(String path, String token) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url(path)))
                .header("Authorization", "Bearer " + token)
                .GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postWithAuth(String path, String json, String token) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(json)).build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> deleteWithAuth(String path, String token) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url(path)))
                .header("Authorization", "Bearer " + token)
                .DELETE().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private static DocumentContext json(HttpResponse<String> response) {
        return JsonPath.parse(response.body());
    }

    private static String jsonArray(List<String> items) {
        return items.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"));
    }

    @Test
    void scenarioFromTheTask() throws Exception {
        //POST /hotels
        HttpResponse<String> created = post("/hotels", DOUBLETREE);
        assertThat(created.statusCode()).isEqualTo(201);
        DocumentContext c = json(created);
        int id1 = c.read("$.id", Integer.class);
        assertThat(created.headers().firstValue("Location").orElseThrow())
            .endsWith("/property-view/hotels/" + id1);
        assertThat(c.read("$.address", String.class)).isEqualTo("9 Pobediteley Avenue, Minsk, 220004, Belarus");
        assertThat(c.read("$.phone", String.class)).isEqualTo("+375 17 309-80-00");

        int id2 = json(post("/hotels", MOSCOW_HILTON)).read("$.id", Integer.class);

        // POST /hotels/{id}/amenities
        HttpResponse<String> amenities = post("/hotels/" + id1 + "/amenities", jsonArray(TASK_AMENITIES));
        assertThat(amenities.statusCode()).isEqualTo(200);
        assertThat(json(amenities).read("$.amenities", List.class)).isEqualTo(TASK_AMENITIES);
        assertThat(post("/hotels/" + id2 + "/amenities", "[\"free wifi\", \"Pool\"]").statusCode()).isEqualTo(200);

        // GET /hotels/{id}
        DocumentContext details = json(get("/hotels/" + id1));
        assertThat(details.read("$.brand", String.class)).isEqualTo("Hilton");
        assertThat(details.read("$.address.houseNumber", Integer.class)).isEqualTo(9);
        assertThat(details.read("$.address.postCode", String.class)).isEqualTo("220004");
        assertThat(details.read("$.contacts.email", String.class)).isEqualTo("doubletreeminsk.info@hilton.com");
        assertThat(details.read("$.arrivalTime.checkIn", String.class)).isEqualTo("14:00");
        assertThat(details.read("$.arrivalTime.checkOut", String.class)).isEqualTo("12:00");
        assertThat(details.read("$.amenities.length()", Integer.class)).isEqualTo(10);
        assertThat(json(get("/hotels/" + id2)).read("$.amenities", List.class)).containsExactly("Free WiFi", "Pool");

        // GET /hotels
        assertThat(json(get("/hotels")).read("$.length()", Integer.class)).isGreaterThanOrEqualTo(2);

        // GET /search
        assertThat(json(get("/search?city=minsk")).read("$[*].name", List.class))
            .contains("DoubleTree by Hilton Minsk");
        assertThat(json(get("/search?brand=hilton")).read("$.length()", Integer.class)).isGreaterThanOrEqualTo(2);
        assertThat(json(get("/search?name=double&country=belarus")).read("$.length()", Integer.class)).isEqualTo(1);
        assertThat(json(get("/search?amenities=free%20wifi")).read("$.length()", Integer.class)).isGreaterThanOrEqualTo(2);
        assertThat(json(get("/search?amenities=Free%20WiFi&amenities=Concierge")).read("$.length()", Integer.class)).isEqualTo(1);
        assertThat(json(get("/search?amenities=Free%20WiFi,Pool")).read("$.length()", Integer.class)).isEqualTo(1);
        assertThat(get("/search?city=nowhere").body()).isEqualTo("[]");

        // GET /histogram/{param}
        Map<String, Object> byCity = json(get("/histogram/city")).read("$", Map.class);
        assertThat(byCity).containsKey("Minsk").containsKey("Moscow");
        assertThat(json(get("/histogram/brand")).read("$", Map.class)).containsKey("Hilton");
        assertThat(json(get("/histogram/country")).read("$", Map.class)).containsKey("Belarus").containsKey("Russia");
        Map<String, Object> byAmenity = json(get("/histogram/amenities")).read("$", Map.class);
        assertThat(byAmenity).containsKey("Free WiFi").containsKey("Pool").containsKey("Concierge");
    }

    @Test
    void hotelLifecycleStateTransitionsAndSecurityScenario() throws Exception {
        // 1. State transition: TransientDraft -> RejectedInvalid (400 Bad Request)
        HttpResponse<String> rejected = post("/hotels", "{\"name\":\" \"}");
        assertThat(rejected.statusCode()).isEqualTo(400);

        // 2. State transition: TransientDraft -> CreatedWithoutAmenities (201 Created, amenities = [])
        HttpResponse<String> created = post("/hotels", DOUBLETREE);
        assertThat(created.statusCode()).isEqualTo(201);
        int hotelId = json(created).read("$.id", Integer.class);
        assertThat(json(get("/hotels/" + hotelId)).read("$.amenities", List.class)).isEmpty();

        // 3. State transition: CreatedWithoutAmenities -> CreatedWithoutAmenities on empty list []
        HttpResponse<String> emptyUpdate = post("/hotels/" + hotelId + "/amenities", "[]");
        assertThat(emptyUpdate.statusCode()).isEqualTo(200);
        assertThat(json(emptyUpdate).read("$.amenities", List.class)).isEmpty();

        // 4. State transition: CreatedWithoutAmenities -> EnrichedWithAmenities
        HttpResponse<String> enriched = post("/hotels/" + hotelId + "/amenities", "[\"Free WiFi\", \"Sauna\"]");
        assertThat(enriched.statusCode()).isEqualTo(200);
        assertThat(json(enriched).read("$.amenities", List.class)).containsExactly("Free WiFi", "Sauna");

        // 5. State transition: EnrichedWithAmenities -> EnrichedWithAmenities (idempotent duplicate)
        HttpResponse<String> idempotent = post("/hotels/" + hotelId + "/amenities", "[\"free wifi\"]");
        assertThat(idempotent.statusCode()).isEqualTo(200);
        assertThat(json(idempotent).read("$.amenities", List.class)).containsExactly("Free WiFi", "Sauna");

        // 6. Auth & IDOR checks before deletion
        HttpResponse<String> loginAlice = post("/auth/login", "{\"username\":\"alice\",\"password\":\"pass1234\"}");
        assertThat(loginAlice.statusCode()).isEqualTo(200);
        String aliceToken = json(loginAlice).read("$.token", String.class);

        HttpResponse<String> loginBob = post("/auth/login", "{\"username\":\"bob\",\"password\":\"pass1234\"}");
        String bobToken = json(loginBob).read("$.token", String.class);

        // Direct access without token -> 401
        assertThat(get("/hotels/" + hotelId + "/management").statusCode()).isEqualTo(401);

        // Claim hotel for Alice
        assertThat(postWithAuth("/hotels/" + hotelId + "/claim", "", aliceToken).statusCode()).isEqualTo(204);

        // Bob tries to access Alice's hotel (IDOR) -> 403 Forbidden
        assertThat(getWithAuth("/hotels/" + hotelId + "/management", bobToken).statusCode()).isEqualTo(403);
        assertThat(deleteWithAuth("/hotels/" + hotelId, bobToken).statusCode()).isEqualTo(403);

        // Alice accesses her hotel -> 200 OK
        assertThat(getWithAuth("/hotels/" + hotelId + "/management", aliceToken).statusCode()).isEqualTo(200);

        // 7. State transition: EnrichedWithAmenities -> Deleted (204 No Content -> 404 Not Found)
        assertThat(deleteWithAuth("/hotels/" + hotelId, aliceToken).statusCode()).isEqualTo(204);
        assertThat(get("/hotels/" + hotelId).statusCode()).isEqualTo(404);
    }

    @Test
    void everythingLivesUnderThePropertyViewPrefix() throws Exception {
        HttpResponse<String> withoutPrefix = http.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/hotels")).GET().build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(withoutPrefix.statusCode()).isEqualTo(404);
        assertThat(get("/hotels").statusCode()).isEqualTo(200);
    }

    @Test
    void errorsAreProblemDetails() throws Exception {
        HttpResponse<String> notFound = get("/hotels/999999");
        assertThat(notFound.statusCode()).isEqualTo(404);
        assertThat(notFound.headers().firstValue("Content-Type").orElse("")).contains("application/problem+json");
        assertThat(notFound.body()).doesNotContain("java.lang", "org.springframework", "select ");

        HttpResponse<String> invalid = post("/hotels", "{\"name\": \" \"}");
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(json(invalid).read("$.errors.name", String.class)).isNotBlank();

        HttpResponse<String> unknownParam = get("/histogram/stars");
        assertThat(unknownParam.statusCode()).isEqualTo(400);
        assertThat(json(unknownParam).read("$.detail", String.class)).contains("brand, city, country, amenities");

        assertThat(post("/hotels/999999/amenities", "[\"x\"]").statusCode()).isEqualTo(404);
    }

    @Test
    void swaggerOpenApiAndWebClientAreAvailable() throws Exception {
        HttpResponse<String> docs = get("/v3/api-docs");
        assertThat(docs.statusCode()).isEqualTo(200);
        assertThat(docs.body()).contains("/hotels/{id}/amenities", "/search", "/histogram/{param}");

        HttpResponse<String> ui = get("/index.html");
        assertThat(ui.statusCode()).isEqualTo(200);
        assertThat(ui.body()).contains("Property View Catalog");
    }
}
