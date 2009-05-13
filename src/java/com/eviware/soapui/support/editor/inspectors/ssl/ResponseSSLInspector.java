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

package com.eviware.soapui.support.editor.inspectors.ssl;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.cert.Certificate;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SSLInfo;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.xml.XmlDocument;

public class ResponseSSLInspector extends AbstractXmlInspector implements PropertyChangeListener
{
	private JEditorPane sslInfoPane;
	private JPanel panel;
	private AbstractHttpRequest<?> request;

	protected ResponseSSLInspector( AbstractHttpRequest<?> abstractHttpRequest )
	{
		super( "SSL Info", "SSL Certificate Information for this response", true, SSLInspectorFactory.INSPECTOR_ID );
		this.request = abstractHttpRequest;
		abstractHttpRequest.addPropertyChangeListener( WsdlRequest.RESPONSE_PROPERTY, this );
	}

	public JComponent getComponent()
	{
		if( panel != null )
			return panel;

		panel = new JPanel( new BorderLayout() );
		sslInfoPane = new JEditorPane();
		sslInfoPane.setEditorKit( new HTMLEditorKit() );
		panel.add( new JScrollPane( sslInfoPane ) );

		return panel;
	}

	@Override
	public void release()
	{
		super.release();

		request.removePropertyChangeListener( WsdlRequest.RESPONSE_PROPERTY, this );
	}

	private void updateSSLInfo( SSLInfo sslInfo )
	{
		String sslInfoTabTitle = "SSL Info";

		if( sslInfo != null )
		{
			StringBuffer buf = new StringBuffer( "<html><body><table cellpadding=1 cellspacing=1 border=0>" );

			buf.append( "<tr><td><b>CipherSuite:</b></td><td align=left valign=top>" ).append(
					sslInfo.getCipherSuite().toString() ).append( "</td></tr>" );

			if( sslInfo.getLocalPrincipal() != null )
				buf.append( "<tr><td><b>LocalPrincipal:</b></td><td align=left valign=top>" ).append(
						sslInfo.getLocalPrincipal().getName() ).append( "</td></tr>" );

			Certificate[] localCertificates = sslInfo.getLocalCertificates();

			if( localCertificates != null )
			{
				buf.append( "</table><table cellpadding=0 cellspacing=0 border=0>" );

				int cnt = 1;
				for( Certificate cert : localCertificates )
				{
					buf.append( "<tr><td><b>Local Certificate " ).append( cnt++ ).append( ":</b><pre><font size=-1>" )
							.append( cert.toString() ).append( "</font></pre></td></tr>" );

				}

				buf.append( "</table><table cellpadding=1 cellspacing=1 border=0>" );
			}

			if( sslInfo.getPeerPrincipal() != null )
				buf.append( "<tr><td><b>PeerPrincipal:</b></td><td align=left valign=top>" ).append(
						sslInfo.getPeerPrincipal().toString() ).append( "</td></tr>" );

			Certificate[] peerCertificates = sslInfo.getPeerCertificates();
			if( peerCertificates != null )
			{
				buf.append( "</table><table cellpadding=0 cellspacing=0 border=0>" );

				int cnt = 1;
				for( Certificate cert : peerCertificates )
				{
					buf.append( "<tr><td colspan=2><b>Peer Certificate " ).append( cnt++ ).append(
							":</b><pre><font size=-1>" ).append( cert.toString() ).append( "</font></pre></td></tr>" );
				}

				buf.append( "</table><table cellpadding=0 cellspacing=0 border=0>" );
			}

			buf.append( "</table></body></html>" );
			sslInfoPane.setText( buf.toString() );

			sslInfoTabTitle += " (" + sslInfo.getPeerCertificates().length + " certs)";
		}

		setTitle( sslInfoTabTitle );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		HttpResponse response = request.getResponse();
		updateSSLInfo( response == null ? null : response.getSSLInfo() );
		setEnabled( response != null && response.getSSLInfo() != null );
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return true;
	}
}
