/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

public class XPathMockOperationDispatcher extends AbstractMockOperationDispatcher
{
	private GroovyEditor xpathEditor;

	public XPathMockOperationDispatcher( WsdlMockOperation mockOperation )
	{
		super( mockOperation );
	}

	public WsdlMockResponse selectMockResponse( WsdlMockRequest request, WsdlMockResult result )
			throws DispatchException
	{
		XmlObject xmlObject;
		try
		{
			xmlObject = request.getRequestXmlObject();
		}
		catch( XmlException e )
		{
			throw new DispatchException( "Error getting XmlObject for request: " + e );
		}

		String path = getMockOperation().getDispatchPath();
		if( StringUtils.isNullOrEmpty( path ) )
			throw new DispatchException( "Missing dispatch XPath expression" );

		String[] values = XmlUtils.selectNodeValues( xmlObject, path );
		for( String value : values )
		{
			WsdlMockResponse mockResponse = getMockOperation().getMockResponseByName( value );
			if( mockResponse != null )
				return mockResponse;
		}

		return null;
	}

	@Override
	public JComponent buildEditorComponent()
	{
		JPanel xpathEditorPanel = new JPanel( new BorderLayout() );
		DispatchXPathGroovyEditorModel editorModel = new DispatchXPathGroovyEditorModel();
		xpathEditor = new GroovyEditor( editorModel );
		xpathEditorPanel.add( xpathEditor, BorderLayout.CENTER );
		xpathEditorPanel.add( buildXPathEditorToolbar( editorModel ), BorderLayout.PAGE_START );

		return xpathEditorPanel;
	}

	public GroovyEditor getXPathEditor()
	{
		return xpathEditor;
	}

	@Override
	public void release()
	{
		if( xpathEditor != null )
			xpathEditor.release();

		super.release();
	}

	protected JXToolBar buildXPathEditorToolbar( DispatchXPathGroovyEditorModel editorModel )
	{
		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.addSpace( 3 );
		addToolbarActions( editorModel, toolbar );
		toolbar.addGlue();
		toolbar.addFixed( ModelItemDesktopPanel.createActionButton( new ShowOnlineHelpAction(
				HelpUrls.MOCKOPERATION_XPATHDISPATCH_HELP_URL ), true ) );
		return toolbar;
	}

	protected void addToolbarActions( DispatchXPathGroovyEditorModel editorModel, JXToolBar toolbar )
	{
		toolbar.addFixed( UISupport.createToolbarButton( editorModel.getRunAction() ) );
	}

	public static class Factory implements MockOperationDispatchFactory
	{
		public MockOperationDispatcher build( WsdlMockOperation mockOperation )
		{
			return new XPathMockOperationDispatcher( mockOperation );
		}
	}

	public class DispatchXPathGroovyEditorModel implements GroovyEditorModel
	{
		private RunXPathAction runXPathAction = new RunXPathAction();

		public String[] getKeywords()
		{
			return new String[] { "define", "namespace" };
		}

		public Action getRunAction()
		{
			return runXPathAction;
		}

		public String getScript()
		{
			return getMockOperation().getDispatchPath();
		}

		public Settings getSettings()
		{
			return getMockOperation().getSettings();
		}

		public void setScript( String text )
		{
			getMockOperation().setDispatchPath( text );
		}

		public String getScriptName()
		{
			return null;
		}
	}

	private class RunXPathAction extends AbstractAction
	{
		public RunXPathAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_groovy_script.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Evaluates this xpath expression against the latest request" );
		}

		public void actionPerformed( ActionEvent e )
		{
			WsdlMockResult lastMockResult = getMockOperation().getLastMockResult();
			if( lastMockResult == null )
			{
				UISupport.showErrorMessage( "Missing last request to select from" );
				return;
			}

			try
			{
				WsdlMockResponse retVal = selectMockResponse( lastMockResult.getMockRequest(), null );
				UISupport.showInfoMessage( "XPath Selection returned [" + ( retVal == null ? "null" : retVal.getName() )
						+ "]" );
			}
			catch( Exception e1 )
			{
				SoapUI.logError( e1 );
			}
		}
	}
}