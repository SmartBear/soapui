/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
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
import java.io.StringWriter;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.support.wss.support.KeystoresComboBoxModel;
import com.eviware.soapui.impl.wsdl.support.wss.support.WSPartsTable;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jgoodies.binding.PresentationModel;

public class AddEncryptionEntry extends WssEntryBase
{
	private static final String DEFAULT_OPTION = "<default>";
	public static final String TYPE = "Encryption";
	private String crypto;
	private int keyIdentifierType;
	private String symmetricEncAlgorithm;
	private String encKeyTransport;
	private List<StringToStringMap> parts;
	private String embeddedKeyName;
	private String embeddedKeyPassword;
	private String encryptionCanonicalization;
	private JTextField embeddedKeyNameTextField;
	private JTextField embeddedKeyNamePassword;
	private boolean encryptSymmetricKey;
	private KeyAliasComboBoxModel keyAliasComboBoxModel;
	private InternalWssContainerListener wssContainerListener;

	public void init( WSSEntryConfig config, OutgoingWss container )
	{
		super.init( config, container, TYPE );
	}

	@Override
	protected JComponent buildUI()
	{
		SimpleBindingForm form = new SimpleBindingForm( new PresentationModel<AddSignatureEntry>( this ) );

		form.addSpace( 5 );
		wssContainerListener = new InternalWssContainerListener();
		getWssContainer().addWssContainerListener( wssContainerListener );

		KeystoresComboBoxModel keystoresComboBoxModel = new KeystoresComboBoxModel( getWssContainer(), getWssContainer()
				.getCryptoByName( crypto ) );
		form.appendComboBox( "crypto", "Keystore", keystoresComboBoxModel,
				"Selects the Keystore containing the key to use for signing" ).addItemListener( new ItemListener()
		{

			public void itemStateChanged( ItemEvent e )
			{
				keyAliasComboBoxModel.update( getWssContainer().getCryptoByName( crypto ) );
			}
		} );

		keyAliasComboBoxModel = new KeyAliasComboBoxModel( getWssContainer().getCryptoByName( crypto ) );
		form.appendComboBox( "username", "Alias", keyAliasComboBoxModel, "The alias for the key to use for encryption" );

		form.appendPasswordField( "password", "Password",
				"The password for the key to use for encryption (if it is private)" );

		form.appendComboBox( "keyIdentifierType", "Key Identifier Type", new Integer[] { 0, 1, 2, 3, 4, 5, 6, 8 },
				"Sets which key identifier to use" ).setRenderer( new KeyIdentifierTypeRenderer() );

		( embeddedKeyNameTextField = form.appendTextField( "embeddedKeyName", "Embedded Key Name",
				"The embedded key name" ) ).setEnabled( keyIdentifierType == WSConstants.EMBEDDED_KEYNAME );
		( embeddedKeyNamePassword = form.appendPasswordField( "embeddedKeyPassword", "Embedded Key Password",
				"The embedded key password" ) ).setEnabled( keyIdentifierType == WSConstants.EMBEDDED_KEYNAME );

		form.appendComboBox( "symmetricEncAlgorithm", "Symmetric Encoding Algorithm", new String[] { DEFAULT_OPTION,
				WSConstants.AES_128, WSConstants.AES_192, WSConstants.AES_256, WSConstants.TRIPLE_DES },
				"Set the name of the symmetric encryption algorithm to use" );

		form.appendComboBox( "encKeyTransport", "Key Encryption Algorithm", new String[] { DEFAULT_OPTION,
				WSConstants.KEYTRANSPORT_RSA15, WSConstants.KEYTRANSPORT_RSAOEP },
				"Sets the algorithm to encode the symmetric key" );

		form.appendComboBox( "encryptionCanonicalization", "Encryption Canonicalization", new String[] { DEFAULT_OPTION,
				WSConstants.C14N_OMIT_COMMENTS, WSConstants.C14N_WITH_COMMENTS, WSConstants.C14N_EXCL_OMIT_COMMENTS,
				WSConstants.C14N_EXCL_WITH_COMMENTS },
				"Set the name of an optional canonicalization algorithm to use before encryption" );

		form.appendCheckBox( "encryptSymmetricKey", "Create Encrypted Key",
				"Indicates whether to encrypt the symmetric key into an EncryptedKey or not" );

		form.append( "Parts", new WSPartsTable( parts, this ) );

		return new JScrollPane( form.getPanel() );
	}

	public void release()
	{
		if( wssContainerListener != null )
			getWssContainer().removeWssContainerListener( wssContainerListener );
	}

	@Override
	protected void load( XmlObjectConfigurationReader reader )
	{
		crypto = reader.readString( "crypto", null );
		keyIdentifierType = reader.readInt( "keyIdentifierType", 0 );
		symmetricEncAlgorithm = reader.readString( "symmetricEncAlgorithm", null );
		encKeyTransport = reader.readString( "encKeyTransport", null );
		embeddedKeyName = reader.readString( "embeddedKeyName", null );
		embeddedKeyPassword = reader.readString( "embeddedKeyPassword", null );
		encryptionCanonicalization = reader.readString( "encryptionCanonicalization", null );
		encryptSymmetricKey = reader.readBoolean( "encryptSymmetricKey", true );
		parts = readParts( reader, "encryptionPart" );
	}

