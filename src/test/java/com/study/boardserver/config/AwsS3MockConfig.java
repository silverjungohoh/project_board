package com.study.boardserver.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class AwsS3MockConfig {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${test-path}")
    private String testPath;

    @Value("${test-port}")
    private int testPort;

    @Bean
    public S3Mock s3Mock() {
        return new S3Mock.Builder().withPort(testPort).withInMemoryBackend().build();
    }

    @Primary
    @Bean
    public AmazonS3 amazonS3(S3Mock s3Mock) {
        s3Mock.start();

        AwsClientBuilder.EndpointConfiguration endpoint
                = new AwsClientBuilder.EndpointConfiguration(testPath, region);

        AmazonS3 client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();

        client.createBucket(bucket);
        return client;
    }
}
