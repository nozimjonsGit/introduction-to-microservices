package com.epam.resourceservice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;

import java.net.URI;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTestContainer {

    private static final String BUCKET = "resource-bucket";
    private static S3Client s3Client;

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("resources")
            .withUsername("postgres")
            .withPassword("password");

    @Container
    static LocalStackContainer LOCALSTACK = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3)
            .withEnv("DEFAULT_REGION", "us-east-1")
            .withEnv("AWS_S3_BUCKET_NAME", BUCKET);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        POSTGRES.start();
        LOCALSTACK.start();

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        URI endpoint = LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.S3);
        registry.add("aws.s3.endpoint-url", endpoint::toString);
        registry.add("aws.s3.bucket-name", () -> BUCKET);
        registry.add("aws.s3.region", () -> LOCALSTACK.getRegion());
        registry.add("aws.s3.access-key", LOCALSTACK::getAccessKey);
        registry.add("aws.s3.secret-key", LOCALSTACK::getSecretKey);

        s3Client = S3Client.builder()
                .endpointOverride(LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.S3))
                .region(Region.of(LOCALSTACK.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                LOCALSTACK.getAccessKey(),
                                LOCALSTACK.getSecretKey()
                        )
                ))
                .build();

        s3Client.createBucket(b -> b.bucket(BUCKET));
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
        WaiterResponse<HeadBucketResponse> waiterResponse = s3Client.waiter()
                .waitUntilBucketExists(HeadBucketRequest.builder().bucket(BUCKET).build());
        waiterResponse.matched().response().ifPresent(System.out::println);
    }

    @AfterAll
    void tearDown() {
        s3Client.close();
        LOCALSTACK.stop();
        POSTGRES.stop();
    }
}
