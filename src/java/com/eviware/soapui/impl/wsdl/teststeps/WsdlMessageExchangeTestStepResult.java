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

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.MessageExchangeTestStepResult;

public class WsdlMessageExchangeTestStepResult extends WsdlTestStepResult implements MessageExchangeTestStepResult
{
	private List<MessageExchange> exchanges = new ArrayList<MessageExchange>();

	public WsdlMessageExchangeTestStepResult( WsdlTestStep testStep )
	{
		super( testStep );
	}

	public MessageExchange[] getMessageExchanges()
	{
		return exchanges == null ? new MessageExchange[0] : exchanges.toArray( new MessageExchange[exchanges.size()] );
	}

	public void addMessageExchange( MessageExchange messageExchange )
	{
		if( exchanges != null )
		exchanges.add( messageExchange );
	}

	public void addMessages( MessageExchange[] messageExchanges )
	{
		if( exchanges != null )
		for( MessageExchange messageExchange : messageExchanges )
			exchanges.add( messageExchange );
	}

	@Override
	public void discard()
	{
		super.discard();

		exchanges = null;
	}
}
