package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class AuthenticationParsingTest extends RecipeParserTestBase {
    @Test
    public void parsesRequestWithBasicAuth() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-basic-auth.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.getSelectedAuthProfile(), is("Basic"));
        assertThat(postRequest.getUsername(), is("Steve"));
        assertThat(postRequest.getPassword(), is("greyhole"));
    }

    @Test
    public void parsesRequestWithNTLMAuth() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-ntlm-auth.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.getSelectedAuthProfile(), is("NTLM"));
        assertThat(postRequest.getDomain(), is("PhysicsDep"));
        assertThat(postRequest.getUsername(), is("Steve"));
        assertThat(postRequest.getPassword(), is("greyhole"));
    }

    @Test
    public void parsesRequestWithKerberosAuth() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-kerberos-auth.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.getSelectedAuthProfile(), is("SPNEGO/Kerberos"));
        assertThat(postRequest.getDomain(), is("PhysicsDep"));
        assertThat(postRequest.getUsername(), is("Steve"));
        assertThat(postRequest.getPassword(), is("greyhole"));
    }

    @Test
    public void parsesRequestWithOAuth2() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-oauth2.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.getSelectedAuthProfile(), startsWith("OAuth 2.0 Profile"));
        OAuth2Profile oAuth2Profile = project.getOAuth2ProfileContainer().getOAuth2ProfileList().get(0);
        assertOAuth2WithAccessToken(oAuth2Profile);
    }

    @Test
    public void parsesRequestWithOAuth2RefreshToken() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-oauth2-refreshtoken.json");

        RestTestRequestStep restRequestStep = getSingleRestRequestStepIn(project);
        RestTestRequest postRequest = restRequestStep.getTestRequest();
        assertThat(postRequest.getSelectedAuthProfile(), startsWith("OAuth 2.0"));
        OAuth2Profile oAuth2Profile = project.getOAuth2ProfileContainer().getOAuth2ProfileList().get(0);
        assertOAuth2WithRefreshToken(oAuth2Profile);
    }

    @Ignore
    @Test
    public void reusesEqualOAuth2Profiles() throws Exception {
        WsdlProject project = buildProjectFromRecipe("request-with-multiple-oauth2.json");

        WsdlTestCase testCase = project.getTestSuiteAt(0).getTestCaseAt(0);
        assertThat(((RestTestRequestStep) testCase.getTestStepAt(0)).getTestRequest().getSelectedAuthProfile(), is("OAuth 2.0 Profile 1"));
        assertThat(((RestTestRequestStep) testCase.getTestStepAt(1)).getTestRequest().getSelectedAuthProfile(), is("OAuth 2.0 Profile 2"));
        assertThat(((RestTestRequestStep) testCase.getTestStepAt(2)).getTestRequest().getSelectedAuthProfile(), is("OAuth 2.0 Profile 1"));
        assertOAuth2WithRefreshToken(project.getOAuth2ProfileContainer().getProfileByName("OAuth 2.0 Profile 1"));
        assertOAuth2WithAccessToken(project.getOAuth2ProfileContainer().getProfileByName("OAuth 2.0 Profile 2"));
    }

    private void assertOAuth2WithAccessToken(OAuth2Profile oAuth2Profile) {
        assertThat(oAuth2Profile, is(notNullValue()));
        assertThat(oAuth2Profile.getAccessToken(), is("ya29.8gGoTZvDr3fkRyg0rI8RcwWITJZnrbnjN_J9p39pmkUoq6rSuxnLFpRWhfrcSBO01kVv"));
        assertThat(oAuth2Profile.getAccessTokenPosition(), is(OAuth2Profile.AccessTokenPosition.HEADER));
    }

    private void assertOAuth2WithRefreshToken(OAuth2Profile oAuth2Profile) {
        assertThat(oAuth2Profile, is(notNullValue()));
        assertThat(oAuth2Profile.getAccessTokenPosition(), is(OAuth2Profile.AccessTokenPosition.HEADER));
        assertThat(oAuth2Profile.getAccessTokenURI(), is("https://accounts.google.com/o/oauth2/token"));
        assertThat(oAuth2Profile.getClientID(), is("669184148999-uvb9iqhnaq6h0gju1qhdraf5ds0asaeo.apps.googleusercontent.com"));
        assertThat(oAuth2Profile.getClientSecret(), is("vRl0LylcL1eg4baB7Hcpw5I5"));
        assertThat(oAuth2Profile.getRefreshToken(), is("1/86vz0skA9Qvzo1i_zikBxcJgvw-eZqGVEc_TiS6ST_pIgOrJDtdun6zK6XiATCKT"));
    }
}
