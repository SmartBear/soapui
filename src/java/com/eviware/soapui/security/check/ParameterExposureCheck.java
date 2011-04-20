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

package com.eviware.soapui.security.check;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.StrategyTypeConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.security.SecurityCheckedParameter;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestRunnerImpl;
import com.eviware.soapui.support.SecurityCheckUtil;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JStringListFormField;

/**
 * This checks whether any parameters sent in the request are included in the
 * response, If they do appear, this is a good parameter to look at as a
 * possible attack vector for XSS
 * 
 * @author nebojsa.tasic
 */

public class ParameterExposureCheck extends AbstractSecurityCheckWithProperties
{
	public static final String TYPE = "ParameterExposureCheck";
	public static final String NAME = "Parameter Exposure";
	private ParameterExposureCheckConfig parameterExposureCheckConfig;
	StrategyTypeConfig.Enum strategy = StrategyTypeConfig.ONE_BY_ONE;

	String[] defaultParameterExposureStrings = { "<script></script>" };

	public ParameterExposureCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );
		if( config.getConfig() == null || !( config.getConfig() instanceof ParameterExposureCheckConfig ) )
			initConfig();
		else
			parameterExposureCheckConfig = ( ParameterExposureCheckConfig )getConfig().getConfig();
	}

	private void initConfig()
	{
		getConfig().setConfig( ParameterExposureCheckConfig.Factory.newInstance() );
		parameterExposureCheckConfig = ( ParameterExposureCheckConfig )getConfig().getConfig();

		parameterExposureCheckConfig.setParameterExposureStringsArray( defaultParameterExposureStrings );
	}

	@Override
	public void updateSecurityConfig( SecurityCheckConfig config )
	{
		super.updateSecurityConfig( config );

		if( parameterExposureCheckConfig != null )
		{
			parameterExposureCheckConfig = ( ParameterExposureCheckConfig )getConfig().getConfig();
		}
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		PropertyMutation mutation = PropertyMutation.popMutation( context );
		if( mutation != null )
		{
			MessageExchange message = ( MessageExchange )mutation.getTestStep().run( ( TestCaseRunner )securityTestRunner,
					context );
			createMessageExchange( mutation.getMutatedParameters(), message );
		}
	}

	@Override
	public JComponent getComponent()
	{
		return new JLabel(
				"<html><pre>Parameter Exposure <i>Default values applied (can be changed under advanced settings)</i></pre></html>" );
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	protected boolean hasNext( TestStep testStep, SecurityTestRunContext context )
	{
		if( !context.hasProperty( PropertyMutation.REQUEST_MUTATIONS_STACK ) )
		{
			Stack<PropertyMutation> requestMutationsList = new Stack<PropertyMutation>();
			context.put( PropertyMutation.REQUEST_MUTATIONS_STACK, requestMutationsList );
			try
			{
				extractMutations( testStep, context );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
			return true;
		}

		Stack<PropertyMutation> stack = ( Stack<PropertyMutation> )context.get( PropertyMutation.REQUEST_MUTATIONS_STACK );
		if( stack.empty() )
		{
			context.remove( PropertyMutation.REQUEST_MUTATIONS_STACK );
			return false;
		}
		else
		{
			return true;
		}
	}

	private void extractMutations( TestStep testStep, SecurityTestRunContext context )
	{
		strategy = getExecutionStrategy().getStrategy();
		for( String value : parameterExposureCheckConfig.getParameterExposureStringsList() )
		{
			PropertyMutation allAtOncePropertyMutation = new PropertyMutation();
			TestStep testStepCopy = null;
			XmlObjectTreeModel model = null;
			List<SecurityCheckedParameter> scpList = getParameterHolder().getParameterList();
			StringToStringMap stsmap = new StringToStringMap();
			for( SecurityCheckedParameter scp : scpList )
			{

				if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
				{
					stsmap = new StringToStringMap();
					model = SecurityCheckUtil.getXmlObjectTreeModel( testStep, scp );
					testStepCopy = SecurityTestRunnerImpl.cloneTestStepForSecurityCheck( ( WsdlTestStep )testStep );
				}
				else
				{
					if( model == null )
					{
						model = SecurityCheckUtil.getXmlObjectTreeModel( testStep, scp );
					}
					if( testStepCopy == null )
					{
						testStepCopy = SecurityTestRunnerImpl.cloneTestStepForSecurityCheck( ( WsdlTestStep )testStep );
					}
				}

				// if parameter is xml
				if( scp.isChecked() && scp.getXpath().trim().length() > 0 )
				{
					XmlTreeNode[] treeNodes = null;

					treeNodes = model.selectTreeNodes( context.expand( scp.getXpath() ) );

					if( treeNodes.length > 0 )
					{
						XmlTreeNode mynode = treeNodes[0];

						// work only for simple types
						if( mynode.isLeaf() )
						{
							mynode.setValue( 1, value );

							if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
							{
								PropertyMutation pm = new PropertyMutation();
								pm.setPropertyName( scp.getName() );
								pm.setPropertyValue( model.getXmlObject().toString() );
								stsmap.put( scp.getLabel(), mynode.getNodeText() );
								pm.setMutatedParameters( stsmap );
								pm.updateRequestProperty( testStepCopy );
								pm.setTestStep( testStepCopy );
								pm.addMutation( context );
							}
							else
							{
								allAtOncePropertyMutation.setPropertyName( scp.getName() );
								allAtOncePropertyMutation.setPropertyValue( model.getXmlObject().toString() );
								stsmap.put( scp.getLabel(), mynode.getNodeText() );
								allAtOncePropertyMutation.setMutatedParameters( stsmap );
								allAtOncePropertyMutation.updateRequestProperty( testStepCopy );
								allAtOncePropertyMutation.setTestStep( testStepCopy );

							}
						}
					}
				}
				// non xml parameter
				else
				{
					if( strategy.equals( StrategyTypeConfig.ONE_BY_ONE ) )
					{
						PropertyMutation pm = new PropertyMutation();
						pm.setPropertyName( scp.getName() );
						pm.setPropertyValue( value );
						stsmap.put( scp.getLabel(), value );
						pm.setMutatedParameters( stsmap );
						pm.updateRequestProperty( testStepCopy );
						pm.setTestStep( testStepCopy );
						pm.addMutation( context );
					}
					else
					{
						allAtOncePropertyMutation.setPropertyName( scp.getName() );
						allAtOncePropertyMutation.setPropertyValue( value );
						stsmap.put( scp.getLabel(), value );
						allAtOncePropertyMutation.setMutatedParameters( stsmap );
						allAtOncePropertyMutation.updateRequestProperty( testStepCopy );
						allAtOncePropertyMutation.setTestStep( testStepCopy );
					}
				}

			}

			if( strategy.equals( StrategyTypeConfig.ALL_AT_ONCE ) )
			{
				allAtOncePropertyMutation.addMutation( context );
			}

		}

	}

	@Override
	public String getConfigDescription()
	{
		return "Configures parameter exposure security check";
	}

	@Override
	public String getConfigName()
	{
		return "Parameter Exposure Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}

	@Override
	public JComponent getAdvancedSettingsPanel()
	{
		JFormDialog dialog = ( JFormDialog )ADialogBuilder.buildDialog( AdvancedSettings.class );
		JStringListFormField stringField = ( JStringListFormField )dialog
				.getFormField( AdvancedSettings.PARAMETER_EXPOSURE_STRINGS );
		stringField.setOptions( parameterExposureCheckConfig.getParameterExposureStringsList().toArray() );
		stringField.setProperty( "dimension", new Dimension( 470, 150 ) );
		stringField.getComponent().addPropertyChangeListener( "options", new PropertyChangeListener()
		{

			@Override
			public void propertyChange( PropertyChangeEvent evt )
			{
				String[] newOptions = ( String[] )evt.getNewValue();
				String[] oldOptions = ( String[] )evt.getOldValue();
				// added
				if( newOptions.length > oldOptions.length )
				{
					// new element is always added to the end
					String[] newValue = ( String[] )evt.getNewValue();
					String itemToAdd = newValue[newValue.length - 1];
					parameterExposureCheckConfig.addParameterExposureStrings( itemToAdd );
				}
				// removed
				if( newOptions.length < oldOptions.length )
				{
					/*
					 * items with same index should me same. first one in oldOptions
					 * that does not match is element that is removed.
					 */
					for( int cnt = 0; cnt < oldOptions.length; cnt++ )
					{
						if( cnt < newOptions.length )
						{
							if( newOptions[cnt] != oldOptions[cnt] )
							{
								parameterExposureCheckConfig.removeParameterExposureStrings( cnt );
								break;
							}
						}
						else
						{
							// this is border case, last lement in array is removed.
							parameterExposureCheckConfig.removeParameterExposureStrings( oldOptions.length - 1 );
						}
					}
				}
			}
		} );

		return dialog.getPanel();
	}

	@AForm( description = "Parameter Exposure", name = "Parameter Exposure" )
	protected interface AdvancedSettings
	{

		@AField( description = "Parameter Exposure Values", name = "###Parameter Exposure", type = AFieldType.STRINGLIST )
		public final static String PARAMETER_EXPOSURE_STRINGS = "###Parameter Exposure";

	}
}
