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
package com.eviware.soapui.security.scan;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.support.types.StringToStringMap;

public class PropertyMutation
{

	public static final String REQUEST_MUTATIONS_STACK = "RequestMutationsStack";

	private String propertyName;
	private String propertyValue;
	private StringToStringMap mutatedParameters;
	private TestStep testStep;

	public TestStep getTestStep()
	{
		return testStep;
	}

	public void setTestStep( TestStep testStep )
	{
		this.testStep = testStep;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public String getPropertyValue()
	{
		return propertyValue;
	}

	public StringToStringMap getMutatedParameters()
	{
		return mutatedParameters;
	}

	public void setPropertyName( String propertyName )
	{
		this.propertyName = propertyName;
	}

	public void setPropertyValue( String propertyValue )
	{
		this.propertyValue = propertyValue;
	}

	public void setMutatedParameters( StringToStringMap mutatedParameters )
	{
		if( this.mutatedParameters == null )
		{
			this.mutatedParameters = new StringToStringMap();
		}
		this.mutatedParameters.putAll( mutatedParameters );
	}

	public void updateRequestProperty( TestStep testStep )
	{
		testStep.getProperty( this.getPropertyName() ).setValue( this.getPropertyValue() );
	}

	@SuppressWarnings( "unchecked" )
	public void addMutation( SecurityTestRunContext context )
	{
		Stack<PropertyMutation> stack = ( Stack<PropertyMutation> )context.get( REQUEST_MUTATIONS_STACK );
		stack.push( this );
	}

	@SuppressWarnings( "unchecked" )
	public static PropertyMutation popMutation( SecurityTestRunContext context )
	{
		Stack<PropertyMutation> requestMutationsStack = ( Stack<PropertyMutation> )context.get( REQUEST_MUTATIONS_STACK );
		return requestMutationsStack.empty() ? null : requestMutationsStack.pop();
	}

	@SuppressWarnings( "unchecked" )
	public static List<PropertyMutation> popAllMutation( SecurityTestRunContext context )
	{
		Stack<PropertyMutation> requestMutationsStack = ( Stack<PropertyMutation> )context.get( REQUEST_MUTATIONS_STACK );
		PropertyMutation[] array = requestMutationsStack.toArray( new PropertyMutation[requestMutationsStack.size()] );
		requestMutationsStack.clear();
		return Arrays.asList( array );
	}

}
