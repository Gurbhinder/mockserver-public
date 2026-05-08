package com.test.mockserver.initializers;

import com.test.mockserver.helper.ConvertInternalToMockObjects;
import com.test.mockserver.helper.FileHelper;
import com.test.mockserver.objects.RequestAndResponse;
import org.mockserver.mock.Expectation;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;
import org.mockserver.server.initialize.ExpectationInitializer;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class GetInitializer implements ExpectationInitializer {
    @Override
    public Expectation[] initializeExpectations() {
        ArrayList<Expectation> expectations = new ArrayList<>();
        List<String> resources = null;
        try {
            resources = FileHelper.getResourceFiles("get/");
            for (String resource : resources) {
                Yaml yaml = new Yaml(new Constructor(RequestAndResponse.class, new LoaderOptions()));

                InputStream inputStream = this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("get/" + resource);

                for (Object object : yaml.loadAll(inputStream)) {
                    if (object instanceof RequestAndResponse) {
                        RequestAndResponse rpObj = (RequestAndResponse) object;
                        List<Parameter> queryParams = ConvertInternalToMockObjects
                                .convertOQueryParametersToMockParameters(rpObj.getRequest().getQueryParameters());

                        List<Parameter> pathParams = ConvertInternalToMockObjects
                                .convertOQueryParametersToMockParameters(rpObj.getRequest().getPathParameters());

                        List<Header> requestHeaders = ConvertInternalToMockObjects
                                .convertOHeadersToMockHeaders(rpObj.getRequest().getHeaders());

                        List<Header> responseHeaders = ConvertInternalToMockObjects
                                .convertOHeadersToMockHeaders(rpObj.getResponse().getHeaders());

                        HttpRequest httpRequest = request().withMethod(rpObj.getRequest().getMethod())
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
                        HttpResponse httpResponse = response().withStatusCode(rpObj.getResponse().getStatusCode())
                                .withHeaders(responseHeaders)
                                .withBody(responseBody);

                        expectations.add(new Expectation(httpRequest).thenRespond(httpResponse));
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return expectations.toArray(new Expectation[0]);
    }

}
