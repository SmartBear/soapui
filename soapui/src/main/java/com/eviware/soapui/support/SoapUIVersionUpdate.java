/*
 *  SoapUI, copyright (C) 2004-2011 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.ProxySettings;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class SoapUIVersionUpdate
{
	static final String VERSION_UPDATE_URL_SYS_PROP_KEY = "versionUpdateUrl";
	static final String LATEST_VERSION_XML_LOCATION = versionUpdateUrl( "http://dl.eviware.com/version-update/soapui-version.xml" );
	public static final String VERSION_TO_SKIP = SoapUI.class.getName() + "@versionToSkip";
	protected static final String NO_RELEASE_NOTES_INFO = "Sorry! No Release notes currently available.";

	private String latestVersion;
	private String releaseNotesCore;
	private String releaseNotesPro;
	private String downloadLinkCore;
	private String downloadLinkPro;

	public void getLatestVersionAvailable( String documentContent ) throws Exception
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			InputStream inputStream = IOUtils.toInputStream( documentContent, "UTF-8" );

			Document doc = db.parse( inputStream );
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName( "version" );

			Node firstNode = nodeList.item( 0 );

			if( firstNode.getNodeType() == Node.ELEMENT_NODE )
			{

				Element firstElement = ( Element )firstNode;

				latestVersion = getNodeValue( firstElement, "version-number" );
				releaseNotesCore = getNodeValue( firstElement, "release-notes-core" );
				releaseNotesPro = getNodeValue( firstElement, "release-notes-pro" );
				downloadLinkCore = getNodeValue( firstElement, "download-link-core" );
				downloadLinkPro = getNodeValue( firstElement, "download-link-pro" );
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e, "Network Error for Version Update or Proxy" );
			throw e;
		}
	}

	private String getNodeValue( Element firstElement, String tagName )
	{
		NodeList elementList = firstElement.getElementsByTagName( tagName );
		Element element = ( Element )elementList.item( 0 );
		NodeList nodes = element.getChildNodes();
		return nodes.item( 0 ).getNodeValue();
	}

	private String fetchVersionDocumentContent( final URL versionUrl ) throws URISyntaxException, IOException
	{
		Proxy proxy = null;
		if( ProxyUtils.isProxyEnabled() )
		{
			HttpRoutePlanner routePlanner = HttpClientSupport.getHttpClient().getRoutePlanner();
			HttpRoute httpRoute;
			try
			{
				HttpGet request = new HttpGet( versionUrl.toURI() );
				HttpContext httpContext = HttpClientSupport.createEmptyContext();
				ProxyUtils.initProxySettings( SoapUI.getSettings(), request, httpContext, versionUrl.toString(), null );
				httpRoute = routePlanner.determineRoute( new HttpHost( versionUrl.getHost() ), request, null );
			}
			catch( HttpException e )
			{
				throw new IOException( "Error detecting proxy", e );
			}
			HttpHost proxyHost = httpRoute.getProxyHost();
			if( proxyHost != null )
			{
				proxy = new Proxy( Proxy.Type.HTTP, new InetSocketAddress( proxyHost.getHostName(), proxyHost.getPort() ) );
				Authenticator.setDefault( new Authenticator()
				{
					@Override
					protected PasswordAuthentication getPasswordAuthentication()
					{
						if( !getRequestingURL().getHost().equals( versionUrl.getHost() ) )
						{
							return null;
						}
						Settings settings = SoapUI.getSettings();
						String proxyUsername = PropertyExpander.expandProperties( ( PropertyExpansionContext )null,
								settings.getString( ProxySettings.USERNAME, null ) );
						String proxyPassword = PropertyExpander.expandProperties( ( PropertyExpansionContext )null,
								settings.getString( ProxySettings.PASSWORD, null ) );

						return new PasswordAuthentication( proxyUsername, proxyPassword.toCharArray() );
					}
				} );
			}
		}

		URLConnection connection = proxy == null ? versionUrl.openConnection() : versionUrl.openConnection(proxy);
		String response = IOUtils.toString( connection.getInputStream() );
		Authenticator.setDefault( null );
		return response;
	}

	private static String versionUpdateUrl( String defaultUrl )
	{
		return System.getProperty( VERSION_UPDATE_URL_SYS_PROP_KEY, defaultUrl );
	}

	private boolean isNewReleaseAvailable()
	{
		String currentSoapuiVersion = SoapUI.SOAPUI_VERSION;
		int snapshotIndex = currentSoapuiVersion.indexOf( "SNAPSHOT" );
		boolean isSnapshot = snapshotIndex > 0;
		//if version is snapshot strip SNAPSHOT
		if( isSnapshot )
		{
			currentSoapuiVersion = currentSoapuiVersion.substring( 0, snapshotIndex - 1 );
		}

		String latestVersion = getLatestVersion();

		if( StringUtils.isNullOrEmpty( latestVersion ) )
		{
			return false;
		}

		// user has to be notified when SNAPSHOT version became OFFICIAL 
		if( isSnapshot && currentSoapuiVersion.equals( latestVersion ) )
		{
			return true;
		}

		return currentSoapuiVersion.compareTo( latestVersion ) < 0;

	}

	protected String getReleaseNotes()
	{
		return getReleaseNotesCore();
	}

	public String getReleaseNotesCore()
	{
		return releaseNotesCore;
	}

	public String getReleaseNotesPro()
	{
		return releaseNotesPro;
	}

	public String getLatestVersion()
	{
		return latestVersion;
	}

	public void showNewVersionDownloadDialog()
	{

		JPanel versionUpdatePanel = new JPanel( new BorderLayout() );
		JDialog dialog = new JDialog();
		versionUpdatePanel.add( UISupport.buildDescription( "New Version of SoapUI is Available", "", null ),
				BorderLayout.NORTH );
		JEditorPane text = createReleaseNotesPane();
		JScrollPane scb = new JScrollPane( text );
		versionUpdatePanel.add( scb, BorderLayout.CENTER );
		JPanel toolbar = buildToolbar( dialog );
		versionUpdatePanel.add( toolbar, BorderLayout.SOUTH );
		dialog.setTitle( "New Version Update" );

		dialog.setModal( true );
		dialog.getContentPane().add( versionUpdatePanel );
		dialog.setSize( new Dimension( 500, 640 ) );
		UISupport.centerDialog( dialog, SoapUI.getFrame() );
		dialog.setVisible( true );
	}

	protected JEditorPane createReleaseNotesPane()
	{
		JEditorPane text = new JEditorPane();
		try
		{
			text.setPage( getReleaseNotes() );
			text.setEditable( false );
			text.setBorder( BorderFactory.createLineBorder( Color.black ) );
		}
		catch( IOException e )
		{
			text.setText( NO_RELEASE_NOTES_INFO );
			SoapUI.logError( e );
		}
		return text;
	}

	protected JPanel buildToolbar( JDialog dialog )
	{
		JPanel toolbarPanel = new JPanel( new BorderLayout() );
		JPanel leftBtns = new JPanel();
		leftBtns.add( new JButton( ( new IgnoreUpdateAction( dialog ) ) ) );
		leftBtns.add( new JButton( new RemindLaterAction( dialog ) ) );
		JButton createToolbarButton = new JButton( new OpenDownloadUrlAction( "Download latest version",
				getDownloadLinkCore(), dialog ) );
		JPanel rightBtn = new JPanel();
		rightBtn.add( createToolbarButton );
		toolbarPanel.add( leftBtns, BorderLayout.WEST );
		toolbarPanel.add( rightBtn, BorderLayout.EAST );
		return toolbarPanel;
	}

	public boolean skipThisVersion()
	{
		return SoapUI.getSettings().getString( VERSION_TO_SKIP, "" ).equals( getLatestVersion() );
	}

	public void checkForNewVersion( final boolean helpAction )
	{
		try
		{
			String documentContent = fetchVersionDocumentContent( new URL( LATEST_VERSION_XML_LOCATION ) );
			getLatestVersionAvailable( documentContent );
		}
		catch( Exception e )
		{
			SoapUI.log( e.getMessage() );
			return;
		}
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				if( isNewReleaseAvailable() && ( !skipThisVersion() || helpAction ) )
					showNewVersionDownloadDialog();
				else if( helpAction )
					UISupport.showInfoMessage( "You are running the latest version of SoapUI!", "Version Check" );
			}
		} );
	}

	protected class IgnoreUpdateAction extends AbstractAction
	{
		private JDialog dialog;

		public IgnoreUpdateAction( JDialog dialog )
		{
			super( "Ignore This Update" );
			this.dialog = dialog;
		}

		public void actionPerformed( ActionEvent e )
		{
			SoapUI.getSettings().setString( VERSION_TO_SKIP, getLatestVersion() );
			dialog.setVisible( false );
		}
	}

	protected class RemindLaterAction extends AbstractAction
	{
		private JDialog dialog;

		public RemindLaterAction( JDialog dialog )
		{
			super( "Remind Me Later" );
			this.dialog = dialog;
		}

		public void actionPerformed( ActionEvent e )
		{
			dialog.setVisible( false );
		}
	}

	public class OpenDownloadUrlAction extends AbstractAction
	{
		private final String url;
		private JDialog dialog;

		public OpenDownloadUrlAction( String title, String url, JDialog dialog )
		{
			super( title );
			this.url = url;
			this.dialog = dialog;
		}

		public void actionPerformed( ActionEvent e )
		{
			if( url == null )
				UISupport.showErrorMessage( "Missing url" );
			else
				Tools.openURL( url );
			dialog.setVisible( false );
		}
	}

	protected String getDownloadLinkCore()
	{
		return downloadLinkCore;
	}

	protected String getDownloadLinkPro()
	{
		return downloadLinkPro;
	}

}
