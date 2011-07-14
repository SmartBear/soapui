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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.actions.support.OpenUrlAction;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class SoapUIVersionUpdate
{
	private final static String MAJOR_VERSION = "major";
	private final static String MINOR_VERSION = "minor";

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

	public static void getLatestVersionAvailable2()
	{
		InputStream in;
		try
		{
			File file = new File( "E:\\eviware\\soapui-version.xml" );
			in = new FileInputStream( file );
			Document doc = XmlUtils.parse( in );
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName( "version" );
		}
		catch( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getLatestVersionAvailable()
	{
		try
		{
			File file = new File( "E:\\eviware\\soapui-version.xml" );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( file );
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

	public static boolean promptForNewVersionDownload()
	{

		ActionList actions = new DefaultActionList();
		actions.addAction( new ShowOnlineHelpAction( "http://www.eviware.com/nightly-builds" ) );
		//		actions.addAction( createBuyLicenseAction() );
		actions.addAction( new OpenUrlAction( "Download latest version", "ccc" ) );
		XFormDialog dialog = ADialogBuilder.buildDialog( Form.class, actions );
		dialog.show();

		return false;
	}

	@AForm( description = "Enter soapUI Pro license data as obtained at purchase", name = "soapUI Pro License", helpUrl = "", icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "The license file", name = "License File", type = AFieldType.FILE )
		public final static String LICENSEFILE = "License File";
	}

}
