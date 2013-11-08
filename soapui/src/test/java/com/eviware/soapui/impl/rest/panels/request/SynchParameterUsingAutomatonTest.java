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

package com.eviware.soapui.impl.rest.panels.request;

import com.athaydes.automaton.SwingUtil;
import com.athaydes.automaton.Swinger;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.panels.method.RestMethodDesktopPanel;
import com.eviware.soapui.impl.rest.panels.request.views.content.RestRequestContentView;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestResourceDesktopPanel;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AddParamAction;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.ui.Navigator;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation.RESOURCE;
import static com.eviware.soapui.impl.rest.panels.method.RestMethodDesktopPanel.REST_METHOD_EDITOR;
import static com.eviware.soapui.impl.rest.panels.request.RestRequestDesktopPanel.REST_REQUEST_EDITOR;
import static com.eviware.soapui.impl.rest.panels.resource.RestResourceDesktopPanel.REST_RESOURCE_EDITOR;
import static java.awt.event.KeyEvent.VK_ENTER;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-11-05
 * Time: 10:47
 * To change this template use File | Settings | File Templates.
 */
public class SynchParameterUsingAutomatonTest
{

	private Window soapUI;

	@Before
	public void setUp() throws Exception
	{
		SoapUI.main( new String[0] );
		Thread.sleep( 4000 );
		soapUI = getWindowWithTitle( "SoapUI" );

	}

	@Test
	public void testSynchparam() throws InterruptedException
	{
		assertNotNull( soapUI );

		createNewRestProject( soapUI );

		Thread.sleep( 1000 );
		RestRequestDesktopPanel reqDesktopPanel = ( RestRequestDesktopPanel )SwingUtil.lookup( REST_REQUEST_EDITOR, soapUI );

		addNewParameter( reqDesktopPanel, "ParamName", "value" );
		verifyParamValues( getRestParameterTable( reqDesktopPanel ), 0, "ParamName", "value" );

		openResourceEditor( soapUI );

		RestResourceDesktopPanel resourceDesktopPanel = ( RestResourceDesktopPanel )SwingUtil
				.lookup( REST_RESOURCE_EDITOR, soapUI );
		verifyParamValues( resourceDesktopPanel.getParamsTable(), 0, "ParamName", "" );

		addNewParameter( resourceDesktopPanel, "resParam", "value11" );
		verifyParamValues( resourceDesktopPanel.getParamsTable(), 1, "resParam", "value11" );
		verifyParamValues( getRestParameterTable( reqDesktopPanel ), 1, "resParam", "value11" );

		openMethodEditor( soapUI );

		RestMethodDesktopPanel methodDesktopPanel = ( RestMethodDesktopPanel )SwingUtil.lookup( REST_METHOD_EDITOR,
				soapUI );
		addNewParameter( methodDesktopPanel, "mParam", "mValue" );
		verifyParamValues( methodDesktopPanel.getParamsTable(), 0, "mParam", "mValue" );
		verifyParamValues( getRestParameterTable( reqDesktopPanel ), 2, "mParam", "mValue" );

		changeParameterLevelToResource( methodDesktopPanel.getParamsTable() );
		verifyEmptyTable( methodDesktopPanel.getParamsTable() );
		verifyParamValues( resourceDesktopPanel.getParamsTable(), 2, "mParam", "mValue" );

		openResourceEditor( soapUI );

	}


	private void verifyEmptyTable( RestParamsTable paramsTable )
	{
		assertThat( paramsTable.getParamsTable().getRowCount(), is( 0 ) );
	}

	private void changeParameterLevelToResource( RestParamsTable paramsTable )
	{

		paramsTable.getParamsTable().setValueAt( RESOURCE, 0, 3 );
	}

	private RestParamsTable getRestParameterTable( RestRequestDesktopPanel requestDesktopPanel )
	{
		java.util.List<? extends EditorView<? extends XmlDocument>> views = requestDesktopPanel.getRequestEditor().getViews();
		RestRequestContentView restRequestContentView = ( RestRequestContentView )views.get( 0 );
		return restRequestContentView.getParamsTable();
	}

	private void verifyParamValues( RestParamsTable table, int rowNumber, String paramName, String value )
	{
		assertThat( ( String )table.getParamsTable().getValueAt( rowNumber, 0 ), is( paramName ) );
		assertThat( ( String )table.getParamsTable().getValueAt( rowNumber, 1 ), is( value ) );
	}

	private void addNewParameter( Component parentPanel, String parameterName, String parameterValue )
	{
		Swinger.getUserWith( parentPanel ).clickOn( AddParamAction.ADD_PARAM_ACTION_NAME )
				.pause( 100 )
				.type( parameterName )
				.pressSimultaneously( VK_ENTER )
				.pause( 100 )
				.type( parameterValue )
				.pressSimultaneously( VK_ENTER )
				.pause( 200 );
	}

	private void openResourceEditor( Window soapUIWindow )
	{
		Navigator navigator = ( Navigator )SwingUtil.lookup( Navigator.NAVIGATOR, soapUIWindow );
		Swinger.getUserWith( navigator ).doubleClickOn( "text:Soapui.org [//soapui.org]" ).pause( 200 );
	}

	private void openMethodEditor( Window soapUIWindow )
	{
		Navigator navigator = ( Navigator )SwingUtil.lookup( Navigator.NAVIGATOR, soapUIWindow );
		Swinger.getUserWith( navigator ).doubleClickOn( "text:Soapui.org" ).pause( 200 );
	}

	private void createNewRestProject( Window soapUI )
	{
		Component file = SwingUtil.lookup( "File", soapUI );
		Swinger swinger = Swinger.getUserWith( file );
		swinger.clickOn( file )
				.pause( 200 )
				.clickOn( "text:New REST Project" )
				.pause( 200 );

		Window newRestProjectDialog = getWindowWithTitle( "New REST Project" );
		assertNotNull( newRestProjectDialog );

		Swinger.getUserWith( newRestProjectDialog )
				.clickOn( "type:com.eviware.soapui.support.components.JUndoableTextField" )
				.type( "http://soapui.org" )
				.pressSimultaneously( VK_ENTER );
	}

	private Window getWindowWithTitle( String title )
	{
		for( Window window : Window.getWindows() )
		{
			if( window instanceof JFrame && ( ( JFrame )window ).getTitle().startsWith( title ) )
			{
				return window;
			}
			else if( window instanceof Dialog && ( ( JDialog )window ).getTitle().startsWith( title ) )
			{
				return window;
			}
		}
		return null;
	}
}
