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

package com.eviware.soapui.impl.wsdl.support.assertions;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.TestAssertion;

public class AssertedXPathImpl implements AssertedXPath
{
	private final TestAssertion assertion;
	private final String path;
	private XmlObject assertedContent;

	public AssertedXPathImpl( TestAssertion assertion, String path, XmlObject assertedContent )
	{
		this.assertion = assertion;
		this.path = path;
		this.assertedContent = assertedContent;
	}

	public TestAssertion getAssertion()
	{
		return assertion;
	}

	public String getLabel()
	{
		return assertion.getName();
	}

	public String getPath()
	{
		return path;
	}

	public XmlObject getAssertedContent()
	{
		return assertedContent;
	}

	public void setAssertedContent( XmlObject assertedContent )
	{
		this.assertedContent = assertedContent;
	}

}