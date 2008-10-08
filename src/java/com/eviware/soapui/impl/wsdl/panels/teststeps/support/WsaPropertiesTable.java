package com.eviware.soapui.impl.wsdl.panels.teststeps.support;


public class WsaPropertiesTable 
{
	public WsaPropertiesTable(boolean assertAction, boolean assertTo, boolean assertRelatesTo) {
		this.assertAction = assertAction;
		this.assertTo = assertTo;
		this.assertRelatesTo = assertRelatesTo;
	}
	private boolean assertAction;
	private boolean assertTo;
	private boolean assertRelatesTo;
	public boolean isAssertAction()
	{
		return assertAction;
	}
	public void setAssertAction(boolean assertAction)
	{
		this.assertAction = assertAction;
	}
	public boolean isAssertTo()
	{
		return assertTo;
	}
	public void setAssertTo(boolean assertTo)
	{
		this.assertTo = assertTo;
	}
	public boolean isAssertRelatesTo()
	{
		return assertRelatesTo;
	}
	public void setAssertRelatesTo(boolean assertRelatesTo)
	{
		this.assertRelatesTo = assertRelatesTo;
	}
	
}
