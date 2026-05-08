package com.test.mockserver.initializers;

import com.test.mockserver.helper.ConvertInternalToMockObjects;
import com.test.mockserver.helper.FileHelper;
import com.test.mockserver.objects.ODelay;
import com.test.mockserver.objects.RequestAndResponse;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;
import org.mockserver.model.Delay;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;
import org.mockserver.server.initialize.ExpectationInitializer;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class YamlInitializer implements ExpectationInitializer {

    private final String resourceFolder;
    private int loadedCount = 0;

    public YamlInitializer(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }

    public int getLoadedCount() {
        return loadedCount;
    }

    @Override
    public Expectation[] initializeExpectations() {
        List<Expectation> expectations = new ArrayList<>();
        try {
            List<String> resources = FileHelper.getResourceFiles(resourceFolder);
            for (String resource : resources) {
                Yaml yaml = new Yaml(new Constructor(RequestAndResponse.class, new LoaderOptions()));

                InputStream inputStream = getClass().getClassLoader()
                        .getResourceAsStream(resourceFolder + resource);

                for (Object object : yaml.loadAll(inputStream)) {
                    if (object instanceof RequestAndResponse rpObj) {
                        if (!isValid(rpObj, resource)) {
                            continue;
                        }

                        List<Parameter> queryParams = ConvertInternalToMockObjects
                                .convertOQueryParametersToMockParameters(rpObj.getRequest().getQueryParameters());
                        List<Parameter> pathParams = ConvertInternalToMockObjects
                                .convertOQueryParametersToMockParameters(rpObj.getRequest().getPathParameters());
                        List<Header> requestHeaders = ConvertInternalToMockObjects
                                .convertOHeadersToMockHeaders(rpObj.getRequest().getHeaders());
                        List<Header> responseHeaders = ConvertInternalToMockObjects
                                .convertOHeadersToMockHeaders(rpObj.getResponse().getHeaders());

                        HttpRequest httpRequest = request()
                                .withMethod(rpObj.getRequest().getMethod())
                                .withPath(rpObj.getRequest().getPath())
                                .withPathParameters(pathParams)
                                .withHeaders(requestHeaders)
                                .withQueryStringParameters(queryParams);

                        if (rpObj.getRequest().getRequestBody() != null
                                && rpObj.getRequest().getRequestBody().getType() != null) {
                            httpRequest.withBody(ConvertInternalToMockObjects
                                    .mockBodyMatching(rpObj.getRequest().getRequestBody()));
                        }

                        String responseBody = rpObj.getResponse().getBody();
                        if (responseBody != null && responseBody.endsWith("\n")) {
                            responseBody = responseBody.substring(0, responseBody.length() - 1);
                        }

                        HttpResponse httpResponse = response()
                                .withStatusCode(rpObj.getResponse().getStatusCode())
                                .withHeaders(responseHeaders)
                                .withBody(responseBody);

                        ODelay delay = rpObj.getResponse().getDelay();
                        if (delay != null && delay.getUnit() != null) {
                            TimeUnit unit = TimeUnit.valueOf(delay.getUnit().toUpperCase());
                            httpResponse.withDelay(new Delay(unit, delay.getValue()));
                        }

                        Times times = rpObj.getTimes() != null
                                ? Times.exactly(rpObj.getTimes())
                                : Times.unlimited();

                        expectations.add(new Expectation(httpRequest, times, TimeToLive.unlimited(), 0)
                                .thenRespond(httpResponse));
                        loadedCount++;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load mocks from folder: " + resourceFolder, e);
        }
        return expectations.toArray(new Expectation[0]);
    }

    private boolean isValid(RequestAndResponse rpObj, String filename) {
        if (rpObj.getRequest() == null) {
            System.err.printf("[WARN] Skipping invalid mock in %s%s — request is missing%n", resourceFolder, filename);
            return false;
        }
        if (rpObj.getRequest().getMethod() == null || rpObj.getRequest().getMethod().isBlank()) {
            System.err.printf("[WARN] Skipping invalid mock in %s%s — request.method is required%n", resourceFolder, filename);
            return false;
        }
        if (rpObj.getRequest().getPath() == null || rpObj.getRequest().getPath().isBlank()) {
            System.err.printf("[WARN] Skipping invalid mock in %s%s — request.path is required%n", resourceFolder, filename);
            return false;
        }
        if (rpObj.getResponse() == null) {
            System.err.printf("[WARN] Skipping invalid mock in %s%s — response is missing%n", resourceFolder, filename);
            return false;
        }
        if (rpObj.getResponse().getStatusCode() == null) {
            System.err.printf("[WARN] Skipping invalid mock in %s%s — response.statusCode is required%n", resourceFolder, filename);
            return false;
        }
        return true;
    }
}
