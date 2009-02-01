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

package com.eviware.soapui.impl.wsdl.loadtest.data;

import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;

/**
 * Base class for a LoadTest sample
 * 
 * @author Ole.Matzura
 */

public class LoadTestStepSample
{
	private long size;
	private TestStepStatus status;
	private long timeTaken;
	private String[] messages;
	private long timeStamp;

	LoadTestStepSample( TestStepResult result )
	{
		size = result.getSize();
		status = result.getStatus();
		timeTaken = result.getTimeTaken();
		messages = result.getMessages();
		timeStamp = result.getTimeStamp();
	}

	public String[] getMessages()
	{
		return messages.clone();
	}

	public long getSize()
	{
		return size;
	}

	public TestStepStatus getStatus()
	{
		return status;
	}

	public long getTimeStamp()
	{
		return timeStamp;
	}

	public long getTimeTaken()
	{
		return timeTaken;
	}
}