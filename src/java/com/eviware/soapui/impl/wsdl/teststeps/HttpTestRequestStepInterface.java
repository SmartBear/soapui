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

package com.eviware.soapui.impl.wsdl.teststeps;

import java.beans.PropertyChangeListener;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.resolver.ResolveContext;

public interface HttpTestRequestStepInterface extends PropertyChangeListener, PropertyExpansionContainer, Assertable,
		HttpRequestTestStep, ModelItem
{
	public WsdlTestStep clone( WsdlTestCase targetTestCase, String name );

	public void release();

	public void resetConfigOnMove( TestStepConfig config );

	public HttpTestRequestInterface<?> getTestRequest();

	public void setName( String name );

	public boolean dependsOn( AbstractWsdlModelItem<?> modelItem );

	public void beforeSave();

	public void setDescription( String description );

	public String getDefaultSourcePropertyName();

	public String getDefaultTargetPropertyName();

	public void resolve( ResolveContext<?> context );

	public WsdlTestCase getTestCase();
}