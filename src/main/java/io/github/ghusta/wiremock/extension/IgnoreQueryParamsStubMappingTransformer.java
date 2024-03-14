package io.github.ghusta.wiremock.extension;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * Stub will ignore request query parameters.
 * <p>
 * Using : {@code request.urlPath}.
 * <p>
 * Documentation :
 * <ul>
 *     <li><a href="https://wiremock.org/docs/extending-wiremock/">Extensions</a></li>
 *     <li><a href="https://stackoverflow.com/questions/70977265/wiremock-customize-recording-and-playback">Wiremock customize recording and playback</a></li>
 * </ul>
 */
public class IgnoreQueryParamsStubMappingTransformer extends StubMappingTransformer {

    @Override
    public StubMapping transform(StubMapping stubMapping, FileSource files, Parameters parameters) {
        String path = Urls.getPath(stubMapping.getRequest().getUrl());

        // query parameters are omitted
        return WireMock.get(WireMock.urlPathEqualTo(path))
                .willReturn(ResponseDefinitionBuilder.like(stubMapping.getResponse()))
                .build();
    }

    @Override
    public String getName() {
        return "ignore-query-params";
    }
}
