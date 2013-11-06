/*
 *  SoapUI, copyright (C) 2004-2011 eviware.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.wss.crypto;

import com.eviware.soapui.config.KeyMaterialCryptoConfig;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wss.DefaultWssContainer;
import com.eviware.soapui.support.SoapUIException;
import junit.framework.JUnit4TestAdapter;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.security.KeyStore;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Erik R. Yverling
 * 
 *         Tests loading all supported types of cryptos with correct and
 *         incorrect credentials
 */
// FIXME We should also add tests for truststores
public class KeyMaterialWssCryptoTest
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( KeyMaterialWssCryptoTest.class );
	}

	private static final String TEST_RESOURCES_DIR = "/keys/";

	private static final String CORRECT_PASSWORD = "test";
	private static final String INCORRECT_PASSWORD = "not-correct";

	private KeyMaterialWssCrypto crypto;

	@Mock
	private KeyMaterialCryptoConfig configMock;
	@Mock
	private DefaultWssContainer containerMock;
	private WsdlProject project;

	@Before
	public void setUp() throws XmlException, IOException, SoapUIException
	{
		MockitoAnnotations.initMocks( this );
		project = new WsdlProject();
	}

	@Test
	public void testLoadingJKSKeystoreWithCorrectPassword() throws Exception
	{
		loadCryptoSucessfully( CryptoType.KEYSTORE, "jks-test-key.jks", CORRECT_PASSWORD );
	}

	@Test
	public void testLoadingJKSKeystoreWithNullPassword() throws Exception
	{
		loadCryptoSucessfully( CryptoType.TRUSTSTORE, "jks-test-key.jks", null );
	}

	@Test
	public void testLoadingJKSKeystoreWithBlankPassword() throws Exception
	{
		loadCryptoSucessfully( CryptoType.TRUSTSTORE, "jks-test-key.jks", "" );
	}

	@Test
	public void testLoadingJCEKSSKeystoreWithCorrectPassword() throws Exception
	{
		loadCryptoSucessfully( CryptoType.KEYSTORE, "jceks-test-key.jck", CORRECT_PASSWORD );
	}

	@Test
	public void testLoadingPKCS12KeystoreWithCorrectPassword() throws Exception
	{
		loadCryptoSucessfully( CryptoType.KEYSTORE, "pkcs12-test-key.p12", CORRECT_PASSWORD );
	}

	@Test
	public void testLoadingJKSKeystoreWithIncorrectPassword() throws Exception
	{
		loadCryptoUnsucessfully( CryptoType.KEYSTORE, "jks-test-key.jks", INCORRECT_PASSWORD );
	}

	@Test
	public void testLoadingJCEKSSKeystoreWithIncorrectPassword() throws Exception
	{
		loadCryptoUnsucessfully( CryptoType.KEYSTORE, "jceks-test-key.jck", INCORRECT_PASSWORD );
	}

	@Test
	public void testLoadingPKCS12KeystoreWithIncorrectPassword() throws Exception
	{
		loadCryptoUnsucessfully( CryptoType.KEYSTORE, "pkcs12-test-key.p12", INCORRECT_PASSWORD );
	}

	private void loadCryptoSucessfully( CryptoType type, String filename, String password )
	{
		String path = initLoad( type, filename, password );

		try
		{
			KeyStore loadedKeystore = crypto.load();
			assertNotNull( "The crypto (" + path + ") is not null", loadedKeystore );
		}
		catch( Exception e )
		{
			fail( "The crypto (" + path + ") could not be loaded because of: " + e.getMessage() );
		}
	}

	private void loadCryptoUnsucessfully( CryptoType type, String filename, String password )
	{
		String path = initLoad( type, filename, password );

		KeyStore loadedKeystore = null;
		try
		{
			loadedKeystore = crypto.load();
			fail( "The keystore was loaded suessfully" );
		}
		catch( Exception e )
		{
			assertNull( "The crypto (" + path + ") is null", loadedKeystore );
		}
	}

	private String initLoad( CryptoType type, String filename, String password )
	{
		String path = KeyMaterialWssCryptoTest.class.getResource( TEST_RESOURCES_DIR + filename).getPath();

		when( containerMock.getModelItem() ).thenReturn( project );
		when( configMock.getSource() ).thenReturn( path );
		when( configMock.getPassword() ).thenReturn( password );

		crypto = new KeyMaterialWssCrypto( configMock, containerMock, path, password, type );
		return path;
	}
}