	@Override
	protected void save( XmlObjectConfigurationBuilder builder )
	{
		builder.add( "crypto", crypto );
		builder.add( "keyIdentifierType", keyIdentifierType );
		builder.add( "symmetricEncAlgorithm", symmetricEncAlgorithm );
		builder.add( "encKeyTransport", encKeyTransport );
		builder.add( "embeddedKeyName", embeddedKeyName );
		builder.add( "embeddedKeyPassword", embeddedKeyPassword );
		builder.add( "encryptionCanonicalization", encryptionCanonicalization );
		builder.add( "encryptSymmetricKey", encryptSymmetricKey );
		saveParts( builder, parts, "encryptionPart" );
	}

	public String getEmbeddedKeyName()
	{
		return embeddedKeyName;
	}

	public void setEmbeddedKeyName( String embeddedKeyName )
	{
		this.embeddedKeyName = embeddedKeyName;
		saveConfig();
	}

	public String getEmbeddedKeyPassword()
	{
		return embeddedKeyPassword;
	}

	public void setEmbeddedKeyPassword( String embeddedKeyPassword )
	{
		this.embeddedKeyPassword = embeddedKeyPassword;
		saveConfig();
	}

	public String getEncKeyTransport()
	{
		return StringUtils.isNullOrEmpty( encKeyTransport ) ? DEFAULT_OPTION : encKeyTransport;
	}

	public void setEncKeyTransport( String encKeyTransport )
	{
		if( DEFAULT_OPTION.equals( encKeyTransport ) )
			encKeyTransport = null;

		this.encKeyTransport = encKeyTransport;
		saveConfig();
	}

	public String getEncryptionCanonicalization()
	{
		return StringUtils.isNullOrEmpty( encryptionCanonicalization ) ? DEFAULT_OPTION : encryptionCanonicalization;
	}

	public void setEncryptionCanonicalization( String encryptionCanonicalization )
	{
		if( DEFAULT_OPTION.equals( encryptionCanonicalization ) )
			encryptionCanonicalization = null;

		this.encryptionCanonicalization = encryptionCanonicalization;
		saveConfig();
	}

	public boolean isEncryptSymmetricKey()
	{
		return encryptSymmetricKey;
	}

	public void setEncryptSymmetricKey( boolean encryptSymmetricKey )
	{
		this.encryptSymmetricKey = encryptSymmetricKey;
		saveConfig();
	}

	public int getKeyIdentifierType()
	{
		return keyIdentifierType;
	}

	public void setKeyIdentifierType( int keyIdentifierType )
	{
		this.keyIdentifierType = keyIdentifierType;

		if( embeddedKeyNameTextField != null )
		{
			embeddedKeyNameTextField.setEnabled( keyIdentifierType == WSConstants.EMBEDDED_KEYNAME );
			embeddedKeyNamePassword.setEnabled( keyIdentifierType == WSConstants.EMBEDDED_KEYNAME );
		}
		saveConfig();
	}

	public String getSymmetricEncAlgorithm()
	{
		return StringUtils.isNullOrEmpty( symmetricEncAlgorithm ) ? DEFAULT_OPTION : symmetricEncAlgorithm;
	}

	public void setSymmetricEncAlgorithm( String symmetricEncAlgorithm )
	{
		if( DEFAULT_OPTION.equals( symmetricEncAlgorithm ) )
			symmetricEncAlgorithm = null;

		this.symmetricEncAlgorithm = symmetricEncAlgorithm;
		saveConfig();
	}

	public void process( WSSecHeader secHeader, Document doc, PropertyExpansionContext context )
	{
		StringWriter writer = null;

		try
		{
			WSSecEncrypt wsEncrypt = new WSSecEncrypt();
			WssCrypto wssCrypto = getWssContainer().getCryptoByName( crypto );
			Crypto crypto = wssCrypto.getCrypto();

			wsEncrypt.setUserInfo( context.expand( getUsername() ) );

			if( getKeyIdentifierType() != 0 )
			{
				wsEncrypt.setKeyIdentifierType( getKeyIdentifierType() );
			}

			if( getKeyIdentifierType() == WSConstants.EMBEDDED_KEYNAME )
			{
				wsEncrypt.setEmbeddedKeyName( getEmbeddedKeyName() );
				wsEncrypt.setKey( crypto.getPrivateKey( getEmbeddedKeyName(), getEmbeddedKeyPassword() ).getEncoded() );
			}

			if( getSymmetricEncAlgorithm() != null )
			{
				wsEncrypt.setSymmetricEncAlgorithm( getSymmetricEncAlgorithm() );
			}

			if( getEncKeyTransport() != null )
			{
				wsEncrypt.setKeyEnc( getEncKeyTransport() );
			}

			if( getEncryptionCanonicalization() != null )
			{
				wsEncrypt.setEncCanonicalization( getEncryptionCanonicalization() );
			}

			wsEncrypt.setEncryptSymmKey( isEncryptSymmetricKey() );

			if( parts.size() > 0 )
			{
				Vector<WSEncryptionPart> wsParts = createWSParts( parts );
				if( !wsParts.isEmpty() )
					wsEncrypt.setParts( wsParts );
			}

			// create backup
			writer = new StringWriter();
			XmlUtils.serialize( doc, writer );

			wsEncrypt.build( doc, crypto, secHeader );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );

			if( writer != null && writer.getBuffer().length() > 0 )
			{
				try
				{
					// try to restore..
					doc.replaceChild( doc.importNode( XmlUtils.parseXml( writer.toString() ).getDocumentElement(), true ),
							doc.getDocumentElement() );
				}
				catch( Exception e1 )
				{
					SoapUI.logError( e1 );
				}
			}
		}
	}

	@Override
	protected void addPropertyExpansions( PropertyExpansionsResult result )
	{
		super.addPropertyExpansions( result );
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
