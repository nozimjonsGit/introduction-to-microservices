package com.epam.resourceservice.cucumber;

import com.epam.resourceservice.BaseTestContainer;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"resource-uploaded"})
public class ResourceUploadSteps extends BaseTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private byte[] audio;

    private String responseBody;

    private int resourceId;

    @Given("a valid MP3 file")
    public void givenMP3() {
        try {
            ClassPathResource resource = new ClassPathResource("sample.mp3");
            if (!resource.exists()) {
                throw new FileNotFoundException("The file 'sample.mp3' was not found in the classpath.");
            }
            audio = Files.readAllBytes(resource.getFile().toPath());
            assertThat(audio).isNotEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load the MP3 file", e);
        }
    }

    @When("I POST the payload to {string}")
    public void whenPost(String path) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .contentType("audio/mpeg")
                        .accept("application/json")
                        .content(audio))
                .andExpect(status().isOk())
                .andReturn();

        responseBody = result.getResponse().getContentAsString();
    }

    @Then("the response JSON contains an {string}")
    public void thenResponseContains(String field) {
        assertThat(responseBody).contains(field);

        resourceId = JsonPath.read(responseBody, "$.id");
        assertThat(resourceId).isPositive();
    }

    @Then("a message with that id is published to the Kafka topic {string}")
    public void thenMessagePublished(String topic) {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                "testGroup", "false", embeddedKafkaBroker
        );
        Consumer<String, Long> consumer = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new LongDeserializer()
        ).createConsumer();
        embeddedKafkaBroker.consumeFromEmbeddedTopics(consumer, topic);

        ConsumerRecord<String, Long> record = KafkaTestUtils.getSingleRecord(consumer, topic);

        long actualId = record.value();
        assertThat(actualId).isEqualTo(resourceId);

        consumer.close();
    }
}
