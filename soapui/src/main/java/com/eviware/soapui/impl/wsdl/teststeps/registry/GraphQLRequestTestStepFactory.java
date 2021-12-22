package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.GraphQLTestRequestConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;


public class GraphQLRequestTestStepFactory extends WsdlTestStepFactory {
    public static final String GRAPHQL_TYPE = "graphqltestrequest";

    public GraphQLRequestTestStepFactory() {
        super(GRAPHQL_TYPE, "GraphQL Request", "Submits a GraphQL request and validates its response", "/graphql-request.png");
    }

    @Override
    public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        return new GraphQLRequestTestStep(testCase, config, forLoadTest);
    }

    @Override
    public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name) {
        GraphQLTestRequestConfig graphQLRequestConfig = GraphQLTestRequestConfig.Factory.newInstance();

        TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
        testStepConfig.setType(GRAPHQL_TYPE);
        testStepConfig.setConfig(graphQLRequestConfig);
        testStepConfig.setName(name);

        return testStepConfig;
    }

    public static TestStepConfig createConfig(GraphQLTestRequest request, String stepName) {
        GraphQLTestRequestConfig graphQLRequestConfig = GraphQLTestRequestConfig.Factory.newInstance();
        graphQLRequestConfig.setMethod(request.getMethod().toString());
        graphQLRequestConfig.setEndpoint(request.getEndpoint());
        graphQLRequestConfig.setRequest(request.getConfig().getRequest());

        TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
        testStepConfig.setType(GRAPHQL_TYPE);
        testStepConfig.setConfig(graphQLRequestConfig);
        testStepConfig.setName(stepName);

        return testStepConfig;
    }

    @Override
    public boolean canCreate() {
        return true;
    }
}
