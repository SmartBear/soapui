/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.saml.WSSecSignatureSAML;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.saml.ext.SAMLParms;
import org.w3c.dom.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAML1CallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAML2CallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAMLCallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.support.KeystoresComboBoxModel;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.binding.PresentationModel;

// FIXME Why is the entries named Add* consider changing to a noun
public class AddSAMLEntry extends WssEntryBase
{
	// TODO Some of these should be enums if possible.

	public static final String TYPE = "SAML";

	public static final String SAML_VERSION_1 = "1.1";
	public static final String SAML_VERSION_2 = "2.0";

	public static final String ASSERTION__ASSERTION_TYPE = "Authentication";
	public static final String ATTRIBUTE_ASSERTION_TYPE = "Attribute";

	public static final String HOLDER_OF_KEY_SIGNING_TYPE = "Holder-of-key";

	// FIXME How should be support input for these fields? How are they used?
	private static final String DEFAULT_SUBJECT_NAME = "uid=joe,ou=people,ou=saml-demo,o=example.com";

	private static final String SHA256_DIGEST_ALGORITHM = "http://www.w3.org/2001/04/xmlenc#sha256";
	private static final String RSA_SHA256_SIGNATURE_ALGORITHM = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

	private KeyAliasComboBoxModel keyAliasComboBoxModel;
	private InternalWssContainerListener wssContainerListener;

	private String samlVersion;
	private String assertionType;
	private String signingType;
	private String crypto;
	private String issuer;
	private String subjectName;
	private String subjectQualifier;
	private String digestAlgorithm;
	private String signatureAlgorithm;

	public void init( WSSEntryConfig config, OutgoingWss container )
	{
		super.init( config, container, TYPE );
	}

	// TODO How can we make FindBugs that these fields will always be initialized and be able to add NonNull annotations?
	@Override
	protected void load( XmlObjectConfigurationReader reader )
	{
		// FIXME This seams much better than the inline if-case found in AddSignatureEntry and others. Refactor!
		samlVersion = StringUtils.defaultIfEmpty( reader.readString( "samlVersion", null ), SAML_VERSION_1 );
		assertionType = StringUtils
				.defaultIfEmpty( reader.readString( "assertionType", null ), ASSERTION__ASSERTION_TYPE );
		signingType = StringUtils.defaultIfEmpty( reader.readString( "signingType", null ), HOLDER_OF_KEY_SIGNING_TYPE );
		crypto = reader.readString( "crypto", null );
		issuer = reader.readString( "issuer", null );
		subjectName = StringUtils.defaultIfEmpty( reader.readString( "subjectName", null ), DEFAULT_SUBJECT_NAME );
		subjectQualifier = reader.readString( "subjectQualifier", null );
		digestAlgorithm = StringUtils.defaultIfEmpty( reader.readString( "digestAlgorithm", null ),
				SHA256_DIGEST_ALGORITHM );
		signatureAlgorithm = StringUtils.defaultIfEmpty( reader.readString( "signatureAlgorithm", null ),
				RSA_SHA256_SIGNATURE_ALGORITHM );
	}

	@Override
	protected void save( XmlObjectConfigurationBuilder builder )
	{
		builder.add( "samlVersion", samlVersion );
		builder.add( "assertionType", assertionType );
		builder.add( "signingType", signingType );
		builder.add( "crypto", crypto );
		builder.add( "issuer", issuer );
		builder.add( "subjectName", subjectName );
		builder.add( "subjectQualifier", subjectQualifier );
		builder.add( "digestAlgorithm", digestAlgorithm );
		builder.add( "signatureAlgorithm", signatureAlgorithm );
	}

	@Override
	protected JComponent buildUI()
	{
		wssContainerListener = new InternalWssContainerListener();
		getWssContainer().addWssContainerListener( wssContainerListener );

		SimpleBindingForm form = new SimpleBindingForm( new PresentationModel<AddSignatureEntry>( this ) );
		form.addSpace( 5 );
		form.appendComboBox( "samlVersion", "SAML version", new String[] { SAML_VERSION_1, SAML_VERSION_2 },
				"Choose the SAML version" );
		form.appendComboBox( "assertionType", "Assertion type", new String[] { ASSERTION__ASSERTION_TYPE,
				ATTRIBUTE_ASSERTION_TYPE }, "Choose the type of assertion" );
		form.appendComboBox( "signingType", "Signing type", new String[] { HOLDER_OF_KEY_SIGNING_TYPE },
				"Choose the type of signing" );
		form.appendComboBox( "crypto", "Keystore",
				new KeystoresComboBoxModel( getWssContainer(), getWssContainer().getCryptoByName( crypto ) ),
				"Selects the Keystore containing the key to use for signing the SAML message" ).addItemListener(
				new ItemListener()
				{
					public void itemStateChanged( ItemEvent e )
					{
						// FIXME This cases the drop down to be blank when changing keystore
						keyAliasComboBoxModel.update( getWssContainer().getCryptoByName( crypto ) );
					}
				} );

		// FIXME Why is this called username?
		keyAliasComboBoxModel = new KeyAliasComboBoxModel( getWssContainer().getCryptoByName( crypto ) );
		form.appendComboBox( "username", "Alias", keyAliasComboBoxModel, "The alias for the key to use for encryption" );
		form.appendPasswordField( "password", "Password", "The certificate password" );
		form.appendTextField( "issuer", "Issuer", "The issuer" );
		form.appendTextField( "subjectName", "Subject Name", "The subject qualifier" );
		form.appendTextField( "subjectQualifier", "Subject Qualifier", "The subject qualifier" );
		form.appendComboBox( "digestAlgorithm", "Digest algorithm", new String[] { SHA256_DIGEST_ALGORITHM },
				"Set the digest algorithm" );
		form.appendComboBox( "signatureAlgorithm", "Signature algorithm",
				new String[] { RSA_SHA256_SIGNATURE_ALGORITHM }, "Set the signature algorithm" );

		return new JScrollPane( form.getPanel() );
	}

