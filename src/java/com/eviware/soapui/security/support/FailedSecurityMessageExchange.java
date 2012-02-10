package com.eviware.soapui.security.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

public class FailedSecurityMessageExchange implements MessageExchange
{

	@Override
	public String getEndpoint()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getResponse()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getMessages()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModelItem getModelItem()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Operation getOperation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringToStringMap getProperties()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProperty( String name )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getRawRequestData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getRawResponseData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attachment[] getRequestAttachments()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attachment[] getRequestAttachmentsForPart( String partName )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestContent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestContentAsXml()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringToStringsMap getRequestHeaders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attachment[] getResponseAttachments()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attachment[] getResponseAttachmentsForPart( String partName )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResponseContent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResponseContentAsXml()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringToStringsMap getResponseHeaders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeTaken()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTimestamp()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasRawData()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasRequest( boolean ignoreEmpty )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasResponse()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDiscarded()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
