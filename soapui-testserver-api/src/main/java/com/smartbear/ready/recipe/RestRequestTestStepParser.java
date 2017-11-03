package com.smartbear.ready.recipe;

import com.eviware.soapui.config.AccessTokenPositionConfig;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.actions.RestServiceBuilder;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.smartbear.ready.recipe.teststeps.AuthenticationStruct;
import com.smartbear.ready.recipe.teststeps.ParamStruct;
import com.smartbear.ready.recipe.teststeps.RestTestRequestStepStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.util.UUID;

import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import static com.smartbear.ready.recipe.TestStepNames.createUniqueName;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Parses a JSON Object describing a REST Request test step.
 */
class RestRequestTestStepParser extends HttpRequestTestStepParser {

    public void createTestStep(WsdlTestCase testCase, TestStepStruct testStepStruct, StringToObjectMap context) {
        RestTestRequestStepStruct requestTestStepElement = (RestTestRequestStepStruct) testStepStruct;
        String requestUri = requestTestStepElement.URI;
        HttpMethod httpMethod = getHttpMethod(requestTestStepElement);

        RestRequest restRequest;
        try {
            restRequest = new RestServiceBuilder().createRestServiceHeadlessFromUri(
                    (WsdlProject) testCase.getProject(), new RestServiceBuilder.RequestInfo(requestUri, httpMethod),
                    RestServiceBuilder.ModelCreationStrategy.CREATE_NEW_MODEL);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create REST service for " + requestUri, e);
        }

        restRequest.setMethod(httpMethod);
        String testStepName = createUniqueName(testCase, testStepStruct.name, createRequestName(httpMethod, testCase));

        RestTestRequestStep requestTestStep = (RestTestRequestStep) testCase.addTestStep(
                RestRequestStepFactory.createConfig(restRequest, testStepName));
        RestTestRequest testRequest = requestTestStep.getTestRequest();
        if (StringUtils.isNotEmpty(requestTestStepElement.clientCertificateFileName)) {
            testRequest.setSslKeystore(requestTestStepElement.clientCertificateFileName);
        }

        addParameters(requestTestStepElement, testRequest);
        addHeaders(requestTestStepElement, testRequest);
        addAuthentication(requestTestStepElement, testRequest);
        addProperties(requestTestStepElement, testRequest);
        addAssertions(requestTestStepElement, testRequest);
        addAttachments(requestTestStepElement, testRequest);
    }

    private String createRequestName(HttpMethod httpMethod, WsdlTestCase testCase) {
        return ModelItemNamer.createName(httpMethod + " request", testCase.getTestStepList());
    }

    private HttpMethod getHttpMethod(RestTestRequestStepStruct testStepStruct) {
        return testStepStruct.method == null ? HttpMethod.GET : HttpMethod.valueOf(testStepStruct.method);
    }

    private void addProperties(RestTestRequestStepStruct testStepStruct, RestTestRequest testRequest) {

        super.addProperties(testStepStruct, testRequest);

        if (testStepStruct.requestBody != null) {
            testRequest.setRequestContent(testStepStruct.requestBody);
        }

        if (testStepStruct.mediaType != null) {
            testRequest.setMediaType(testStepStruct.mediaType);
        }

        testRequest.setPostQueryString(testStepStruct.postQueryString);
    }

    private void addParameters(RestTestRequestStepStruct testStepStruct, RestTestRequest testRequest) {
        ParamStruct[] parameterArray = testStepStruct.parameters;
        if (parameterArray != null) {
            for (ParamStruct parameter : parameterArray) {
                RestParamProperty restParamProperty = testRequest.getRestMethod().addProperty(parameter.name);
                testRequest.getParams().getProperty(parameter.name).setValue(parameter.value);
                restParamProperty.setValue(parameter.value);
                String parameterTypeName = parameter.type;
                if (parameterTypeName == null) {
                    restParamProperty.setStyle(ParameterStyle.QUERY);
                } else if (parameterTypeName.equals("PATH")) {
                    restParamProperty.setStyle(ParameterStyle.TEMPLATE);
                } else {
                    restParamProperty.setStyle(ParameterStyle.valueOf(parameterTypeName));
                }
            }
        }
    }

