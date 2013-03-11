/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.model.support;

import java.util.Collections;
import java.util.List;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.google.common.collect.Lists;

public class MessageExchangeUtil
{
	public static MessageExchange findMessageExchangeByTestStepId( List<TestStepResult> results, String testStepId )
	{
		List<TestStepResult> tmpList = Lists.newArrayList(results);
		Collections.reverse(tmpList);

		for( TestStepResult tsr : tmpList )
		{
			String id = tsr.getTestStep().getId();
			if( id.equals( testStepId ) && tsr instanceof MessageExchange )
				return ( MessageExchange )tsr;
		}
		return null;

	}
}
