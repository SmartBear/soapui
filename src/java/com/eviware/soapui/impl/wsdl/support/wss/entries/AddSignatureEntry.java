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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.xml.security.signature.XMLSignature;
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

public class AddSignatureEntry extends WssEntryBase
{
	private static final String DEFAULT_OPTION = "<default>";
	public static final String TYPE = "Signature";
	private String crypto;
	private int keyIdentifierType = 0;
	private String signatureAlgorithm;
	private boolean useSingleCert;
	private String signatureCanonicalization;
	private List<StringToStringMap> parts = new ArrayList<StringToStringMap>();
	private com.eviware.soapui.impl.wsdl.support.wss.entries.WssEntryBase.KeyAliasComboBoxModel keyAliasComboBoxModel;
	private com.eviware.soapui.impl.wsdl.support.wss.entries.AddSignatureEntry.InternalWssContainerListener wssContainerListener;

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

		form.appendComboBox( "crypto", "Keystore",
				new KeystoresComboBoxModel( getWssContainer(), getWssContainer().getCryptoByName( crypto ) ),
				"Selects the Keystore containing the key to use for signing" ).addItemListener( new ItemListener()
		{

			public void itemStateChanged( ItemEvent e )
			{
				keyAliasComboBoxModel.update( getWssContainer().getCryptoByName( crypto ) );
			}
		} );

		keyAliasComboBoxModel = new KeyAliasComboBoxModel( getWssContainer().getCryptoByName( crypto ) );
		form.appendComboBox( "username", "Alias", keyAliasComboBoxModel, "The alias for the key to use for encryption" );

		// form.appendTextField( "username", "Alias", "The certificate alias" );
		form.appendPasswordField( "password", "Password", "The certificate password" );

		form.appendComboBox( "keyIdentifierType", "Key Identifier Type", new Integer[] { 0, 1, 2, 3, 4 },
				"Sets which key identifier to use" ).setRenderer( new KeyIdentifierTypeRenderer() );
		form
				.appendComboBox( "signatureAlgorithm", "Signature Algorithm", new String[] { DEFAULT_OPTION,
						WSConstants.RSA, WSConstants.DSA, XMLSignature.ALGO_ID_MAC_HMAC_SHA1,
						XMLSignature.ALGO_ID_MAC_HMAC_SHA256, XMLSignature.ALGO_ID_MAC_HMAC_SHA384,
						XMLSignature.ALGO_ID_MAC_HMAC_SHA512, XMLSignature.ALGO_ID_MAC_HMAC_RIPEMD160,
						XMLSignature.ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5, XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA1,
						XMLSignature.ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
						XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384,
						XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512, XMLSignature.ALGO_ID_SIGNATURE_RSA_RIPEMD160 },
						"Set the name of the signature encryption algorithm to use" );
		form.appendComboBox( "signatureCanonicalization", "Signature Canonicalization", new String[] { DEFAULT_OPTION,
				WSConstants.C14N_OMIT_COMMENTS, WSConstants.C14N_WITH_COMMENTS, WSConstants.C14N_EXCL_OMIT_COMMENTS,
				WSConstants.C14N_EXCL_WITH_COMMENTS }, "Set the canonicalization method to use." );

		form.appendCheckBox( "useSingleCert", "Use Single Certificate", "Use single certificate for signing" );

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
		signatureAlgorithm = reader.readString( "signatureAlgorithm", null );
		signatureCanonicalization = reader.readString( "signatureCanonicalization", null );
		useSingleCert = reader.readBoolean( "useSingleCert", false );
		parts = readParts( reader, "signaturePart" );
	}

	@Override
	protected void save( XmlObjectConfigurationBuilder builder )
	{
		builder.add( "crypto", crypto );
		builder.add( "keyIdentifierType", keyIdentifierType );
		builder.add( "signatureAlgorithm", signatureAlgorithm );
		builder.add( "signatureCanonicalization", signatureCanonicalization );
		builder.add( "useSingleCert", useSingleCert );
		saveParts( builder, parts, "signaturePart" );
	}

	public void process( WSSecHeader secHeader, Document doc, PropertyExpansionContext context )
	{
		StringWriter writer = null;

		try
		{
			WssCrypto wssCrypto = getWssContainer().getCryptoByName( crypto );
			if( wssCrypto == null )
			{
				throw new Exception( "Missing crypto [" + crypto + "] for signature entry" );
			}

			WSSecSignature wssSign = new WSSecSignature();
			wssSign.setUserInfo( context.expand( getUsername() ), context.expand( getPassword() ) );

			if( keyIdentifierType != 0 )
				wssSign.setKeyIdentifierType( keyIdentifierType );

			if( StringUtils.hasContent( signatureAlgorithm ) )
				wssSign.setSignatureAlgorithm( signatureAlgorithm );

			if( StringUtils.hasContent( signatureCanonicalization ) )
				wssSign.setSigCanonicalization( signatureCanonicalization );

			wssSign.setUseSingleCertificate( useSingleCert );

			Vector<WSEncryptionPart> wsParts = createWSParts( parts );
			if( !wsParts.isEmpty() )
			{
				wssSign.setParts( wsParts );
			}

			writer = new StringWriter();
			XmlUtils.serialize( doc, writer );

			wssSign.build( doc, wssCrypto.getCrypto(), secHeader );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );

			if( writer != null && writer.getBuffer().length() > 0 )
			{
				try
				{
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

	public int getKeyIdentifierType()
	{
		return keyIdentifierType;
	}

	public void setKeyIdentifierType( int keyIdentifierType )
	{
		this.keyIdentifierType = keyIdentifierType;
		saveConfig();
	}

	public String getSignatureAlgorithm()
	{
		return StringUtils.isNullOrEmpty( signatureAlgorithm ) ? DEFAULT_OPTION : signatureAlgorithm;
	}

	public void setSignatureAlgorithm( String signatureAlgorithm )
	{
		if( DEFAULT_OPTION.equals( signatureAlgorithm ) )
			signatureAlgorithm = null;

		this.signatureAlgorithm = signatureAlgorithm;
		saveConfig();
	}

	public String getSignatureCanonicalization()
	{
		return StringUtils.isNullOrEmpty( signatureCanonicalization ) ? DEFAULT_OPTION : signatureCanonicalization;
	}

	public void setSignatureCanonicalization( String signatureCanonicalization )
	{
		if( DEFAULT_OPTION.equals( signatureCanonicalization ) )
			signatureCanonicalization = null;

		this.signatureCanonicalization = signatureCanonicalization;
		saveConfig();
	}

	public boolean isUseSingleCert()
	{
		return useSingleCert;
	}

	public void setUseSingleCert( boolean useSingleCert )
	{
		this.useSingleCert = useSingleCert;
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
