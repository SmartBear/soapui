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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.DefaultTestStepProperty;
import com.eviware.soapui.model.testsuite.TestRunContext;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

/**
 * TestStep that delays execution for a number of milliseconds
 * 
 * @author ole.matzura
 */

public class WsdlDelayTestStep extends WsdlTestStepWithProperties
{
	private static final String DEFAULT_DELAY = "1000";
	private static final int DELAY_CHUNK = 100;
	private int delay = 0;
	private String delayString = WsdlDelayTestStep.DEFAULT_DELAY;
	private int timeWaited = 0;
	private boolean canceled;
	private boolean running;

	public WsdlDelayTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, false, forLoadTest );

		if( !forLoadTest )
		{
			setIcon( UISupport.createImageIcon( "/wait.gif" ) );
		}

		if( config.getConfig() == null )
		{
			if( !forLoadTest )
				saveDelay( config );
		}
		else
		{
			readConfig( config );
		}

		addProperty( new DefaultTestStepProperty( "delay", true, new DefaultTestStepProperty.PropertyHandlerAdapter()
		{

			public String getValue( DefaultTestStepProperty property )
			{
				return getDelayString();
			}

			@Override
			public void setValue( DefaultTestStepProperty property, String value )
			{
				setDelayString( value );
			}
		}, this ) );
	}

	private void readConfig( TestStepConfig config )
	{
		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( config.getConfig() );
		delayString = reader.readString( "delay", DEFAULT_DELAY );
	}

	public String getLabel()
	{
		String str = running ? super.getName() + " [" + ( delay - timeWaited ) + "ms]" : super.getName() + " ["
				+ delayString + "]";

		if( isDisabled() )
			str += " (disabled)";

		return str;
	}

	public String getDefaultSourcePropertyName()
	{
		return "delay";
	}

	public String getDefaultTargetPropertyName()
	{
		return "delay";
	}

	private void saveDelay( TestStepConfig config )
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( "delay", delayString );
		config.setConfig( builder.finish() );
	}

	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );
		readConfig( config );
	}

	public void setDelayString( String delayString )
	{
		if( this.delayString.equals( delayString ) )
			return;

		String oldLabel = getLabel();

		this.delayString = delayString;
		saveDelay( getConfig() );
		notifyPropertyChanged( WsdlTestStep.LABEL_PROPERTY, oldLabel, getLabel() );
	}

	public String getDelayString()
	{
		return delayString;
	}

	public int getDelay()
	{
		return delay;
	}

	public void setDelay( int delay )
	{
		if( this.delay == delay )
			return;

		String oldLabel = getLabel();

		this.delay = delay;
		saveDelay( getConfig() );
		notifyPropertyChanged( WsdlTestStep.LABEL_PROPERTY, oldLabel, getLabel() );
	}

	public TestStepResult run( TestRunner testRunner, TestRunContext context )
	{
		WsdlTestStepResult result = new WsdlTestStepResult( this );
		result.startTimer();
		String oldLabel = getLabel();

		try
		{
			canceled = false;
			running = true;

			try
			{
				delay = Integer.parseInt( PropertyExpansionUtils.expandProperties( context, delayString ) );
			}
			catch( NumberFormatException e )
			{
				delay = Integer.parseInt( DEFAULT_DELAY );
			}

			// sleep in chunks for canceling
			for( timeWaited = 0; !canceled && timeWaited < delay; timeWaited += DELAY_CHUNK )
			{
				if( timeWaited % 1000 == 0 && context.getProperty( TestRunContext.LOAD_TEST_RUNNER ) == null )
				{
					String newLabel = getLabel();
					notifyPropertyChanged( WsdlTestStep.LABEL_PROPERTY, oldLabel, newLabel );
					oldLabel = newLabel;
				}

				if( timeWaited <= delay - DELAY_CHUNK )
					Thread.sleep( DELAY_CHUNK );
				else
					Thread.sleep( delay % DELAY_CHUNK );
			}
		}
		catch( InterruptedException e )
		{
			SoapUI.logError( e );
		}

		result.stopTimer();
		result.setStatus( canceled ? TestStepStatus.CANCELED : TestStepStatus.OK );

		timeWaited = 0;
		running = false;

		if( context.getProperty( TestRunContext.LOAD_TEST_RUNNER ) == null )
			notifyPropertyChanged( WsdlTestStep.LABEL_PROPERTY, oldLabel, getLabel() );

		return result;
	}

	public boolean cancel()
	{
		canceled = true;
		return true;
	}
}
