package com.test.mockserver.helper;

import com.test.mockserver.objects.OHeader;
import com.test.mockserver.objects.OParameters;
import com.test.mockserver.objects.ORequestBody;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Body;
import org.mockserver.model.Header;
import org.mockserver.model.HttpMessage;
import org.mockserver.model.Parameter;

import java.util.ArrayList;
import java.util.List;

import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.JsonPathBody.jsonPath;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.RegexBody.regex;
import static org.mockserver.model.XPathBody.xpath;
import static org.mockserver.model.XmlBody.xml;

public interface ConvertInternalToMockObjects {

    static List<Parameter> convertOQueryParametersToMockParameters(List<OParameters> oParameters){
        List<Parameter> queryParams = new ArrayList<>();

        for (OParameters queryParm : oParameters) {
            Parameter param = new Parameter(queryParm.getName(), queryParm.getValue());
            queryParams.add(param);
        }
        return queryParams ;
    }

    static List<Header> convertOHeadersToMockHeaders(List<OHeader> oHeaders){
        List<Header> headers = new ArrayList<>();
        for (OHeader header : oHeaders) {
            Header hd = new Header(header.getName(), header.getValue());
            headers.add(hd);
        }
        return headers ;
    }

    @SuppressWarnings("unchecked")
    static <T extends Body> T mockBodyMatching (ORequestBody requestBody) {
        if(("JSON_PATH").equalsIgnoreCase(requestBody.getType())){
            return (T) jsonPath(requestBody.getMatcher());
        } else if (("JSON").equalsIgnoreCase(requestBody.getType())) {
            return (T) json(requestBody.getMatcher(), MatchType.STRICT);
        } else if (("XPATH").equalsIgnoreCase(requestBody.getType())) {
            return (T) xpath(requestBody.getMatcher());
        } else if (("REGEX").equalsIgnoreCase(requestBody.getType())) {
            return (T) regex(requestBody.getMatcher());
        } else if (("XML").equalsIgnoreCase(requestBody.getType())) {
            return (T) xml(requestBody.getMatcher());
        } else if (("PARAMETERS").equalsIgnoreCase(requestBody.getType())) {
            return (T) params(ConvertInternalToMockObjects.convertOQueryParametersToMockParameters(requestBody.getParameters()));
        } else {
            return (T) json(requestBody.getMatcher(), MatchType.STRICT);
        }
    }
}
