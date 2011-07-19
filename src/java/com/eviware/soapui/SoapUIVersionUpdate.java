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
package com.eviware.soapui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.x.form.XFormDialog;

public class SoapUIVersionUpdate
{
	private static final String MAJOR_VERSION = "major";
	private static final String MINOR_VERSION = "minor";
	private static final String LATEST_VERSION_XML_LOCATION = "http://www.soapui.org/version/soapui-latest-version.xml";
	public static final String VERSION_TO_SKIP = SoapUI.class.getName() + "@versionToSkip";

	//	JDialog dialog

	XFormDialog formDialog;
	private String latestVersion;
	private String releaseNotes;
	private String versionType;
	private String coreDownloadLink;
	private String proDownloadLink;

	private void getLatestVersionAvailable()
	{
		try
		{
			//			File file = new File( LATEST_VERSION_XML_LOCATION );

			URL versionUrl = new URL( LATEST_VERSION_XML_LOCATION );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( versionUrl.openStream() );
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName( "version" );

			//			for( int s = 0; s < nodeLst.getLength(); s++ )
			//			{

			Node fstNode = nodeLst.item( 0 );

			if( fstNode.getNodeType() == Node.ELEMENT_NODE )
			{

				Element fstElmnt = ( Element )fstNode;
				NodeList vrsnTypeElmntLst = fstElmnt.getElementsByTagName( "version-type" );
				Element vrsnTypeElmnt = ( Element )vrsnTypeElmntLst.item( 0 );
				NodeList vrsnType = vrsnTypeElmnt.getChildNodes();
				versionType = ( ( Node )vrsnType.item( 0 ) ).getNodeValue().toString();

				NodeList vrsnNmbrElmntLst = fstElmnt.getElementsByTagName( "version-number" );
				Element vrsnNmbrElmnt = ( Element )vrsnNmbrElmntLst.item( 0 );
				NodeList vrsnNmbr = vrsnNmbrElmnt.getChildNodes();
				latestVersion = ( ( Node )vrsnNmbr.item( 0 ) ).getNodeValue().toString();

				NodeList rlsNtsElmntLst = fstElmnt.getElementsByTagName( "release-notes" );
				Element rlsNtsElmnt = ( Element )rlsNtsElmntLst.item( 0 );
				NodeList rlsNts = rlsNtsElmnt.getChildNodes();
				releaseNotes = ( ( Node )rlsNts.item( 0 ) ).getNodeValue().toString();

				NodeList coreDownloadNtsElmntLst = fstElmnt.getElementsByTagName( "core-download-link" );
				Element coreDownloadNtsElmnt = ( Element )coreDownloadNtsElmntLst.item( 0 );
				NodeList coreDownloadNts = coreDownloadNtsElmnt.getChildNodes();
				coreDownloadLink = ( ( Node )coreDownloadNts.item( 0 ) ).getNodeValue().toString();

				NodeList proDownloadNtsElmntElmntLst = fstElmnt.getElementsByTagName( "pro-download-link" );
				Element proDownloadNtsElmnt = ( Element )proDownloadNtsElmntElmntLst.item( 0 );
				NodeList proDownloadNts = proDownloadNtsElmnt.getChildNodes();
				proDownloadLink = ( ( Node )proDownloadNts.item( 0 ) ).getNodeValue().toString();
			}

			//			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	private boolean isNewMajorReleaseAvailable()
	{
		return !StringUtils.isNullOrEmpty( versionType ) && versionType.equals( MAJOR_VERSION )
				&& SoapUI.SOAPUI_VERSION.compareTo( getLatestVersion() ) < 0;
	}

	private boolean isNewMinorReleaseAvailable()
	{
		return !StringUtils.isNullOrEmpty( versionType ) && versionType.equals( MINOR_VERSION )
				&& SoapUI.SOAPUI_VERSION.compareTo( getLatestVersion() ) < 0;
	}

	public String getReleaseNotes()
	{
		return releaseNotes;
	}

	public String getLatestVersion()
	{
		return latestVersion;
	}

	public void showNewMajorVersionDownloadDialog()
	{

		JPanel versionUpdatePanel = new JPanel( new BorderLayout() );
		JDialog dialog = new JDialog();
		versionUpdatePanel.add( UISupport.buildDescription( "New Version of soapUI is Available", "", null ),
				BorderLayout.NORTH );
		JEditorPane text = new JEditorPane();
		try
		{
			text.setPage( getReleaseNotes() );
			text.setEditable( false );
			text.setBorder( BorderFactory.createLineBorder( Color.black ) );
		}
		catch( IOException e )
		{
			SoapUI.logError( e );
		}
		JScrollPane scb = new JScrollPane( text );
		versionUpdatePanel.add( scb, BorderLayout.CENTER );
		JXToolBar toolbar = buildToolbar( dialog );
		versionUpdatePanel.add( toolbar, BorderLayout.SOUTH );
		dialog.setTitle( "New Version Update" );

		dialog.setModal( true );
		dialog.getContentPane().add( versionUpdatePanel );
		dialog.setSize( new Dimension( 500, 400 ) );
		UISupport.centerDialog( dialog, SoapUI.getFrame() );
		dialog.setVisible( true );
	}

	protected JXToolBar buildToolbar( JDialog dialog )
	{
		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.add( new IgnoreUpdateAction( dialog ) );
		toolbar.addGlue();
		toolbar.addSeparator();
		toolbar.add( new RemindLaterAction( dialog ) );
		toolbar.add( new OpenDownloadUrlAction( "Download latest version", getCoreDownloadLink(), dialog ) );
		return toolbar;
	}

	public void showNewMinorVersionDownloadDialog()
	{
		//TODO implement minor, for now left the same
		showNewMinorVersionDownloadDialog();
	}

	public boolean skipThisVersion()
	{
		return SoapUI.getSettings().getString( VERSION_TO_SKIP, "" ).equals( getLatestVersion() );
	}

	public void checkForNewVersion( boolean helpAction )
	{
		getLatestVersionAvailable();
		if( isNewMajorReleaseAvailable() && ( !skipThisVersion() || helpAction ) )
			showNewMajorVersionDownloadDialog();
		else if( isNewMinorReleaseAvailable() && ( !skipThisVersion() || helpAction ) )
			showNewMinorVersionDownloadDialog();
		else if( helpAction )
			UISupport.showInfoMessage( "Currently no new version available", "No New Version" );
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

	protected String getDownloadLink()
	{
		return getCoreDownloadLink();
	}

	protected String getCoreDownloadLink()
	{
		return coreDownloadLink;
	}

	protected String getProDownloadLink()
	{
		return proDownloadLink;
	}

}
