/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
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

import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.ManualTestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.panels.teststeps.amf.AMFSubmit;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;

/**
 * 
 * @author nebojsa.tasic
 */

public class ManualTestStep extends WsdlTestStep

{
	@SuppressWarnings( "unused" )
	private final static Logger log = Logger.getLogger( WsdlTestRequestStep.class );
	protected ManualTestStepConfig manualTestStepConfig;
	public final static String MANUAL_STEP = ManualTestStep.class.getName() + "@manualstep";
	public static final String STATUS_PROPERTY = WsdlTestRequest.class.getName() + "@status";

	public ManualTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		super( testCase, config, true, forLoadTest );

		if( getConfig().getConfig() != null )
		{
			manualTestStepConfig = ( ManualTestStepConfig )getConfig().getConfig().changeType( ManualTestStepConfig.type );
		}
		else
		{
			manualTestStepConfig = ( ManualTestStepConfig )getConfig().addNewConfig().changeType(
					ManualTestStepConfig.type );
		}
	}

	public ManualTestStepConfig getManualTestStepConfig()
	{
		return manualTestStepConfig;
	}

	@Override
	public WsdlTestStep clone( WsdlTestCase targetTestCase, String name )
	{
		beforeSave();

		TestStepConfig config = ( TestStepConfig )getConfig().copy();
		ManualTestStep result = ( ManualTestStep )targetTestCase.addTestStep( config );

		return result;
	}

	@Override
	public void release()
	{
		super.release();
	}

	public TestStepResult run( TestCaseRunner runner, TestCaseRunContext runContext )
	{
		ManualTestStepResult testStepResult = new ManualTestStepResult( this );
		testStepResult.startTimer();

		try
		{
			if( !initManualStep( runContext ) )
			{
				throw new SubmitException( "Manual step is not initialised properly !" );
			}

			// testStepResult.setTimeTaken( response.getTimeTaken() );
			// testStepResult.setSize( response.getContentLength() );
			//
			// switch( amfRequest.getAssertionStatus() )
			// {
			// case FAILED :
			// testStepResult.setStatus( TestStepStatus.FAILED );
			// break;
			// case VALID :
			// testStepResult.setStatus( TestStepStatus.OK );
			// break;
			// case UNKNOWN :
			// testStepResult.setStatus( TestStepStatus.UNKNOWN );
			// break;
			// }

			testStepResult.setStatus( TestStepStatus.CANCELED );
			testStepResult.addMessage( "Request was canceled" );

			testStepResult.stopTimer();
		}
		catch( SubmitException e )
		{
			testStepResult.setStatus( TestStepStatus.FAILED );
			testStepResult.addMessage( "SubmitException: " + e );
			testStepResult.stopTimer();
		}

		return testStepResult;
	}

	@Override
	public boolean cancel()
	{
		return true;
	}

	public String getDefaultSourcePropertyName()
	{
		return "Response";
	}

	public ImageIcon getIcon()
	{
		return null;
	}

	public Interface getInterface()
	{
		return null;
	}

	public String getDescription()
	{
		return manualTestStepConfig.getDescription();
	}

	public void setDescription( String description )
	{
		String old = getDescription();
		manualTestStepConfig.setDescription( description );
		notifyPropertyChanged( "description", old, description );
	}

	public String getExpectedResult()
	{
		return manualTestStepConfig.getExpectedResult();
	}

	public void setExpectedResult( String expectedResult )
	{
		String old = getExpectedResult();
		manualTestStepConfig.setExpectedResult( expectedResult );
		notifyPropertyChanged( "expectedResult", old, expectedResult );
	}

	public String getName()
	{
		return manualTestStepConfig.getName();
	}

	public void setName( String name )
	{
		String old = getName();
		manualTestStepConfig.setName( name );
		notifyPropertyChanged( "name", old, name );
	}

	public Boolean initManualStep( SubmitContext submitContext )
	{
		return null;
	}

	public void resetConfigOnMove( TestStepConfig config )
	{
		super.resetConfigOnMove( config );
		manualTestStepConfig = ( ManualTestStepConfig )config.getConfig().changeType( ManualTestStepConfig.type );
//		propertyHolderSupport.resetPropertiesConfig( manualTestStepConfig.getProperties() );
	}

	public TestStep getTestStep()
	{
		return this;
	}

	@Override
	public void addTestPropertyListener( TestPropertyListener listener )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, TestProperty> getProperties()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TestProperty getProperty( String name )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TestProperty getPropertyAt( int index )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPropertyCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<TestProperty> getPropertyList()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropertyValue( String name )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasProperty( String name )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue( String name, String value )
	{
		// TODO Auto-generated method stub

	}

}
