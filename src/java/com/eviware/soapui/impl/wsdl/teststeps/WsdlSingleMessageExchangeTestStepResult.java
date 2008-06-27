/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.io.PrintWriter;

import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * TestStep Result for a WsdlMessageExchange
 * 
 * @author ole.matzura
 */

public class WsdlSingleMessageExchangeTestStepResult extends WsdlTestStepResult
{
	private WsdlMessageExchange messageExchange;
//	private StringToStringMap properties;
	
	public WsdlSingleMessageExchangeTestStepResult(WsdlTestStep step )
	{
		super( step );
	}
	
	public void setMessageExchange( WsdlMessageExchange messageExchange )
	{
		this.messageExchange = messageExchange;
		addAction( new ShowMessageExchangeAction( messageExchange, "StepResult" ), true );
	}

//	public String getRequestContent()
//	{
//		if( isDiscarded() )
//			return "<discarded>";
//		
//		return messageExchange == null ? null : messageExchange.getRequestContent();
//	}
//
//	public void addProperty( String name, String value )
//	{
//		if( isDiscarded() )
//			return;
//		
//		if( properties == null )
//			properties = new StringToStringMap();
//		
//		properties.put( name, value );
//	}
	
	public void discard()
	{
		super.discard();
		
		messageExchange = null;
//		properties = null;
	}

	public void writeTo(PrintWriter writer)
	{
		super.writeTo( writer );
		
		if( isDiscarded() )
			return;
		
//		writer.println( "---------------- Properties ------------------------" );
//		if( properties == null  )
//		{
//			writer.println( "Missing Properties" );
//		}
//		else
//		{
//			for( String name : properties.keySet() )
//				writer.println( name + ": " + properties.get( name ) );
//		}

		writer.println( "---------------- Message Exchange ------------------" );
		if( messageExchange == null )
		{
			writer.println( "Missing MessageExchange" );
		}
		else
		{
			writer.println( "--- Request" );
			if( messageExchange.getRequestHeaders() != null )
				writer.println( "Request Headers: " + messageExchange.getRequestHeaders().toString() );
			
			writer.println( XmlUtils.prettyPrintXml( messageExchange.getRequestContent() ) );

			writer.println( "--- Response" );
			if( messageExchange.getResponseHeaders() != null )
				writer.println( "Response Headers: " + messageExchange.getResponseHeaders().toString() );
			
			writer.println( XmlUtils.prettyPrintXml( messageExchange.getResponseContent() ) );
		}
	}

//	public StringToStringMap getProperties()
//	{
//		return properties;
//	}
}