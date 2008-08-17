package com.eviware.soapui.impl.wsdl.teststeps;

import java.util.List;
import java.util.Map;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.WsdlAssertionRegistry.AssertableType;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;

public class RestTestRequest extends RestRequest implements Assertable
{

	public RestTestRequest(RestResource resource, RestRequestConfig requestConfig)
	{
		super(resource, requestConfig);
		// TODO Auto-generated constructor stub
	}

	public TestAssertion addAssertion(String selection)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void addAssertionsListener(AssertionsListener listener)
	{
		// TODO Auto-generated method stub

	}

	public TestAssertion cloneAssertion(TestAssertion source, String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getAssertableContent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public AssertableType getAssertableType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public TestAssertion getAssertionAt(int c)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public TestAssertion getAssertionByName(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getAssertionCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public List<TestAssertion> getAssertionList()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public AssertionStatus getAssertionStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, TestAssertion> getAssertions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getDefaultAssertableContent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Interface getInterface()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void removeAssertion(TestAssertion assertion)
	{
		// TODO Auto-generated method stub

	}

	public void removeAssertionsListener(AssertionsListener listener)
	{
		// TODO Auto-generated method stub

	}

}
