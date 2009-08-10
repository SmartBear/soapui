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

package com.eviware.soapui.impl.wsdl.support.wsrm;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

public class WsrmSequence
{
	private String identifier;
	private long lastMsgNumber;
	private String uuid;
	private SoapVersion soapVersion;
	private String wsrmNameSpace;
	private WsdlOperation operation;

	public WsrmSequence( String identifier, String uuid, SoapVersion soapVersion, String namespace,
			WsdlOperation operation )
	{
		this.identifier = identifier;
		this.lastMsgNumber = 0;
		this.soapVersion = soapVersion;
		this.uuid = uuid;
		this.setWsrmNameSpace( namespace );
		this.setOperation( operation );
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public long getLastMsgNumber()
	{
		return lastMsgNumber;
	}

	public long incrementLastMsgNumber()
	{
		lastMsgNumber++ ;
		return lastMsgNumber;
	}

	public void setUuid( String uuid )
	{
		this.uuid = uuid;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setSoapVersion( SoapVersion soapVersion )
	{
		this.soapVersion = soapVersion;
	}

	public SoapVersion getSoapVersion()
	{
		return soapVersion;
	}

	public void setWsrmNameSpace( String wsrmNameSpace )
	{
		this.wsrmNameSpace = wsrmNameSpace;
	}

	public String getWsrmNameSpace()
	{
		return wsrmNameSpace;
	}

	public void setOperation( WsdlOperation operation )
	{
		this.operation = operation;
	}

	public WsdlOperation getOperation()
	{
		return operation;
	}

}
