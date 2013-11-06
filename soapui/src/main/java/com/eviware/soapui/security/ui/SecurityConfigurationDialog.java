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

package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.model.security.SecurityParametersTableModel;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.security.assertion.SecurityAssertionPanel;
import com.eviware.soapui.security.scan.AbstractSecurityScanWithProperties;
import com.eviware.soapui.security.ui.SecurityConfigurationDialogBuilder.Strategy;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.XFormRadioGroup;
import com.eviware.x.impl.swing.JFormDialog;

public class SecurityConfigurationDialog extends SimpleDialog
{
	private SecurityScan securityCheck;
	private boolean result;
	private JTabbedPane tabs;
	private SecurityCheckedParametersTablePanel parametersTable;
	private SecurityAssertionPanel securityAssertionPanel;
	private XFormDialog strategyDialog;

	public SecurityConfigurationDialog( SecurityScan securityCheck )
	{
		super( securityCheck.getName(), securityCheck.getDescription(), securityCheck.getHelpURL() );

		this.securityCheck = securityCheck;
	}

	public SecurityScan getSecurityScan()
	{
		return securityCheck;
	}

	@Override
	protected Component buildContent()
	{
		JPanel mainPanel = new JPanel();

		if( securityCheck instanceof AbstractSecurityScanWithProperties )
		{
			mainPanel.setLayout( new BoxLayout( mainPanel, BoxLayout.Y_AXIS ) );
			mainPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

			JPanel topPanel = UISupport.createEmptyPanel( 0, 0, 10, 0 );
			topPanel.add( buildParametersTable(), BorderLayout.CENTER );

			JPanel p = UISupport.createEmptyPanel( 5, 0, 5, 0 );
			JLabel jLabel = new JLabel( "Parameters:" );
			jLabel.setPreferredSize( new Dimension( 72, 20 ) );
			p.add( jLabel, BorderLayout.NORTH );

			topPanel.add( p, BorderLayout.NORTH );

			JComponent component = securityCheck.getComponent();
			if( component != null )
				topPanel.add( component, BorderLayout.SOUTH );

			mainPanel.add( topPanel );
		}
		else
		{
			mainPanel.setLayout( new BorderLayout() );
			mainPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

			JComponent component = securityCheck.getComponent();
			if( component != null )
			{
				JPanel topPanel = UISupport.createEmptyPanel( 0, 0, 10, 10 );
				topPanel.add( component, BorderLayout.SOUTH );
				mainPanel.add( topPanel, BorderLayout.NORTH );
			}
		}

		Dimension prefSize = mainPanel.getPreferredSize();
		int prefHeight = ( int )( prefSize.getHeight() + 170 );
		int prefWidth = ( int )Math.max( prefSize.getWidth(), 600 );

		mainPanel.setPreferredSize( new Dimension( prefWidth, prefHeight ) );

		mainPanel.add( buildTabs(), BorderLayout.CENTER );

		return mainPanel;
	}

	protected Component buildParametersTable()
	{
		parametersTable = new SecurityCheckedParametersTablePanel( new SecurityParametersTableModel(
				( ( AbstractSecurityScanWithProperties )securityCheck ).getParameterHolder() ), securityCheck.getTestStep()
				.getProperties(), ( AbstractSecurityScanWithProperties )securityCheck );

		parametersTable.setPreferredSize( new Dimension( 400, 150 ) );
		parametersTable.setMinimumSize( new Dimension( 400, 150 ) );
		parametersTable.setMaximumSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );

		return parametersTable;
	}

	protected Component buildTabs()
	{
		tabs = new JTabbedPane();
		securityAssertionPanel = new SecurityAssertionPanel( securityCheck );
		tabs.addTab( "Assertions", securityAssertionPanel );
		tabs.addTab( "Strategy", buildStrategyTab() );

		JComponent advancedSettingsPanel = securityCheck.getAdvancedSettingsPanel();
		if( advancedSettingsPanel != null )
			tabs.addTab( "Advanced", new JScrollPane( advancedSettingsPanel ) );

		tabs.setMaximumSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
		tabs.setPreferredSize( new Dimension( 400, 150 ) );

		return tabs;
	}

	protected Component buildStrategyTab()
	{
		strategyDialog = ADialogBuilder.buildDialog( SecurityConfigurationDialogBuilder.Strategy.class, null );

		XFormRadioGroup strategy = ( XFormRadioGroup )strategyDialog.getFormField( Strategy.STRATEGY );
		final String[] strategyOptions = new String[] { "One by One", "All At Once" };
		strategy.setOptions( strategyOptions );

		if( securityCheck.getExecutionStrategy().getStrategy() == StrategyTypeConfig.NO_STRATEGY )
			strategy.setEnabled( false );
		else
		{
			if( securityCheck.getExecutionStrategy().getStrategy() == StrategyTypeConfig.ONE_BY_ONE )
				strategy.setValue( strategyOptions[0] );
			else
				strategy.setValue( strategyOptions[1] );
		}

		// default is ONE_BY_ONE
		if( securityCheck.getExecutionStrategy().getImmutable() )
		{
			strategy.setDisabled();
		}

		strategy.addFormFieldListener( new XFormFieldListener()
		{
			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{

				if( newValue.equals( strategyOptions[0] ) )
					securityCheck.getExecutionStrategy().setStrategy( StrategyTypeConfig.ONE_BY_ONE );
				else
					securityCheck.getExecutionStrategy().setStrategy( StrategyTypeConfig.ALL_AT_ONCE );

			}
		} );

		XFormField delay = strategyDialog.getFormField( Strategy.DELAY );
		delay.setValue( String.valueOf( securityCheck.getExecutionStrategy().getDelay() ) );

		delay.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				try
				{
					if( "".equals( newValue ) )
						return;
					Integer.valueOf( newValue );
					securityCheck.getExecutionStrategy().setDelay( Integer.valueOf( newValue ) );
				}
				catch( Exception e )
				{
					UISupport.showErrorMessage( "Delay value must be integer number" );
				}
			}
		} );
		XFormField applyToFailedTests = strategyDialog.getFormField( Strategy.APPLY_TO_FAILED_STEPS );
		applyToFailedTests.setValue( String.valueOf( securityCheck.isApplyForFailedStep() ) );
		applyToFailedTests.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				securityCheck.setApplyForFailedTestStep( Boolean.parseBoolean( newValue ) );
			}
		} );
		XFormField runOnlyOnce = strategyDialog.getFormField( Strategy.RUN_ONLY_ONCE );
		runOnlyOnce.setValue( String.valueOf( securityCheck.isRunOnlyOnce() ) );
		runOnlyOnce.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				securityCheck.setRunOnlyOnce( Boolean.parseBoolean( newValue ) );
			}
		} );

		return ( ( JFormDialog )strategyDialog ).getPanel();
	}

	@Override
	protected boolean handleOk()
	{
		result = true;
		return true;
	}

	public boolean configure()
	{
		result = false;
		setVisible( true );
		return result;
	}

	public void release()
	{
		if( strategyDialog != null )
		{
			strategyDialog.release();
			strategyDialog = null;
		}

		securityAssertionPanel.release();
		securityAssertionPanel = null;
		securityCheck = null;
		tabs.removeAll();
		tabs = null;
		dispose();
	}
}
