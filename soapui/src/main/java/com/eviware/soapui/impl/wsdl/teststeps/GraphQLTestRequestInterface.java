package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.GraphQLTestRequestConfig;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;

public interface GraphQLTestRequestInterface extends HttpTestRequestInterface<GraphQLTestRequestConfig>,
        HttpRequestInterface<GraphQLTestRequestConfig> {
    String getVariables();

    void setVariables(String variables);

    String getQuery();

    void setQuery(String query);
}
