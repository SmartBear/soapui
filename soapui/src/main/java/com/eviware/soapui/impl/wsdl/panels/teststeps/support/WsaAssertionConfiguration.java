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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

public class WsaAssertionConfiguration
{
	public WsaAssertionConfiguration( boolean assertAction, boolean assertTo, boolean assertReplyTo,
			boolean assertMessageId, boolean assertRelatesTo, boolean assertReplyToRefParams,
			boolean assertFaultToRefParams )
	{
		this.assertAction = assertAction;
		this.assertTo = assertTo;
		this.assertReplyTo = assertReplyTo;
		this.assertMessageId = assertMessageId;
		this.assertRelatesTo = assertRelatesTo;
		this.assertReplyToRefParams = assertReplyToRefParams;
		this.assertFaultToRefParams = assertFaultToRefParams;
	}

	private boolean assertAction;
	private boolean assertTo;
	private boolean assertReplyTo;
	private boolean assertMessageId;
	private boolean assertRelatesTo;
	private boolean assertReplyToRefParams;
	private boolean assertFaultToRefParams;

	public boolean isAssertAction()
	{
		return assertAction;
	}

	public void setAssertAction( boolean assertAction )
	{
		this.assertAction = assertAction;
	}

	public boolean isAssertTo()
	{
		return assertTo;
	}

	public void setAssertTo( boolean assertTo )
	{
		this.assertTo = assertTo;
	}

	public boolean isAssertRelatesTo()
	{
		return assertRelatesTo;
	}

	public void setAssertRelatesTo( boolean assertRelatesTo )
	{
		this.assertRelatesTo = assertRelatesTo;
	}

	public boolean isReplyToRefParams()
	{
		return assertReplyToRefParams;
	}

	public void setReplyToRefParams( boolean replyToRefParams )
	{
		this.assertReplyToRefParams = replyToRefParams;
	}

	public boolean isAssertReplyToRefParams()
	{
		return assertReplyToRefParams;
	}

	public void setAssertReplyToRefParams( boolean assertReplyToRefParams )
	{
		this.assertReplyToRefParams = assertReplyToRefParams;
	}

	public boolean isAssertFaultToRefParams()
	{
		return assertFaultToRefParams;
	}

	public void setAssertFaultToRefParams( boolean assertFaultToRefParams )
	{
		this.assertFaultToRefParams = assertFaultToRefParams;
	}

	public boolean isAssertReplyTo()
	{
		return assertReplyTo;
	}

	public void setAssertReplyTo( boolean assertReplyTo )
	{
		this.assertReplyTo = assertReplyTo;
	}

	public boolean isAssertMessageId()
	{
		return assertMessageId;
	}

	public void setAssertMessageId( boolean assertMessageId )
	{
		this.assertMessageId = assertMessageId;
	}

}
