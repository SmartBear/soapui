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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import com.eviware.soapui.config.MockOperationDispatchStyleConfig;
import com.eviware.soapui.model.mock.MockOperation;

import java.util.HashMap;
import java.util.Map;

public class MockOperationDispatchRegistry
{
	private static Map<String, MockOperationDispatchFactory> factories = new HashMap<String, MockOperationDispatchFactory>();

	static
	{
		putFactory( MockOperationDispatchStyleConfig.SEQUENCE.toString(), new SequenceMockOperationDispatcher.Factory() );
		putFactory( MockOperationDispatchStyleConfig.RANDOM.toString(), new RandomMockOperationDispatcher.Factory() );
		putFactory( MockOperationDispatchStyleConfig.SCRIPT.toString(), new ScriptMockOperationDispatcher.Factory() );
		putFactory( MockOperationDispatchStyleConfig.XPATH.toString(), new XPathMockOperationDispatcher.Factory() );
		putFactory( MockOperationDispatchStyleConfig.QUERY_MATCH.toString(),
				new QueryMatchMockOperationDispatcher.Factory() );
	}

	public static void putFactory( String type, MockOperationDispatchFactory factory )
	{
		factories.put( type, factory );
	}

	public static String[] getDispatchTypes()
	{
		return factories.keySet().toArray( new String[factories.size()] );
	}

	public static MockOperationDispatcher buildDispatcher( String type, MockOperation mockOperation )
	{
		return factories.get( type ).build( mockOperation );
	}
}
