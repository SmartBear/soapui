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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.JdbcRequestTestStepConfig;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.TestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry.AssertableType;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.AssertionsListener;
import com.eviware.soapui.model.testsuite.TestAssertion;

public class JdbcRequest extends AbstractModelItem implements TestRequest
{
	private final JdbcRequestTestStep testStep;
	private Set<SubmitListener> submitListeners = new HashSet<SubmitListener>();
	private JdbcResponse response;
	final static Logger logger = Logger.getLogger( JdbcRequest.class );

	public JdbcRequest( JdbcRequestTestStep testStep )
	{
		this.testStep = testStep;
	}

	public void addSubmitListener( SubmitListener listener )
	{
		submitListeners.add( listener );
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return ModelSupport.dependsOn( testStep, modelItem );
	}

	public Attachment[] getAttachments()
	{
		return null;
	}

	public String getEncoding()
	{
		return null;
	}

	public String getEndpoint()
	{
		return null;
	}

	public Operation getOperation()
	{
		return null;
	}

	public String getRequestContent()
	{
		return ( ( JdbcRequestTestStepConfig )testStep.getConfig() ).getQuery();
	}

	public MessagePart[] getRequestParts()
	{
		return null;
	}

	public MessagePart[] getResponseParts()
	{
		return null;
	}

	public String getTimeout()
	{
		return testStep.getQueryTimeout();
	}

	public void removeSubmitListener( SubmitListener listener )
	{
		submitListeners.remove( listener );
	}

	public void setEncoding( String string )
	{
	}

	public void setEndpoint( String string )
	{
	}

	public JdbcSubmit submit( SubmitContext submitContext, boolean async ) throws SubmitException
	{
		return new JdbcSubmit( this, submitContext, async );
	}

	public List<? extends ModelItem> getChildren()
	{
		return null;
	}

	public String getDescription()
	{
		return testStep.getDescription();
	}

	public ImageIcon getIcon()
	{
		return testStep.getIcon();
	}

	public String getId()
	{
		return testStep.getId();
	}

	public String getName()
	{
		return testStep.getName();
	}

	public ModelItem getParent()
	{
		return testStep.getParent();
	}

	public Settings getSettings()
	{
		return testStep.getSettings();
	}

	public SubmitListener[] getSubmitListeners()
	{
		return submitListeners.toArray( new SubmitListener[submitListeners.size()] );
	}

	public JdbcRequestTestStep getTestStep()
	{
		return testStep;
	}

	public WsdlMessageAssertion importAssertion( WsdlMessageAssertion source, boolean overwrite, boolean createCopy )
	{
		return testStep.importAssertion( source, overwrite, createCopy );
	}

	public TestAssertion addAssertion( String selection )
	{
		return testStep.addAssertion( selection );
	}

	public void addAssertionsListener( AssertionsListener listener )
	{
		testStep.addAssertionsListener( listener );
	}

	public TestAssertion cloneAssertion( TestAssertion source, String name )
	{
		return testStep.cloneAssertion( source, name );
	}

	public String getAssertableContent()
	{
		return testStep.getAssertableContent();
	}

	public AssertableType getAssertableType()
	{
		return testStep.getAssertableType();
	}

	public TestAssertion getAssertionAt( int c )
	{
		return testStep.getAssertionAt( c );
	}

	public TestAssertion getAssertionByName( String name )
	{
		return testStep.getAssertionByName( name );
	}

	public int getAssertionCount()
	{
		return testStep.getAssertionCount();
	}

	public List<TestAssertion> getAssertionList()
	{
		return testStep.getAssertionList();
	}

	public AssertionStatus getAssertionStatus()
	{
		return testStep.getAssertionStatus();
	}

	public Map<String, TestAssertion> getAssertions()
	{
		return testStep.getAssertions();
	}

	public String getDefaultAssertableContent()
	{
		return testStep.getDefaultAssertableContent();
	}

	public Interface getInterface()
	{
		return testStep.getInterface();
	}

	public ModelItem getModelItem()
	{
		return testStep.getModelItem();
	}

	public TestAssertion moveAssertion( int ix, int offset )
	{
		return testStep.moveAssertion( ix, offset );
	}

	public void removeAssertion( TestAssertion assertion )
	{
		testStep.removeAssertion( assertion );
	}

	public void removeAssertionsListener( AssertionsListener listener )
	{
		testStep.removeAssertionsListener( listener );
	}

	public void setResponse( JdbcResponse response )
	{
		this.response = response;
	}

	public JdbcResponse getResponse()
	{
		return response;
	}
}
