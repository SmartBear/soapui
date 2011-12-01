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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSAMLToken;
import org.apache.ws.security.saml.WSSecSignatureSAML;
import org.apache.ws.security.saml.ext.AssertionWrapper;
import org.apache.ws.security.saml.ext.SAMLParms;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAML1CallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAML2CallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.SAMLCallbackHandler;
import com.eviware.soapui.impl.wsdl.support.wss.support.KeystoresComboBoxModel;
import com.eviware.soapui.impl.wsdl.support.wss.support.SAMLAttributeValuesTable;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.types.StringToStringMap;
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

	public static final String AUTHENTICATION_ASSERTION_TYPE = "Authentication";
	public static final String ATTRIBUTE_ASSERTION_TYPE = "Attribute";
	public static final String AUTHORIZATION_ASSERTION_TYPE = "Authorization";

	public static final String ATTRIBUTE_VALUES_VALUE_COLUMN = "value";

	public static final String HOLDER_OF_KEY_CONFIRMATION_METHOD = "Holder-of-key";
	public static final String SENDER_VOUCHES_CONFIRMATION_METHOD = "Sender vouches";

	// FIXME How should be support input for these fields? How are they used?
	private static final String DEFAULT_SUBJECT_NAME = "uid=joe,ou=people,ou=saml-demo,o=example.com";

	private KeyAliasComboBoxModel keyAliasComboBoxModel;
	private InternalWssContainerListener wssContainerListener;

	private String samlVersion;
	private String assertionType;
	private String confirmationMethod;
	private String crypto;
	private String issuer;
	private String subjectName;
	private String subjectQualifier;
	private String digestAlgorithm;
	private String signatureAlgorithm;
	private boolean signed;
	private String attributeName;
	private List<StringToStringMap> attributeValues;

	public void init( WSSEntryConfig config, OutgoingWss container )
	{
		super.init( config, container, TYPE );
	}

	// TODO How can we make FindBugs that these fields will always be initialized and be able to add NonNull annotations?
	@Override
	protected void load( XmlObjectConfigurationReader reader )
	{
		samlVersion = reader.readString( "samlVersion", SAML_VERSION_1 );
		signed = reader.readBoolean( "signed", false );
		assertionType = reader.readString( "assertionType", AUTHENTICATION_ASSERTION_TYPE );
		confirmationMethod = reader.readString( "confirmationMethod", SENDER_VOUCHES_CONFIRMATION_METHOD );
		crypto = reader.readString( "crypto", null );
		issuer = reader.readString( "issuer", null );
		subjectName = reader.readString( "subjectName", DEFAULT_SUBJECT_NAME );
		subjectQualifier = reader.readString( "subjectQualifier", null );
		digestAlgorithm = reader.readString( "digestAlgorithm", null );
		signatureAlgorithm = reader.readString( "signatureAlgorithm", null );
		attributeName = reader.readString( "attributeName", null );
		attributeValues = readTableValues( reader, "attributeValues" );
	}

	@Override
	protected void save( XmlObjectConfigurationBuilder builder )
	{
		builder.add( "samlVersion", samlVersion );
		builder.add( "signed", signed );
		builder.add( "assertionType", assertionType );
		builder.add( "confirmationMethod", confirmationMethod );
		builder.add( "crypto", crypto );
		builder.add( "issuer", issuer );
		builder.add( "subjectName", subjectName );
		builder.add( "subjectQualifier", subjectQualifier );
		builder.add( "digestAlgorithm", digestAlgorithm );
		builder.add( "signatureAlgorithm", signatureAlgorithm );
		builder.add( "attributeName", attributeName );
		saveTableValues( builder, attributeValues, "attributeValues" );
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
		form.appendCheckBox( "signed", "Signed", null );
		form.appendComboBox( "assertionType", "Assertion type", new String[] { AUTHENTICATION_ASSERTION_TYPE,
				ATTRIBUTE_ASSERTION_TYPE, AUTHORIZATION_ASSERTION_TYPE }, "Choose the type of assertion" );
		form.appendComboBox( "confirmationMethod", "Confirmation method", new String[] {
				SENDER_VOUCHES_CONFIRMATION_METHOD, HOLDER_OF_KEY_CONFIRMATION_METHOD }, "Choose the confirmation method" );
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
		form.appendComboBox( "digestAlgorithm", "Digest Algorithm", new String[] {
				MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256,
				MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA384, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512 },
				"Set the digest algorithm to use" );
		form.appendComboBox( "signatureAlgorithm", "Signature Algorithm", new String[] { WSConstants.RSA,
				WSConstants.DSA, XMLSignature.ALGO_ID_MAC_HMAC_SHA1, XMLSignature.ALGO_ID_MAC_HMAC_SHA256,
				XMLSignature.ALGO_ID_MAC_HMAC_SHA384, XMLSignature.ALGO_ID_MAC_HMAC_SHA512 },
				"Set the name of the signature encryption algorithm to use" );
		form.appendTextField( "attributeName", "Attribute name", "The name of the attribute" );
		form.append( "Attribute names", new SAMLAttributeValuesTable( attributeValues, this ) );

		return new JScrollPane( form.getPanel() );
	}

	public void process( WSSecHeader secHeader, Document doc, PropertyExpansionContext context )
	{
		try
		{
			SAMLParms samlParms = new SAMLParms();
			SAMLCallbackHandler callbackHandler = null;

			if( !signed )
			{
				WSSecSAMLToken wsSecSAMLToken = new WSSecSAMLToken();

				if( samlVersion.equals( SAML_VERSION_1 ) )
				{
					callbackHandler = new SAML1CallbackHandler( context.expand( subjectName ),
							context.expand( subjectQualifier ) );
				}
				else if( samlVersion.equals( SAML_VERSION_2 ) )
				{
					callbackHandler = new SAML2CallbackHandler( context.expand( subjectName ),
							context.expand( subjectQualifier ) );
				}
				AssertionWrapper assertion = createAssertion( context, samlParms, callbackHandler );
				wsSecSAMLToken.build( doc, assertion, secHeader );
			}
			else
			{
				WSSecSignatureSAML wsSecSignatureSAML = new WSSecSignatureSAML();
				// FIXME Add a helper method for this since it's used alot
				WssCrypto wssCrypto = getWssContainer().getCryptoByName( crypto );

				if( wssCrypto == null )
				{
					throw new Exception( "Missing crypto [" + crypto + "] for signature entry" );
				}

				if( samlVersion.equals( SAML_VERSION_1 ) )
				{
					callbackHandler = new SAML1CallbackHandler( wssCrypto.getCrypto(), context.expand( getUsername() ),
							context.expand( subjectName ), context.expand( subjectQualifier ) );
				}
				else if( samlVersion.equals( SAML_VERSION_2 ) )
				{
					callbackHandler = new SAML2CallbackHandler( wssCrypto.getCrypto(), context.expand( getUsername() ),
							context.expand( subjectName ), context.expand( subjectQualifier ) );
				}

				AssertionWrapper assertion = createAssertion( context, samlParms, callbackHandler );

				assertion.signAssertion( context.expand( getUsername() ), context.expand( getPassword() ),
						wssCrypto.getCrypto(), false );

				wsSecSignatureSAML.setUserInfo( context.expand( getUsername() ), context.expand( getPassword() ) );

				if( confirmationMethod.equals( SENDER_VOUCHES_CONFIRMATION_METHOD ) )
				{
					wsSecSignatureSAML.setKeyIdentifierType( WSConstants.BST_DIRECT_REFERENCE );

					wsSecSignatureSAML.build( doc, null, assertion, wssCrypto.getCrypto(), context.expand( getUsername() ),
							context.expand( getPassword() ), secHeader );
				}
				else if( confirmationMethod.equals( HOLDER_OF_KEY_CONFIRMATION_METHOD ) )
				{
					wsSecSignatureSAML.setDigestAlgo( digestAlgorithm );

					if( assertionType.equals( AUTHENTICATION_ASSERTION_TYPE ) )
					{
						wsSecSignatureSAML.setKeyIdentifierType( WSConstants.BST_DIRECT_REFERENCE );
						wsSecSignatureSAML.setSignatureAlgorithm( signatureAlgorithm );
					}
					else if( assertionType.equals( ATTRIBUTE_ASSERTION_TYPE ) )
					{

						wsSecSignatureSAML.setKeyIdentifierType( WSConstants.X509_KEY_IDENTIFIER );
						wsSecSignatureSAML.setSignatureAlgorithm( signatureAlgorithm );

						byte[] ephemeralKey = callbackHandler.getEphemeralKey();
						wsSecSignatureSAML.setSecretKey( ephemeralKey );
					}

					wsSecSignatureSAML.build( doc, wssCrypto.getCrypto(), assertion, null, null, null, secHeader );
				}
			}

		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	private AssertionWrapper createAssertion( PropertyExpansionContext context, SAMLParms samlParms,
			SAMLCallbackHandler callbackHandler ) throws WSSecurityException
	{
		if( assertionType.equals( ATTRIBUTE_ASSERTION_TYPE ) )
		{
			callbackHandler.setCustomAttributeName( attributeName );
			callbackHandler.setCustomAttributeValues( extractValueColumnValues( attributeValues, context ) );
		}

		callbackHandler.setConfirmationMethod( confirmationMethod );
		callbackHandler.setIssuer( issuer );
		callbackHandler.setStatement( assertionType );

		samlParms.setCallbackHandler( callbackHandler );
		return new AssertionWrapper( samlParms );
	}

	// Since we only use one column for the attribute values
	private List<String> extractValueColumnValues( List<StringToStringMap> table, PropertyExpansionContext context )
	{
		List<String> firstColumnValues = new ArrayList<String>();
		for( StringToStringMap row : table )
		{
			String columnValue = row.get( ATTRIBUTE_VALUES_VALUE_COLUMN );
			// TODO Add property expansion to each value
			firstColumnValues.add( columnValue );
		}
		return firstColumnValues;
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
		result.extractAndAddAll( this, "issuer" );
		result.extractAndAddAll( this, "subjectName" );
		result.extractAndAddAll( this, "subjectQualifier" );
		result.extractAndAddAll( this, "attributeName" );
		// TODO Add property expansion refactoring for attributesValues, as with HttpTestRequestStep
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

	public String getConfirmationMethod()
	{
		return confirmationMethod;
	}

	public void setConfirmationMethod( String confirmationMethod )
	{
		this.confirmationMethod = confirmationMethod;
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

	public boolean isSigned()
	{
		return signed;
	}

	public void setSigned( boolean signed )
	{
		this.signed = signed;
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	public void setAttributeName( String attributeName )
	{
		this.attributeName = attributeName;
	}

	public List<StringToStringMap> getAttributeValues()
	{
		return attributeValues;
	}

	public void setAttributeValues( List<StringToStringMap> attributeValues )
	{
		this.attributeValues = attributeValues;
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