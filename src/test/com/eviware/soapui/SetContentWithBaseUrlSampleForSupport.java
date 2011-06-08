package com.eviware.soapui;

/*
 * Copyright (c) 2000-2011 TeamDev Ltd. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

import java.awt.BorderLayout;

import javax.swing.JFrame;

import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserType;

public class SetContentWithBaseUrlSampleForSupport
{
	public static void main( String[] args )
	{
		Browser browser = BrowserFactory.createBrowser( BrowserType.Mozilla );

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.add( browser.getComponent(), BorderLayout.CENTER );
		frame.setSize( 400, 300 );
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );

		String content = "";
		String url = "http://192.168.250.4:8484/App4Test/";
		browser.setContent( content, url );
		browser.waitReady();
		System.out.println( "browser.getContent() = " + browser.getContent() );
	}
}
