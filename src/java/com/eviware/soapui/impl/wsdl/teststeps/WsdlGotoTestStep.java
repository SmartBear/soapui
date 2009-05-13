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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.GotoConditionConfig;
import com.eviware.soapui.config.GotoConditionTypeConfig;
import com.eviware.soapui.config.GotoStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

/**
 * TestStep that moves execution to another step based on the contents of a XML
 * Property
 * 
 * @author ole.matzura
 */

public class WsdlGotoTestStep extends WsdlTestStepWithProperties implements XPathReferenceContainer
{
	private GotoStepConfig gotoStepConfig;
	private List<GotoCondition> conditions = new ArrayList<GotoCondition>();
	private boolean canceled;

	private final static Logger log = Logger.getLogger( WsdlGotoTestStep.class );

	public WsdlGotoTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, true, forLoadTest );

		if( !forLoadTest )
		{
			setIcon( UISupport.createImageIcon( "/goto.gif" ) );
		}
	}

	@Override
	public void afterLoad()
	{
		TestStepConfig config = getConfig();

		if( config.getConfig() == null )
		{
			gotoStepConfig = ( GotoStepConfig )config.addNewConfig().changeType( GotoStepConfig.type );
		}
		else
		{
			gotoStepConfig = ( GotoStepConfig )config.getConfig().changeType( GotoStepConfig.type );
			for( int c = 0; c < gotoStepConfig.sizeOfConditionArray(); c++ )
			{
				conditions.add( new GotoCondition( gotoStepConfig.getConditionArray( c ) ) );
			}
		}

		super.afterLoad();
	}

	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );

		gotoStepConfig = ( GotoStepConfig )config.getConfig().changeType( GotoStepConfig.type );
		for( int c = 0; c < gotoStepConfig.sizeOfConditionArray(); c++ )
		{
			conditions.get( c ).setConfig( gotoStepConfig.getConditionArray( c ) );
		}
	}

	public TestStepResult run( TestRunner runner, TestRunContext context )
	{
		WsdlTestStepResult result = new WsdlTestStepResult( this );
		canceled = false;

		result.startTimer();

		HttpRequestTestStep<?> previousStep = getTestCase().findPreviousStepOfType( this, HttpRequestTestStep.class );

		if( previousStep == null )
		{
			result.stopTimer();
			result.addMessage( "Failed to find previous request step from [" + getName() + "]" );
			result.setStatus( TestStepStatus.FAILED );
			return result;
		}

		GotoCondition target = runConditions( previousStep, context );
		if( target == null )
		{
			result.addMessage( "Missing matching condition, moving on." );
		}
		else
		{
			String targetStepName = target.getTargetStep();
			result.addMessage( "Matched condition [" + targetStepName + "], transferring to [" + targetStepName + "]" );
			runner.gotoStep( runner.getTestCase().getTestStepIndexByName( targetStepName ) );
		}

		result.stopTimer();
		result.setStatus( TestStepStatus.OK );
		return result;
	}

	public GotoCondition runConditions( HttpRequestTestStep<?> previousStep, TestRunContext context )
	{
		for( GotoCondition condition : conditions )
		{
			if( canceled )
				break;

			try
			{
				if( condition.evaluate( previousStep, context ) )
				{
					return condition;
				}
			}
			catch( Exception e )
			{
				log.error( "Error making condition " + condition.getName() + "; " + e );
			}
		}

		return null;
	}

	public boolean cancel()
	{
		canceled = true;
		return canceled;
	}

	public int getConditionCount()
	{
		return conditions.size();
	}

	public GotoCondition getConditionAt( int index )
	{
		return conditions.get( index );
	}

	public GotoCondition addCondition( String name )
	{
		GotoCondition condition = new GotoCondition( gotoStepConfig.addNewCondition() );
		condition.setName( name );
		condition.setType( GotoConditionTypeConfig.XPATH.toString() );
		conditions.add( condition );
		return condition;
	}

	public void removeConditionAt( int index )
	{
		conditions.remove( index );
		gotoStepConfig.removeCondition( index );
	}

	public void release()
	{
		super.release();

		for( GotoCondition condition : conditions )
		{
			condition.release();
		}
	}

	public class GotoCondition implements PropertyChangeListener
	{
		public final static String TARGET_STEP_PROPERTY = "target_step";

		private GotoConditionConfig conditionConfig;
		private TestStep currentStep;
		private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );

		public GotoCondition( GotoConditionConfig conditionConfig )
		{
			this.conditionConfig = conditionConfig;
			initListeners();
		}

		public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
		{
			propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
		}

		public void addPropertyChangeListener( PropertyChangeListener listener )
		{
			propertyChangeSupport.addPropertyChangeListener( listener );
		}

		public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
		{
			propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
		}

		public void removePropertyChangeListener( PropertyChangeListener listener )
		{
			propertyChangeSupport.removePropertyChangeListener( listener );
		}

		private void initListeners()
		{
			release();

			if( getTargetStep() != null )
			{
				int index = getTestCase().getTestStepIndexByName( getTargetStep() );
				if( index != -1 )
				{
					currentStep = getTestCase().getTestStepAt( index );
					currentStep.addPropertyChangeListener( TestStep.NAME_PROPERTY, this );
				}
			}
		}

		public void release()
		{
			if( currentStep != null )
				currentStep.removePropertyChangeListener( this );
		}

		public boolean evaluate( HttpRequestTestStep<?> previousStep, TestRunContext context ) throws Exception
		{
			if( getExpression() == null || getExpression().trim().length() == 0 )
				throw new Exception( "Missing expression in condition [" + getName() + "]" );

			if( getTargetStep() == null || getTargetStep().trim().length() == 0 )
				throw new Exception( "Missing target step in condition [" + getName() + "]" );

			if( getType().equals( GotoConditionTypeConfig.XPATH.toString() ) )
			{
				AbstractHttpRequest<?> testRequest = previousStep.getHttpRequest();
				XmlObject xmlObject = XmlObject.Factory.parse( testRequest.getResponseContentAsXml() );

				String expression = PropertyExpansionUtils.expandProperties( context, getExpression() );
				XmlObject[] selectPath = xmlObject.selectPath( expression );
				if( selectPath.length == 1 && selectPath[0] instanceof XmlBoolean )
				{
					if( ( ( XmlBoolean )selectPath[0] ).getBooleanValue() )
					{
						return true;
					}
				}
			}
			else
			{
				log.error( "Unkown condition type: " + getType() );
			}

			return false;
		}

		protected void setConfig( GotoConditionConfig conditionConfig )
		{
			this.conditionConfig = conditionConfig;
		}

		public String getType()
		{
			return conditionConfig.getType();
		}

		public String getName()
		{
			return conditionConfig.getName();
		}

		public String getExpression()
		{
			return conditionConfig.getExpression();
		}

		public String getTargetStep()
		{
			return conditionConfig.getTargetStep();
		}

		public void setType( String type )
		{
			conditionConfig.setType( type );
		}

		public void setName( String name )
		{
			conditionConfig.setName( name );
		}

		public void setExpression( String expression )
		{
			conditionConfig.setExpression( expression );
		}

		public void setTargetStep( String targetStep )
		{
			String oldStep = getTargetStep();
			conditionConfig.setTargetStep( targetStep );
			initListeners();
			propertyChangeSupport.firePropertyChange( TARGET_STEP_PROPERTY, oldStep, targetStep );
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			conditionConfig.setTargetStep( evt.getNewValue().toString() );
			propertyChangeSupport.firePropertyChange( TARGET_STEP_PROPERTY, evt.getOldValue(), evt.getNewValue() );
		}

		public TestProperty getSourceProperty()
		{
			HttpRequestTestStep<?> previousStep = ( HttpRequestTestStep )getTestCase().findPreviousStepOfType(
					WsdlGotoTestStep.this, HttpRequestTestStep.class );
			return previousStep == null ? null : previousStep.getProperty( "Response" );
		}
	}

	public boolean hasProperties()
	{
		return false;
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

		for( GotoCondition condition : conditions )
		{
			result.addAll( PropertyExpansionUtils.extractPropertyExpansions( this, condition, "expression" ) );
		}

		return result.toArray( new PropertyExpansion[result.size()] );
	}

	public XPathReference[] getXPathReferences()
	{
		List<XPathReference> result = new ArrayList<XPathReference>();

		for( GotoCondition condition : conditions )
		{
			if( StringUtils.hasContent( condition.getExpression() ) )
				result.add( new XPathReferenceImpl( "Condition for " + condition.getName() + " GotoCondition in "
						+ getName(), condition.getSourceProperty(), condition, "expression" ) );
		}

		return result.toArray( new XPathReference[result.size()] );
	}
}
