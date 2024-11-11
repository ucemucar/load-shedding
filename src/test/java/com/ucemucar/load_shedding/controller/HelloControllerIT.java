package com.ucemucar.load_shedding.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloControllerIT {

    @LocalServerPort
    private int port;

    private String baseUrl;
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port + "/hello";
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new CustomResponseErrorHandler()); //This is for avoiding test to fail
    }

    @Test
    public void testLoadShedding_whenUnderLimit_shouldReturnData() throws ExecutionException, InterruptedException {
        // 200 concurrent requests
        List<CompletableFuture<ResponseEntity<String>>> futures = IntStream.range(0, 200)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> restTemplate.getForEntity(baseUrl, String.class)))
                .collect(Collectors.toList());

        // Collect results
        List<ResponseEntity<String>> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        long successCount = responses.stream()
                .filter(response -> response.getStatusCodeValue() == 200)
                .count();

        long overloadCount = responses.stream()
                .filter(response -> response.getStatusCodeValue() == 503)
                .count();

        // Let's check results
        System.out.println("200 OK: " + successCount);
        System.out.println("503 Service Unavailable: " + overloadCount);

        // There should be responses with HTTP 200
        assertTrue(successCount > 0, "successCount is greater than zero");

        // There should be responses with HTTP 503
        assertTrue(overloadCount > 0, "overloadCount is greater than zero");

        // Results should total to 200
        assertEquals(200, successCount + overloadCount);
    }


    private class CustomResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.getStatusCode().value() == 503) {
                System.out.println("503 - Service Unavailable received. Handling it accordingly.");
            }
        }

    }
}