    private void addAuthentication(RestTestRequestStepStruct testStepStruct, RestTestRequest testRequest) {
        AuthenticationStruct authentication = testStepStruct.authentication;
        if (authentication != null) {
            String authenticationType = authentication.type;
            if (authenticationType.equals(CredentialsConfig.AuthType.O_AUTH_2_0.toString())) {
                addOAuth2Authentication(testRequest, authentication);
            } else {
                testRequest.setSelectedAuthProfileAndAuthType(authenticationType, CredentialsConfig.AuthType.Enum.forString(authenticationType));
                if (authenticationType.equals(CredentialsConfig.AuthType.NTLM.toString()) ||
                        authenticationType.equals(CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString())) {
                    testRequest.setDomain(authentication.domain);
                }
                testRequest.setUsername(authentication.username);
                testRequest.setPassword(authentication.password);
            }
        }
    }

    private void addOAuth2Authentication(RestTestRequest testRequest, AuthenticationStruct authentication) {
        OAuth2Profile existingEqualOAuth2Profile = findExistingEqualOAuth2Profile((WsdlProject) testRequest.getProject(), authentication);
        if (existingEqualOAuth2Profile == null) {
            String profileName = "OAuth 2.0 Profile " + UUID.randomUUID();
            testRequest.setSelectedAuthProfileAndAuthType(profileName, CredentialsConfig.AuthType.O_AUTH_2_0);
            OAuth2Profile oAuth2Profile = testRequest.getProject().getOAuth2ProfileContainer().addNewOAuth2Profile(profileName);
            OAuth2Profile.AccessTokenPosition accessTokenPosition = extractAccessTokenPosition(authentication);
            if (accessTokenPosition != null) {
                oAuth2Profile.setAccessTokenPosition(accessTokenPosition);
            }
            if (authentication.accessToken != null) {
                oAuth2Profile.setAccessToken(authentication.accessToken);
            } else if (authentication.refreshToken != null) {
                setRefreshTokenData(oAuth2Profile, authentication);
            }
        } else {
            testRequest.setSelectedAuthProfileAndAuthType(existingEqualOAuth2Profile.getName(), CredentialsConfig.AuthType.O_AUTH_2_0);
        }
    }

    private OAuth2Profile.AccessTokenPosition extractAccessTokenPosition(AuthenticationStruct authentication) {
        for (OAuth2Profile.AccessTokenPosition accessTokenPosition : OAuth2Profile.AccessTokenPosition.values()) {
            if (accessTokenPosition.toString().equals(authentication.accessTokenPosition)) {
                return accessTokenPosition;
            }
        }
        return null;
    }

    private void setRefreshTokenData(OAuth2Profile oAuth2Profile, AuthenticationStruct authentication) {
        oAuth2Profile.setAccessTokenURI(authentication.accessTokenUri);
        oAuth2Profile.setClientID(authentication.clientId);
        oAuth2Profile.setClientSecret(authentication.clientSecret);
        oAuth2Profile.setRefreshToken(authentication.refreshToken);

        oAuth2Profile.setAccessToken("-");
        oAuth2Profile.setAccessTokenIssuedTime(1);
        oAuth2Profile.setAccessTokenExpirationTime(1);
    }

    private OAuth2Profile findExistingEqualOAuth2Profile(WsdlProject project, AuthenticationStruct authentication) {
        for (OAuth2Profile oAuth2Profile : project.getOAuth2ProfileContainer().getOAuth2ProfileList()) {
            if (StringUtils.equals(defaultString(authentication.accessToken, "-"), oAuth2Profile.getAccessToken())
                    && StringUtils.equals(authentication.accessTokenUri, oAuth2Profile.getAccessTokenURI())
                    && StringUtils.equals(authentication.clientId, oAuth2Profile.getClientID())
                    && StringUtils.equals(authentication.clientSecret, oAuth2Profile.getClientSecret())
                    && StringUtils.equals(authentication.refreshToken, oAuth2Profile.getRefreshToken())
                    && StringUtils.equals(defaultString(authentication.accessTokenPosition, AccessTokenPositionConfig.HEADER.toString()), oAuth2Profile.getAccessTokenPosition().toString())
                    ) {
                return oAuth2Profile;
            }
        }
        return null;
    }
}
