package com.eviware.soapui.impl.wsdl.support.assertions;

import java.util.List;

import com.eviware.soapui.config.TestAssertionConfig;

public interface AssertableConfig
{
	List<TestAssertionConfig> getAssertionList();

	void removeAssertion(int ix);

	TestAssertionConfig addNewAssertion();
}
