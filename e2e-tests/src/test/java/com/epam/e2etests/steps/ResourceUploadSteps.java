package com.epam.e2etests.steps;

import com.epam.e2etests.api.ApiClient;
import com.epam.e2etests.dto.SongMetadataDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.awaitility.Awaitility;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceUploadSteps {

    private final ApiClient api;
    private byte[] mp3Data;
    private Response uploadResponse;
    private int resourceId;
    private final String BASE_URL = "http://localhost:8080";

    public ResourceUploadSteps() {
        this.api = new ApiClient(BASE_URL);
    }

    @Given("a valid MP3 file")
    public void loadMp3() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/sample.mp3")) {
            assertThat(in).as("sample.mp3 must exist").isNotNull();
            mp3Data = in.readAllBytes();
        }
    }

    @When("the MP3 is uploaded")
    public void uploadMp3() {
        uploadResponse = api.uploadMp3(mp3Data);
    }

    @Then("the response status is {int} and response contains an id of the created resource")
    public void verifyUploadResponse(int expectedStatus) {
        uploadResponse.then().statusCode(expectedStatus);
        resourceId = uploadResponse.jsonPath().getInt("id");
        assertThat(resourceId).as("new resource id").isPositive();
    }

    @Then("the song metadata eventually matches:")
    public void verifyMetadata(DataTable table) {
        Map<String, String> expected = table
                .asLists(String.class)
                .stream()
                .collect(Collectors.toMap(
                        cols -> cols.get(0),
                        cols -> cols.get(1)
                ));

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    SongMetadataDTO actual = api.getMetadata(resourceId);

                    assertThat(actual.getName())
                            .as("name").isEqualTo(expected.get("name"));
                    assertThat(actual.getArtist())
                            .as("artist").isEqualTo(expected.get("artist"));
                    assertThat(actual.getAlbum())
                            .as("album").isEqualTo(expected.get("album"));
                    assertThat(actual.getDuration())
                            .as("duration").isEqualTo(expected.get("duration"));
                    assertThat(actual.getYear())
                            .as("year").isEqualTo(expected.get("year"));
                });
    }

    @Then("the original MP3 can be re-downloaded intact")
    public void verifyDownload() {
        byte[] downloaded = api.downloadMp3(resourceId);
        assertThat(downloaded).as("downloaded matches uploaded").isEqualTo(mp3Data);
    }
}