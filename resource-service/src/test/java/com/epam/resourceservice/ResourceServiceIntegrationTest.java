package com.epam.resourceservice;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"resource-uploaded"})
@ActiveProfiles("test")
public class ResourceServiceIntegrationTest extends BaseTestContainer {

    @Autowired
    private MockMvc mockMvc;

    private byte[] AUDIO;

    @BeforeAll
    void setUp() throws IOException {
        ClassPathResource resource = new ClassPathResource("sample.mp3");
        if (!resource.exists()) {
            throw new FileNotFoundException("The file 'sample.mp3' was not found in the classpath.");
        }
        AUDIO = Files.readAllBytes(resource.getFile().toPath());
    }

    @Test
    void createAndProcessResource_Success() throws Exception {
        String json = mockMvc.perform(post("/resources")
                        .contentType("audio/mpeg")
                        .content(AUDIO))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int id = JsonPath.read(
                json, "$.id");

        assertEquals(1, id);
    }

    @Test
    void createGetAndDeleteFlow() throws Exception {
        byte[] resource1 = AUDIO;
        byte[] resource2 = AUDIO;

        // Create resources
        String json1 = mockMvc.perform(post("/resources")
                        .contentType("audio/mpeg")
                        .content(resource1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        int id1 = JsonPath.read(json1, "$.id");

        String json2 = mockMvc.perform(post("/resources")
                        .contentType("audio/mpeg")
                        .content(resource2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        int id2 = JsonPath.read(json2, "$.id");

        // Get resource by ID
        mockMvc.perform(get("/resources/" + id1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"))
                .andExpect(content().bytes(resource1));

        mockMvc.perform(get("/resources/" + id2))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"))
                .andExpect(content().bytes(resource2));

        // Delete resources
        mockMvc.perform(delete("/resources?id=" + id1 + "," + id2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ids").isArray())
                .andExpect(jsonPath("$.ids[0]").value(id1))
                .andExpect(jsonPath("$.ids[1]").value(id2));

        // Verify deletion
        mockMvc.perform(get("/resources/" + id1))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/resources/" + id2))
                .andExpect(status().isNotFound());
    }
}
