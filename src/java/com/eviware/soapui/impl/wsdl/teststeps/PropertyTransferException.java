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

import com.eviware.soapui.model.testsuite.TestProperty;

/**
 * Exception than can occur during property-transfers
 * 
 * @author ole.matzura
 */

public class PropertyTransferException extends Exception
{
	private String message;
	private final String sourceStepName;
	private final String targetStepName;
	private String sourcePropertyName;
	private String sourcePropertyValue;
	private String targetPropertyName;
	private String targetPropertyValue;

	public PropertyTransferException( String message, String sourceStepName, TestProperty source, String targetStepName,
			TestProperty target )
	{
		this.message = message;
		this.sourceStepName = sourceStepName;
		this.targetStepName = targetStepName;

		if( source != null )
		{
			sourcePropertyName = source.getName();
			sourcePropertyValue = source.getValue();
		}
		if( target != null )
		{
			targetPropertyName = target.getName();
			targetPropertyValue = target.getValue();
		}
	}

	public String getMessage()
	{
		return message;
	}

	public String getSourcePropertyName()
	{
		return sourcePropertyName;
	}

	public String getSourcePropertyValue()
	{
		return sourcePropertyValue;
	}

	public String getSourceStepName()
	{
		return sourceStepName;
	}

	public String getTargetPropertyName()
	{
		return targetPropertyName;
	}

	public String getTargetPropertyValue()
	{
		return targetPropertyValue;
	}

	public String getTargetStepName()
	{
		return targetStepName;
	}
}