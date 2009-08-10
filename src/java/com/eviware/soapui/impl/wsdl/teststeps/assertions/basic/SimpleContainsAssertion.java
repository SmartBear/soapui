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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.basic;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Assertion that checks for a specified text token in the associated
 * WsdlTestRequests response XML message
 * 
 * @author Ole.Matzura
 */

public class SimpleContainsAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion
{
	private String token;
	private XFormDialog dialog;
	private boolean ignoreCase;
	private boolean useRegEx;
	public static final String ID = "Simple Contains";
	private static final String CONTENT = "Content";
	private static final String IGNORE_CASE = "Ignore Case";
	private static final String USE_REGEX = "Regular Expression";
	public static final String LABEL = "Contains";

	public SimpleContainsAssertion( TestAssertionConfig assertionConfig, Assertable assertable )
	{
		super( assertionConfig, assertable, true, true, true, true );

		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
		token = reader.readString( "token", null );
		ignoreCase = reader.readBoolean( "ignoreCase", false );
		useRegEx = reader.readBoolean( "useRegEx", false );
	}

	public String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return assertContent( context, messageExchange.getResponseContent(), "Response" );
	}

	private String assertContent( SubmitContext context, String content, String type ) throws AssertionException
	{
		if( token == null )
			token = "";
		String replToken = PropertyExpander.expandProperties( context, token );

		if( replToken.length() > 0 )
		{
			int ix = -1;

			if( useRegEx )
			{
				if( content.matches( replToken ) )
					ix = 0;
			}
			else
			{
				ix = ignoreCase ? content.toUpperCase().indexOf( replToken.toUpperCase() ) : content.indexOf( replToken );
			}

			if( ix == -1 )
				throw new AssertionException( new AssertionError( "Missing token [" + replToken + "] in " + type ) );
		}

		return "Response contains token [" + replToken + "]";
	}

	public boolean configure()
	{
		if( dialog == null )
			buildDialog();

		StringToStringMap values = new StringToStringMap();
		values.put( CONTENT, token );
		values.put( IGNORE_CASE, ignoreCase );
		values.put( USE_REGEX, useRegEx );

		values = dialog.show( values );
		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			token = values.get( CONTENT );
			ignoreCase = values.getBoolean( IGNORE_CASE );
			useRegEx = values.getBoolean( USE_REGEX );
		}

		setConfiguration( createConfiguration() );
		return true;
	}

	public boolean isUseRegEx()
	{
		return useRegEx;
	}

	public void setUseRegEx( boolean useRegEx )
	{
		this.useRegEx = useRegEx;
		setConfiguration( createConfiguration() );
	}

	public boolean isIgnoreCase()
	{
		return ignoreCase;
	}

	public void setIgnoreCase( boolean ignoreCase )
	{
		this.ignoreCase = ignoreCase;
		setConfiguration( createConfiguration() );
	}

	public String getToken()
	{
		return token;
	}

	public void setToken( String token )
	{
		this.token = token;
		setConfiguration( createConfiguration() );
	}

	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( "token", token );
		builder.add( "ignoreCase", ignoreCase );
		builder.add( "useRegEx", useRegEx );
		return builder.finish();
	}

	private void buildDialog()
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Contains Assertion" );
		XForm mainForm = builder.createForm( "Basic" );

		mainForm.addTextField( CONTENT, "Content to check for", XForm.FieldType.TEXTAREA ).setWidth( 40 );
		mainForm.addCheckBox( IGNORE_CASE, "Ignore case in comparison" );
		mainForm.addCheckBox( USE_REGEX, "Use token as Regular Expression" );

		dialog = builder.buildDialog( builder.buildOkCancelHelpActions( HelpUrls.SIMPLE_CONTAINS_HELP_URL ),
				"Specify options", UISupport.OPTIONS_ICON );
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		return assertContent( context, messageExchange.getRequestContent(), "Request" );
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

		result.addAll( PropertyExpansionUtils.extractPropertyExpansions( getAssertable().getModelItem(), this, "token" ) );

		return result.toArray( new PropertyExpansion[result.size()] );
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		public Factory()
		{
			super( SimpleContainsAssertion.ID, SimpleContainsAssertion.LABEL, SimpleContainsAssertion.class );
		}
	}
}
