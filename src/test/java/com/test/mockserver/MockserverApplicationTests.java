package com.test.mockserver;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@SpringBootTest
class MockserverApplicationTests {

    private static ClientAndServer mockServer;
    private static HttpClient client;
    private static final String BASE_URL = "http://localhost:8000";

    @BeforeAll
    static void startServer() {
        System.setProperty("mockserver.initializationClass", ExpectationInitialization.class.getName());
        mockServer = startClientAndServer(8000);
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopServer() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getRequest_returnsExpectedResponse() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/test"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("message"));
    }

    @Test
    void getRequest_withQueryParam_returnsMatchedResponse() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/test?query=param1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("query parameter"));
    }

    @Test
    void postRequest_withJsonBody_returnsMatchedResponse() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/post-test"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"id\": 1}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("JSON Exact Match"));
    }
}
