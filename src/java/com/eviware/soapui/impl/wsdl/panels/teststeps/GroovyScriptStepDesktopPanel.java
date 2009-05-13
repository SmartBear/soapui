/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListModel;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunContext;
import com.eviware.soapui.impl.wsdl.panels.support.MockTestRunner;
import com.eviware.soapui.impl.wsdl.panels.support.TestRunComponentEnabler;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditorModel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.support.ListDataChangeListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JEditorStatusBarWithProgress;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.log.JLogList;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

/**
 * DesktopPanel for WsdlGroovyTestSteps
 * 
 * @author Ole.Matzura
 */

public class GroovyScriptStepDesktopPanel extends ModelItemDesktopPanel<WsdlGroovyScriptTestStep> implements
		PropertyChangeListener
{
	private final WsdlGroovyScriptTestStep groovyStep;
	private GroovyEditor editor;
	private JLogList logArea;
	private Logger logger;
	private TestRunComponentEnabler componentEnabler;
	private RunAction runAction = new RunAction();
	private JEditorStatusBarWithProgress statusBar;
	private SettingsListener settingsListener;
	private JComponentInspector<JComponent> logInspector;
	public boolean updating;
	private JInspectorPanel inspectorPanel;

	public GroovyScriptStepDesktopPanel( WsdlGroovyScriptTestStep groovyStep )
	{
		super( groovyStep );
		this.groovyStep = groovyStep;
		componentEnabler = new TestRunComponentEnabler( groovyStep.getTestCase() );

		buildUI();
		setPreferredSize( new Dimension( 600, 440 ) );

		logger = Logger.getLogger( groovyStep.getName() + "#" + hashCode() );

		addFocusListener( new FocusAdapter()
		{

			public void focusGained( FocusEvent e )
			{
				editor.requestFocusInWindow();
			}

		} );

		groovyStep.addPropertyChangeListener( this );
	}

	protected GroovyEditor getEditor()
	{
		return editor;
	}

	private void buildUI()
	{
		editor = new GroovyEditor( new ScriptStepGroovyEditorModel() );

		logArea = new JLogList( "Groovy Test Log" );
		logArea.addLogger( groovyStep.getName() + "#" + hashCode(), true );
		logArea.getLogList().addMouseListener( new MouseAdapter()
		{

			public void mouseClicked( MouseEvent e )
			{
				if( e.getClickCount() < 2 )
					return;

				String value = logArea.getLogList().getSelectedValue().toString();
				if( value == null )
					return;

				editor.selectError( value );
			}
		} );

		logArea.getLogList().getModel().addListDataListener( new ListDataChangeListener()
		{

			@Override
			public void dataChanged( ListModel model )
			{
				logInspector.setTitle( "Log Output (" + model.getSize() + ")" );

			}
		} );

		inspectorPanel = JInspectorPanelFactory.build( editor );
		logInspector = inspectorPanel.addInspector( new JComponentInspector<JComponent>( logArea, "Log Output (0)",
				"Groovy Log output for this script", true ) );
		inspectorPanel.setDefaultDividerLocation( 0.8F );
		inspectorPanel.activate( logInspector );
		add( inspectorPanel.getComponent(), BorderLayout.CENTER );
		add( buildToolbar(), BorderLayout.NORTH );
		add( buildStatusBar(), BorderLayout.SOUTH );

		componentEnabler.add( editor );
	}

	private Component buildStatusBar()
	{
		statusBar = new JEditorStatusBarWithProgress( editor );
		return statusBar;
	}

	private JComponent buildToolbar()
	{
		JXToolBar toolBar = UISupport.createToolbar();
		JButton runButton = UISupport.createToolbarButton( runAction );
		toolBar.add( runButton );
		toolBar.add( Box.createHorizontalGlue() );
		JLabel label = new JLabel( "<html>Script is invoked with <code>log</code>, <code>context</code> "
				+ "and <code>testRunner</code> variables</html>" );
		label.setToolTipText( label.getText() );
		label.setMaximumSize( label.getPreferredSize() );

		toolBar.add( label );
		toolBar.addRelatedGap();
		toolBar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( HelpUrls.GROOVYSTEPEDITOR_HELP_URL ) ) );

		componentEnabler.add( runButton );

		return toolBar;
	}

	public boolean onClose( boolean canCancel )
	{
		componentEnabler.release();
		editor.release();
		SoapUI.getSettings().removeSettingsListener( settingsListener );
		logger.removeAllAppenders();
		logger = null;
		logArea.release();
		inspectorPanel.release();
		return super.release();
	}

	public JComponent getComponent()
	{
		return this;
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return modelItem == groovyStep || modelItem == groovyStep.getTestCase()
				|| modelItem == groovyStep.getTestCase().getTestSuite()
				|| modelItem == groovyStep.getTestCase().getTestSuite().getProject();
	}

	private class ScriptStepGroovyEditorModel implements GroovyEditorModel
	{
		public String[] getKeywords()
		{
			return new String[] { "log", "context", "testRunner" };
		}

		public Action getRunAction()
		{
			return runAction;
		}

		public String getScript()
		{
			return groovyStep.getScript();
		}

		public void setScript( String text )
		{
			if( updating )
				return;

			updating = true;
			groovyStep.setScript( text );
			updating = false;
		}

		public Settings getSettings()
		{
			return SoapUI.getSettings();
		}

		public String getScriptName()
		{
			return null;
		}
	}

	private class RunAction extends AbstractAction
	{
		public RunAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/run_groovy_script.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Runs this script using a mock testRunner and testContext" );
		}

		public void actionPerformed( ActionEvent e )
		{
			MockTestRunner mockTestRunner = new MockTestRunner( groovyStep.getTestCase(), logger );
			statusBar.setIndeterminate( true );
			WsdlTestStepResult result = ( WsdlTestStepResult )groovyStep.run( mockTestRunner, new MockTestRunContext(
					mockTestRunner, groovyStep ) );
			statusBar.setIndeterminate( false );

			Throwable er = result.getError();
			if( er != null )
			{
				String message = er.getMessage();

				// ugly...
				editor.selectError( message );

				UISupport.showErrorMessage( er.toString() );
				editor.requestFocus();
			}
		}
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( "script" ) && !updating )
		{
			updating = true;
			editor.getEditArea().setText( ( String )evt.getNewValue() );
			updating = false;
		}
	}
}
