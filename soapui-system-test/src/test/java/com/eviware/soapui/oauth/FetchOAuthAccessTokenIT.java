/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.oauth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.utils.SoapUIWorkspaceFixture;
import javafx.scene.web.WebEngine;
import org.fest.swing.core.BasicComponentFinder;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JMenuItemFixture;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.List;

import static com.eviware.soapui.utils.FestMatchers.frameWithTitle;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_TAB;
import static org.fest.swing.launcher.ApplicationLauncher.application;

/**
 *
 */
public class FetchOAuthAccessTokenIT
{
	private List<String> existingProjectsNameList;
	private SoapUIWorkspaceFixture soapUIWorkspaceFixture;
	private Robot robot;

	@Before
	public void setUp()
	{
		System.setProperty( "soapui.jxbrowser.disable", "true" );
		application( SoapUI.class ).start();
		robot = BasicRobot.robotWithCurrentAwtHierarchy();
		soapUIWorkspaceFixture = new SoapUIWorkspaceFixture();
	}

	@Ignore
	@Test
	public void getsOAuth2AccessToken() throws InterruptedException
	{
		FrameFixture rootWindow = frameWithTitle( "SoapUI" ).using( robot );
		JMenuItemFixture getOAuthTokenMenu = rootWindow.menuItem( new GenericTypeMatcher<JMenuItem>(JMenuItem.class)
		{
			@Override
			protected boolean isMatching( JMenuItem component )
			{
				return component.getText().equals( "Get OAuth Token" ) ;
			}
		} );
		getOAuthTokenMenu.click();
		Thread.sleep( 2000 );
		robot.enterText( "soapui.development" );
		robot.pressAndReleaseKeys( VK_TAB );
		robot.enterText( "20jz9Kzis2AAA8gr" );
		robot.pressAndReleaseKeys( VK_ENTER );
		Thread.sleep( 500 );
		robot.pressAndReleaseKeys( VK_TAB );
		robot.pressAndReleaseKeys( VK_ENTER );

		Thread.sleep( 40000 );
	}
}
