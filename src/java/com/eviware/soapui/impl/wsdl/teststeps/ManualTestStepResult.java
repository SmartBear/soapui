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

import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

public class ManualTestStepResult extends WsdlTestStepResult implements  MessageExchange
{
	private boolean addedAction;

	public ManualTestStepResult( ManualTestStep testStep )
	{
		super( testStep );
	}

	@Override
	public ActionList getActions()
	{
		if( !addedAction )
		{
			addAction( new ShowMessageExchangeAction( this, "TestStep" ), true );
			addedAction = true;
		}

		return super.getActions();
	}

	public ModelItem getModelItem()
	{
		return getTestStep();
	}

	public Operation getOperation()
	{
		return null;
	}

	public StringToStringMap getProperties()
	{
		return new StringToStringMap();
	}

	public String getProperty( String name )
	{
		return null;
	}

	public byte[] getRawRequestData()
	{
		return null;
	}

	public byte[] getRawResponseData()
	{
		return null;
	}

	public Attachment[] getRequestAttachments()
	{
		return new Attachment[0];
	}

	public Attachment[] getRequestAttachmentsForPart( String partName )
	{
		return new Attachment[0];
	}

	public String getRequestContent()
	{
		return null;
	}

	public String getRequestContentAsXml()
	{
		return null;
	}

	public StringToStringsMap getRequestHeaders()
	{
		return new StringToStringsMap();
	}

	public Attachment[] getResponseAttachments()
	{
		return new Attachment[0];
	}

	public Attachment[] getResponseAttachmentsForPart( String partName )
	{
		return new Attachment[0];
	}

	public String getResponseContent()
	{
		return  null;
	}

	public String getResponseContentAsXml()
	{
		return  null;
	}

	public StringToStringsMap getResponseHeaders()
	{
		return new StringToStringsMap();
	}

	public long getTimestamp()
	{
		return  -1;
	}

	public boolean hasRawData()
	{
		return true;
	}

	public boolean hasRequest( boolean ignoreEmpty )
	{
		return hasResponse();
	}

	public boolean hasResponse()
	{
		return false;
	}

	public String getEndpoint()
	{
		return this.getEndpoint();
	}


}