	public void process( WSSecHeader secHeader, Document doc, PropertyExpansionContext context )
	{
		try
		{
			// FIXME Add a helper method for this since it's used alot
			WssCrypto wssCrypto = getWssContainer().getCryptoByName( crypto );

			if( wssCrypto == null )
			{
				throw new Exception( "Missing crypto [" + crypto + "] for signature entry" );

			}

			SAMLParms samlParms = new SAMLParms();
			SAMLCallbackHandler callbackHandler = null;

			if( samlVersion.equals( SAML_VERSION_1 ) )
			{
				callbackHandler = new SAML1CallbackHandler( wssCrypto.getCrypto(), context.expand( getUsername() ),
						subjectName, subjectQualifier );
			}
			else if( samlVersion.equals( SAML_VERSION_2 ) )
			{
				callbackHandler = new SAML2CallbackHandler( wssCrypto.getCrypto(), context.expand( getUsername() ),
						subjectName, subjectQualifier );
			}

			callbackHandler.setConfirmationMethod( signingType );
			callbackHandler.setIssuer( issuer );
			callbackHandler.setStatement( assertionType );

			samlParms.setCallbackHandler( callbackHandler );

			AssertionWrapper assertion = new AssertionWrapper( samlParms );
			assertion.signAssertion( context.expand( getUsername() ), context.expand( getPassword() ),
					wssCrypto.getCrypto(), false );

			WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
			wsSign.setUserInfo( context.expand( getUsername() ), context.expand( getPassword() ) );
			wsSign.setDigestAlgo( digestAlgorithm );

			// Assertion type

			if( assertionType.equals( ASSERTION__ASSERTION_TYPE ) )
			{
				wsSign.setSignatureAlgorithm( signatureAlgorithm );
				wsSign.setKeyIdentifierType( WSConstants.BST_DIRECT_REFERENCE );
			}
			else if( assertionType.equals( ATTRIBUTE_ASSERTION_TYPE ) )
			{
				wsSign.setKeyIdentifierType( WSConstants.X509_KEY_IDENTIFIER );
				wsSign.setSignatureAlgorithm( WSConstants.HMAC_SHA256 );

				byte[] ephemeralKey = callbackHandler.getEphemeralKey();
				wsSign.setSecretKey( ephemeralKey );
			}

			wsSign.build( doc, wssCrypto.getCrypto(), assertion, null, null, null, secHeader );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public void relase()
	{
		if( wssContainerListener != null )
		{
			getWssContainer().removeWssContainerListener( wssContainerListener );
		}
	}

	@Override
	protected void addPropertyExpansions( PropertyExpansionsResult result )
	{
		super.addPropertyExpansions( result );
		result.extractAndAddAll( "samlAssertion" );
	}

	public String getSamlVersion()
	{
		return samlVersion;
	}

	public void setSamlVersion( String samlVersion )
	{
		this.samlVersion = samlVersion;
		saveConfig();
	}

	public String getAssertionType()
	{
		return assertionType;
	}

	public void setAssertionType( String assertionType )
	{
		this.assertionType = assertionType;
		saveConfig();
	}

	public String getSigningType()
	{
		return signingType;
	}

	public void setSigningType( String signingType )
	{
		this.signingType = signingType;
		saveConfig();
	}

	public String getIssuer()
	{
		return issuer;
	}

	public void setIssuer( String issuer )
	{
		this.issuer = issuer;
		saveConfig();
	}

	public String getCrypto()
	{
		return crypto;
	}

	public void setCrypto( String crypto )
	{
		this.crypto = crypto;
		saveConfig();
	}

	public String getSubjectName()
	{
		return subjectName;
	}

	public void setSubjectName( String subjectName )
	{
		this.subjectName = subjectName;
		saveConfig();
	}

	public String getSubjectQualifier()
	{
		return subjectQualifier;
	}

	public void setSubjectQualifier( String subjectQualifier )
	{
		this.subjectQualifier = subjectQualifier;
		saveConfig();
	}

	public String getDigestAlgorithm()
	{
		return digestAlgorithm;
	}

	public void setDigestAlgorithm( String digestAlgorithm )
	{
		this.digestAlgorithm = digestAlgorithm;
		saveConfig();
	}

	public String getSignatureAlgorithm()
	{
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm( String signatureAlgorithm )
	{
		this.signatureAlgorithm = signatureAlgorithm;
		saveConfig();
	}

	private final class InternalWssContainerListener extends WssContainerListenerAdapter
	{
		@Override
		public void cryptoUpdated( WssCrypto crypto )
		{
			if( crypto.getLabel().equals( getCrypto() ) )
				keyAliasComboBoxModel.update( crypto );
		}
	}
}