/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.*;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.scripting.ScriptEnginePool;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ScriptMockOperationDispatcher extends AbstractMockOperationDispatcher implements PropertyChangeListener
{
	private ScriptEnginePool scriptEnginePool;
	private GroovyEditor groovyEditor;
	private JPanel groovyEditorPanel;

	public ScriptMockOperationDispatcher( MockOperation mockOperation )
	{
		super( mockOperation );

		scriptEnginePool = new ScriptEnginePool( mockOperation );
		scriptEnginePool.setScript( mockOperation.getScript() );

		mockOperation.addPropertyChangeListener( AbstractMockOperation.DISPATCH_PATH_PROPERTY, this );
	}

	public MockResponse selectMockResponse( MockRequest request, MockResult result )
			throws DispatchException
	{
		String dispatchScript = getMockOperation().getScript();
		if( StringUtils.hasContent( dispatchScript ) )
		{
			SoapUIScriptEngine scriptEngine = scriptEnginePool.getScriptEngine();

			try
			{
				MockService mockService = getMockOperation().getMockService();
				MockRunner mockRunner = mockService.getMockRunner();
				MockRunContext context = mockRunner == null ? new WsdlMockRunContext( mockService, null ) : mockRunner
						.getMockContext();

				scriptEngine.setVariable( "context", context );
				scriptEngine.setVariable( "requestContext", request == null ? null : request.getRequestContext() );
				scriptEngine.setVariable( "mockRequest", request );
				scriptEngine.setVariable( "mockOperation", getMockOperation() );
				scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );

				scriptEngine.setScript( dispatchScript );
				Object retVal = scriptEngine.run();
				return getMockOperation().getMockResponseByName( String.valueOf( retVal ) );
			}
			catch( Throwable e )
			{
				SoapUI.logError( e );
				throw new DispatchException( "Failed to dispatch using script; " + e );
			}
			finally
			{
				scriptEnginePool.returnScriptEngine( scriptEngine );
			}
		}

		return null;
	}

	@Override
	public void release()
	{
		scriptEnginePool.release();

		releaseEditorComponent();

		getMockOperation().removePropertyChangeListener( AbstractMockOperation.DISPATCH_PATH_PROPERTY, this );

		super.release();
	}

	@Override
	public JComponent getEditorComponent()
	{
		if( groovyEditorPanel == null )
		{
			groovyEditorPanel = new JPanel( new BorderLayout() );
			DispatchScriptGroovyEditorModel editorModel = new DispatchScriptGroovyEditorModel();
			groovyEditor = ( GroovyEditor )UISupport.getEditorFactory().buildGroovyEditor( editorModel );
			groovyEditorPanel.add( groovyEditor, BorderLayout.CENTER );
			groovyEditorPanel.add( buildGroovyEditorToolbar( editorModel ), BorderLayout.PAGE_START );
		}

		return groovyEditorPanel;
	}

	@Override
	public void releaseEditorComponent()
	{
		if( groovyEditor != null )
			groovyEditor.release();

		groovyEditor = null;
		groovyEditorPanel = null;

		super.releaseEditorComponent();
	}

	protected JXToolBar buildGroovyEditorToolbar( DispatchScriptGroovyEditorModel editorModel )
	{
		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.addSpace( 3 );
		toolbar.addFixed( UISupport.createToolbarButton( editorModel.getRunAction() ) );
		toolbar.addGlue();

		JLabel label = new JLabel( "<html>Script is invoked with <code>log</code>, <code>context</code>, "
				+ "<code>requestContext</code>, <code>mockRequest</code> and <code>mockOperation</code> variables</html>" );
		label.setToolTipText( label.getText() );
		label.setMaximumSize( label.getPreferredSize() );

		toolbar.add( label );
		toolbar.addFixed( ModelItemDesktopPanel.createActionButton( new ShowOnlineHelpAction(
				HelpUrls.MOCKOPERATION_SCRIPTDISPATCH_HELP_URL ), true ) );
		return toolbar;
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		scriptEnginePool.setScript( String.valueOf( evt.getNewValue() ) );
	}

	public static class Factory implements MockOperationDispatchFactory
	{
		public MockOperationDispatcher build( MockOperation mockOperation )
		{
			return new ScriptMockOperationDispatcher( mockOperation );
		}
	}

	public class DispatchScriptGroovyEditorModel implements GroovyEditorModel
	{
		private RunScriptAction runScriptAction = new RunScriptAction();

		public String[] getKeywords()
		{
			return new String[] { "mockRequest", "context", "requestContext", "log", "mockOperation" };
		}

		public Action getRunAction()
		{
			return runScriptAction;
		}

		public String getScript()
		{
			return getMockOperation().getScript();
		}

		public Settings getSettings()
		{
			return getMockOperation().getSettings();
		}

		public void setScript( String text )
		{
			getMockOperation().setScript( text );
		}

		public String getScriptName()
		{
			return "Dispatch";
		}

		public void addPropertyChangeListener( PropertyChangeListener listener )
		{
		}

		public void removePropertyChangeListener( PropertyChangeListener listener )
		{
		}

		public ModelItem getModelItem()
		{
			return getMockOperation();
		}
	}

	private class RunScriptAction extends AbstractAction
	{
		public RunScriptAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_groovy_script.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Runs this script using a mockRequest and context" );
		}

		public void actionPerformed( ActionEvent e )
		{
			MockResult lastMockResult = getMockOperation().getLastMockResult();
			MockRequest mockRequest = lastMockResult == null ? null : lastMockResult.getMockRequest();

			try
			{
				MockResponse retVal = selectMockResponse( mockRequest, null );
				UISupport.showInfoMessage( "Script returned [" + ( retVal == null ? "null" : retVal.getName() ) + "]" );
			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( e1 );
			}
		}
	}
}
