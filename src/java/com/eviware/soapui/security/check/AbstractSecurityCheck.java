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
package com.eviware.soapui.security.check;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.panels.SecurityChecksPanel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public abstract class AbstractSecurityCheck extends SecurityCheck
{
	// configuration of specific request modification
	protected SecurityCheckConfig config;
	protected String startupScript;
	protected String tearDownScript;
	protected SoapUIScriptEngine scriptEngine;
	private boolean disabled = false;
	protected JPanel panel;
	protected SimpleForm form;
	protected JDialog dialog;
	private boolean configureResult;

	// private
	public AbstractSecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon, Securable securable )
	{
		super( config, parent, icon, securable );
		this.config = config;
		this.startupScript = config.getSetupScript() != null ? config.getSetupScript().getStringValue() : "";
		this.tearDownScript = config.getTearDownScript() != null ? config.getTearDownScript().getStringValue() : "";
		scriptEngine = SoapUIScriptEngineRegistry.create( this );
	}

	// TODO check if should exist and what to do with securable
	public AbstractSecurityCheck( SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon, null );
		this.config = config;
		this.startupScript = config.getSetupScript() != null ? config.getSetupScript().getStringValue() : "";
		this.tearDownScript = config.getTearDownScript() != null ? config.getTearDownScript().getStringValue() : "";
		scriptEngine = SoapUIScriptEngineRegistry.create( this );
	}

	abstract protected void execute( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog );

	@Override
	abstract public void analyze( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog );

	@Override
	public void run( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog )
	{
		runStartupScript( testStep );
		execute( testStep, context, securityTestLog );
		sensitiveInfoCheck( testStep, context, securityTestLog );
		runTearDownScript( testStep );
	}

	private void sensitiveInfoCheck( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog )
	{
		if( this instanceof SensitiveInformationCheckable )
		{
			( ( SensitiveInformationCheckable )this ).checkForSensitiveInformationExposure( testStep, context,
					securityTestLog );
		}
	}

	@Override
	public boolean configure()
	{
		if( dialog == null )
		{
			buildDialog();
		}

		UISupport.showDialog( dialog );
		return configureResult;
	}

	protected void buildDialog()
	{
		dialog = new JDialog( UISupport.getMainFrame(), getTitle(), true );

		JPanel contentPanel = ( JPanel )getComponent();
		ButtonBarBuilder builder = new ButtonBarBuilder();

		ShowOnlineHelpAction showOnlineHelpAction = new ShowOnlineHelpAction( HelpUrls.XPATHASSERTIONEDITOR_HELP_URL );
		builder.addFixed( UISupport.createToolbarButton( showOnlineHelpAction ) );
		builder.addGlue();

		JButton okButton = new JButton( new OkAction() );
		builder.addFixed( okButton );
		builder.addRelatedGap();
		builder.addFixed( new JButton( new CancelAction() ) );

		builder.setBorder( BorderFactory.createEmptyBorder( 1, 5, 5, 5 ) );

		contentPanel.add( builder.getPanel(), BorderLayout.SOUTH );
		dialog.setContentPane( contentPanel );
		dialog.setSize( 600, 500 );
		dialog.setModal( true );
		dialog.pack();
		UISupport.initDialogActions( dialog, showOnlineHelpAction, okButton );

	}

	public class OkAction extends AbstractAction
	{
		public OkAction()
		{
			// TODO save the config
			super( "Save" );
			configureResult = true;
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			dialog.setVisible( false );
		}
	}

	public class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super( "Cancel" );
			configureResult = false;
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			dialog.setVisible( false );
		}
	}

	private void runTearDownScript( TestStep testStep )
	{
		scriptEngine.setScript( tearDownScript );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			scriptEngine.clearVariables();
		}

	}

	private void runStartupScript( TestStep testStep )
	{
		scriptEngine.setScript( startupScript );
		scriptEngine.setVariable( "testStep", testStep );
		scriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		// scriptEngine.setVariable( "context", context );

		try
		{
			scriptEngine.run();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			scriptEngine.clearVariables();
		}
	}

	@Override
	public SecurityCheckConfig getConfig()
	{
		return config;
	}

	@Override
	public List<? extends ModelItem> getChildren()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription()
	{
		return config.getDescription();
	}

	@Override
	public ImageIcon getIcon()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId()
	{
		return config.getId();
	}

	@Override
	public String getName()
	{
		return config.getName();
	}

	@Override
	public void setName( String arg0 )
	{
		config.setName( arg0 );
	}

	@Override
	public ModelItem getParent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDisabled()
	{
		return disabled;
	}

	@Override
	public void setDisabled( boolean disabled )
	{
		this.disabled = disabled;

	}

	@Override
	public String getTitle()
	{
		return "";
	}
	public static boolean isSecurable( TestStep testStep ) {
		if( testStep != null && testStep instanceof HttpTestRequestStep )
		{
			return true;
		} else {
			return false;
		}
	}


}
