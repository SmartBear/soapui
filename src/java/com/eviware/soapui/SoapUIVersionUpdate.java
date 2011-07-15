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
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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

	//	JDialog dialog

	XFormDialog formDialog;
	private static String latestVersion;
	private static String releaseNotes;
	private static String versionType;

	public static void main( String argv[] )
	{
		getLatestVersionAvailable();
		System.out.println( "latest version:" + getLatestVersion() );
		System.out.println( "notes:" + getReleaseNotes() );
		System.out.println( "new major available:" + isNewMajorReleaseAvailable() );
		System.out.println( "new minor available:" + isNewMinorReleaseAvailable() );

	}

	public static void getLatestVersionAvailable()
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
			}

			//			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	public static boolean isNewMajorReleaseAvailable()
	{
		return !StringUtils.isNullOrEmpty( versionType ) && versionType.equals( MAJOR_VERSION )
				&& SoapUI.SOAPUI_VERSION.compareTo( getLatestVersion() ) < 0;
	}

	public static boolean isNewMinorReleaseAvailable()
	{
		return !StringUtils.isNullOrEmpty( versionType ) && versionType.equals( MINOR_VERSION )
				&& SoapUI.SOAPUI_VERSION.compareTo( getLatestVersion() ) < 0;
	}

	public static String getReleaseNotes()
	{
		return releaseNotes;
	}

	public static String getLatestVersion()
	{
		return latestVersion;
	}

	public static void showNewVersionDownloadDialog()
	{

		JPanel versionUpdatePanel = new JPanel( new BorderLayout() );
		JDialog dialog = new JDialog();
		versionUpdatePanel.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		versionUpdatePanel.add(
				UISupport.buildDescription( "New Version of soapUI is Available", "soapUI update available", null ),
				BorderLayout.NORTH );
		JTextArea text = new JTextArea( getReleaseNotes() );
		text.setEditable( false );
		text.setBorder( BorderFactory.createLineBorder( Color.black ) );
		versionUpdatePanel.add( text, BorderLayout.CENTER );
		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.add( new IgnoreUpdateAction() );
		toolbar.addGlue();
		toolbar.addSeparator();
		toolbar.add( new RemindLaterAction() );
		toolbar.add( new OpenDownloadUrlAction( "Download latest version", "http://www.eviware.com/nightly-builds",
				dialog ) );
		versionUpdatePanel.add( toolbar, BorderLayout.SOUTH );
		dialog.setTitle( "New Version Update" );
		dialog.setIconImage( null );

		dialog.setModal( true );
		dialog.getContentPane().add( versionUpdatePanel );
		dialog.setSize( new Dimension( 500, 400 ) );
		UISupport.centerDialog( dialog, SoapUI.getFrame() );
		dialog.setVisible( true );
		//TODO position window
	}

	protected static class IgnoreUpdateAction extends AbstractAction
	{
		public IgnoreUpdateAction()
		{
			super( "Ignore This Update" );
		}

		public void actionPerformed( ActionEvent e )
		{
			//TODO implement action
		}
	}

	protected static class RemindLaterAction extends AbstractAction
	{
		public RemindLaterAction()
		{
			super( "Remind Me Later" );
		}

		public void actionPerformed( ActionEvent e )
		{
			//TODO implement action
		}
	}

	public static class OpenDownloadUrlAction extends AbstractAction
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

}
