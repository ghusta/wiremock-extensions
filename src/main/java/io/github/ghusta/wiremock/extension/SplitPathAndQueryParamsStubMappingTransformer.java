package io.github.ghusta.wiremock.extension;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.havingExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;

/**
 * Stub with query parameters split from request url.
 * <p>
 * Using : {@code request.urlPath} and {@code request.queryParameters}.
 * <br>
 * Will use {@code request.queryParameters...hasExactly} if multiple values for a query parameter.
 */
public class SplitPathAndQueryParamsStubMappingTransformer extends StubMappingTransformer {

    private boolean parametersMatchAnything = false;

    public SplitPathAndQueryParamsStubMappingTransformer() {
    }

    public SplitPathAndQueryParamsStubMappingTransformer(boolean parametersMatchAnything) {
        this.parametersMatchAnything = parametersMatchAnything;
    }

    @Override
    public StubMapping transform(StubMapping stubMapping, FileSource files, Parameters parameters) {
        String path = Urls.getPath(stubMapping.getRequest().getUrl());
        Map<String, QueryParameter> queryParameterMap = Urls.splitQueryFromUrl(stubMapping.getRequest().getUrl());

        MappingBuilder mappingBuilder = WireMock.get(WireMock.urlPathEqualTo(path))
                .willReturn(ResponseDefinitionBuilder.like(stubMapping.getResponse()));

        queryParameterMap.forEach((key, queryParameter) ->
                {
                    if (parametersMatchAnything) {
                        mappingBuilder.withQueryParam(key, matching("^(.*)$"));
                    } else {
                        if (queryParameter.isSingleValued()) {
                            mappingBuilder.withQueryParam(key, equalTo(queryParameter.firstValue()));
                        } else {
                            // https://wiremock.org/docs/request-matching/#matching-headerquery-parameter-containing-multiple-values
                            MultiValuePattern havingExactly = havingExactly(queryParameter.values().stream()
                                    .map(WireMock::equalTo)
                                    .toArray(StringValuePattern[]::new));
                            mappingBuilder.withQueryParam(key, havingExactly);
                        }
                    }
                }
        );

        return mappingBuilder.build();
    }

    @Override
    public String getName() {
        return "split-path-query-params";
    }
}
