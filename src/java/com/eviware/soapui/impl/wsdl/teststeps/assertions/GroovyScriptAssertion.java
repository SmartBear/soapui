/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RequestAssertionConfig;
import com.eviware.soapui.impl.wsdl.actions.support.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.log.JLogList;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Assertion performed by a custom Grooy Script
 * 
 * @author ole.matzura
 */

public class GroovyScriptAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion
{
	public static final String ID = "GroovyScriptAssertion";
	public static final String LABEL = "Script Assertion";
	private String scriptText;
	private SoapUIScriptEngine scriptEngine;
	private JDialog dialog;
	private GroovyScriptAssertionPanel groovyScriptAssertionPanel;

	public GroovyScriptAssertion( RequestAssertionConfig assertionConfig, Assertable modelItem )
	{
		super( assertionConfig, modelItem, true, true, true, false );
		
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration());
		scriptText = reader.readString( "scriptText", "" );
      
		scriptEngine = SoapUIScriptEngineRegistry.create( SoapUIScriptEngineRegistry.GROOVY_ID, this );
		scriptEngine.setScript( scriptText );
	}

	@Override
	protected String internalAssertRequest( WsdlMessageExchange messageExchange, SubmitContext context ) throws AssertionException
	{
		return assertScript( messageExchange, context, SoapUI.ensureGroovyLog() );
	}

	private String assertScript( WsdlMessageExchange messageExchange, SubmitContext context, Logger log ) throws AssertionException
	{
		try
		{
			scriptEngine.setVariable("context", context);
			scriptEngine.setVariable("messageExchange", messageExchange);
			scriptEngine.setVariable( "log", log );
			
			Object result = scriptEngine.run();
			return result == null ? null : result.toString();
		}
		catch( Throwable e )
		{
			throw new AssertionException( new AssertionError( e.getMessage() ));
		}
		finally
		{
			scriptEngine.clearVariables();
		}
	}

	@Override
	protected String internalAssertResponse( WsdlMessageExchange messageExchange, SubmitContext context ) throws AssertionException
	{
		return assertScript( messageExchange, context, SoapUI.ensureGroovyLog() );
	}

	@Override
	public boolean configure()
	{
		if( dialog == null )
		{
			dialog = new JDialog( UISupport.getMainFrame(), "Script Assertion", true );
			groovyScriptAssertionPanel = new GroovyScriptAssertionPanel();
			dialog.setContentPane( groovyScriptAssertionPanel );
			UISupport.initDialogActions( dialog, groovyScriptAssertionPanel.getShowOnlineHelpAction(),
						groovyScriptAssertionPanel.getDefaultButton());
			dialog.setSize( 600, 500 );
			dialog.setModal( true );
			dialog.pack();
		}

		UISupport.showDialog( dialog );
		
		setConfiguration( createConfiguration() );
		return true;
	}

	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( "scriptText", scriptText );
		return builder.finish();
	}

	private class GroovyScriptAssertionPanel extends JPanel
	{
		private GroovyEditor editor;
		private JSplitPane mainSplit;
		private JLogList logArea;
		private RunAction runAction = new RunAction();
		private Logger logger;
		private JButton okButton;
		private ShowOnlineHelpAction showOnlineHelpAction;

		public GroovyScriptAssertionPanel()
		{
			super( new BorderLayout() );
			
			buildUI();
			setPreferredSize( new Dimension( 600, 440 ));
			
			logger = Logger.getLogger( "ScriptAssertion." + getName() );
			editor.requestFocusInWindow();
		}

		public void release()
		{
			logArea.release();
			editor.release();
			logger = null;
		}
		
		private void buildUI()
		{
			editor = new GroovyEditor( new ScriptStepGroovyEditorModel( ));
			
			logArea = new JLogList( "Groovy Test Log" );
			logArea.addLogger( "ScriptAssertion." + getName(), true );
			logArea.getLogList().addMouseListener( new MouseAdapter() {

				public void mouseClicked(MouseEvent e)
				{
					if( e.getClickCount() < 2 )
						return;
					
					String value = logArea.getLogList().getSelectedValue().toString();
					if( value == null )
						return;
					
					editor.selectError( value );
				}} );
			
			JScrollPane scrollPane = new JScrollPane( editor );
			scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 0, 3, 0, 3 ), 
						scrollPane.getBorder() ));
			
			mainSplit = UISupport.createVerticalSplit( scrollPane, logArea);
			mainSplit.setDividerLocation( 280 );
			mainSplit.setResizeWeight( 0.8 );
			add( mainSplit, BorderLayout.CENTER );
			add( buildToolbar(), BorderLayout.NORTH );
			add( buildStatusBar(), BorderLayout.SOUTH );
		}
		
		public JButton getDefaultButton()
		{
			return okButton;
		}
		
		public ShowOnlineHelpAction getShowOnlineHelpAction()
		{
			return showOnlineHelpAction;
		}

		private Component buildStatusBar()
		{
			ButtonBarBuilder builder = new ButtonBarBuilder();
			
			showOnlineHelpAction = new ShowOnlineHelpAction( HelpUrls.GROOVYASSERTION_HELP_URL );
			builder.addFixed( UISupport.createToolbarButton( showOnlineHelpAction));
			builder.addGlue();
			okButton = new JButton( new OkAction() );
			builder.addFixed( okButton );
			builder.setBorder( BorderFactory.createEmptyBorder( 0, 3, 3, 3 ));
			return builder.getPanel();
		}

		private JComponent buildToolbar()
		{
			JXToolBar toolBar = UISupport.createToolbar();
			JButton runButton = UISupport.createToolbarButton( runAction );
			toolBar.add( runButton );
			toolBar.add( Box.createHorizontalGlue() );
			JLabel label = new JLabel("<html>Script is invoked with <code>log</code>, <code>context</code> " +
							"and <code>messageExchange</code> variables</html>");
			label.setToolTipText( label.getText() );
			label.setMaximumSize( label.getPreferredSize() );
			
			toolBar.addFixed( label);
			toolBar.addSpace( 3 );
			
			return toolBar;
		}

		private final class OkAction extends AbstractAction
		{
			public OkAction()
			{
			   super( "OK" );	
			}
			
			public void actionPerformed( ActionEvent e )
			{
				dialog.setVisible( false );
			}
		}

		private class ScriptStepGroovyEditorModel implements GroovyEditorModel
		{
			public String[] getKeywords()
			{
				return new String[] {"log", "context", "messageExchange"};
			}

			public Action getRunAction()
			{
				return runAction;
			}

			public String getScript()
			{
				return scriptText;
			}

			public void setScript( String text )
			{
				scriptText = text;
				scriptEngine.setScript( scriptText );
			}

			public Settings getSettings()
			{
				return SoapUI.getSettings();
			}

			public String getScriptName()
			{
				return "Assertion";
			}}
		
		private class RunAction extends AbstractAction
		{
			public RunAction()
			{
				putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_groovy_script.gif" ));
				putValue( Action.SHORT_DESCRIPTION, "Runs this assertion script against the last messageExchange with a mock testContext" );
			}

			public void actionPerformed(ActionEvent e)
			{
				TestStep testStep = ( TestStep ) getAssertable().getModelItem();
				WsdlMessageExchange exchange = null;
				
				if( testStep instanceof WsdlTestRequestStep )
				{
					WsdlTestRequestStep testRequestStep = ( WsdlTestRequestStep ) testStep;
					exchange = new WsdlResponseMessageExchange( testRequestStep.getTestRequest() );
					((WsdlResponseMessageExchange)exchange).setResponse( testRequestStep.getTestRequest().getResponse() );
				}
				
				try
				{
					String result = assertScript( exchange, new WsdlTestRunContext( testStep ), logger );
					UISupport.showInfoMessage( "Script Assertion Passed" + ((result==null) ? "" : ": [" + result + "]" ));
				}
				catch( AssertionException e1 )
				{
					UISupport.showErrorMessage( e1.getMessage() );
				}
				catch( Throwable t )
				{
					SoapUI.logError( t );
					UISupport.showErrorMessage( t.getMessage() );
				}
				
				editor.requestFocusInWindow();
			}
		}
	}

	@Override
	public void release()
	{
		super.release();
		scriptEngine.release();
		
		if( groovyScriptAssertionPanel != null )
			groovyScriptAssertionPanel.release();
	}
}
