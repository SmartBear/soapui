package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import org.apache.http.auth.AuthScheme;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.auth.NegotiateScheme;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Anders Jaensson
 */
public class HttpAuthenticationRequestFilterTest
{
	private HttpAuthenticationRequestFilter filter;
	private WsdlRequest wsdlRequest;

	@Before
	public void setup(){
		filter = new HttpAuthenticationRequestFilter();
		wsdlRequest = Mockito.mock( WsdlRequest.class );
		when( wsdlRequest.getUsername() ).thenReturn( "Uwe" );
		when( wsdlRequest.getWssPasswordType() ).thenReturn( WsdlRequest.PW_TYPE_NONE );
	}

	@Test
	public void selectingAuthTypeNtlmReturnsNtlmEvenIfSpnegoRequested()
	{
		selectAuthMethod( CredentialsConfig.AuthType.NTLM );

		filter.filterAbstractHttpRequest( null, wsdlRequest );

		AuthScheme scheme = getSchemeFor( AuthPolicy.SPNEGO );
		assertThat( scheme, instanceOf( NTLMScheme.class ) );
	}

	@Test
	public void selectingAuthTypeNtlmReturnsNtlmIfNtlmRequested()
	{
		selectAuthMethod( CredentialsConfig.AuthType.NTLM );

		filter.filterAbstractHttpRequest( null, wsdlRequest );

		AuthScheme scheme = getSchemeFor( AuthPolicy.NTLM );
		assertThat( scheme, instanceOf( NTLMScheme.class ) );
	}

	@Test
	public void selectingAuthTypeSpnegoReturnsSpnegoIfSpnegoRequested()
	{
		selectAuthMethod( CredentialsConfig.AuthType.SPNEGO_KERBEROS );

		filter.filterAbstractHttpRequest( null, wsdlRequest );

		AuthScheme scheme = getSchemeFor( AuthPolicy.SPNEGO );
		assertThat( scheme, instanceOf( NegotiateScheme.class ) );
	}

	@Test
	public void selectingAuthTypeSpnegoReturnsNtlmIfNtlmRequested()
	{
		selectAuthMethod( CredentialsConfig.AuthType.SPNEGO_KERBEROS );

		filter.filterAbstractHttpRequest( null, wsdlRequest );

		AuthScheme scheme = getSchemeFor( AuthPolicy.NTLM );
		assertThat( scheme, instanceOf( NTLMScheme.class ) );
	}

	private AuthScheme getSchemeFor( String schemeName )
	{
		return HttpClientSupport.getHttpClient().getAuthSchemes().getAuthScheme( schemeName, null );
	}

	private void selectAuthMethod( CredentialsConfig.AuthType.Enum authType )
	{
		when( wsdlRequest.getAuthType() ).thenReturn(authType.toString() );
	}
}
