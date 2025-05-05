package com.epam.e2etests.api;

import com.epam.e2etests.dto.SongMetadataDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class ApiClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String baseUri;
    private final RequestSpecification spec;

    public ApiClient(String baseUri) {
        this.baseUri = baseUri;
        this.spec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType("audio/mpeg")
                .build();
    }

    public Response uploadMp3(byte[] payload) {
        return given()
                .spec(spec)
                .body(payload)
                .when()
                .post("/resources");
    }


    public SongMetadataDTO getMetadata(int resourceId) throws Exception {
        String json = given()
                .baseUri(baseUri)
                .accept("application/json")
                .when()
                .get("/songs/{id}", resourceId)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        return MAPPER.readValue(json, SongMetadataDTO.class);
    }


    public byte[] downloadMp3(int resourceId) {
        return given()
                .baseUri(baseUri)
                .accept("audio/mpeg")
                .when()
                .get("/resources/{id}", resourceId)
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();
    }
}
