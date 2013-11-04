/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.rest.panels.component;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Joel
 */
public class RestResourceEditorTest
{
	private RestResource lonelyResource;
	private RestResource parentResource;
	private RestResource childResource;
	private RestResource templateResource;
	private XDialogs oldDialogs;
	private StubbedDialogs dialogs;

	@Before
	public void setUp() throws Exception
	{
		oldDialogs = UISupport.getDialogs();
		dialogs = new StubbedDialogs();
		UISupport.setDialogs( dialogs );
		lonelyResource = ModelItemFactory.makeRestResource();
		lonelyResource.setPath( "/lonely" );

		templateResource = ModelItemFactory.makeRestResource();
		templateResource.setPath( "/hello{param1}{param2}" );

		parentResource = ModelItemFactory.makeRestResource();
		parentResource.setPath( "/parent{param1}" );
		RestParamProperty existingparam = parentResource.addProperty( "existingparam" );
		existingparam.setValue( "existingvalue" );
		existingparam.setDefaultValue( "existingvalue" );

		childResource = parentResource.addNewChildResource( "child", "the_child{param2}{existingparam}" );
	}

	@After
	public void tearDown() throws Exception
	{
		UISupport.setDialogs( oldDialogs );
	}

	@Test
	public void changingPathTextForLonelyResourceShouldUpdateTheResourcePath()
	{
		RestResourceEditor restResourceEditor = new RestResourceEditor( lonelyResource, new MutableBoolean() );
		restResourceEditor.setText( "hello" );
		assertThat( lonelyResource.getFullPath(), is( "/hello" ) );
	}

	@Test
	public void displaysBasePathOfServiceInField()
	{
		lonelyResource.getInterface().setBasePath( "/base" );
		lonelyResource.setPath("resource");
		RestResourceEditor restResourceEditor = new RestResourceEditor( lonelyResource, new MutableBoolean() );
		assertThat( restResourceEditor.getText(), is( "/base/resource" ) );
	}

	@Test
	public void displaysResourcePopupIfHasBasePath()
	{
		lonelyResource.getInterface().setBasePath( "/base" );
		lonelyResource.setPath("resource");
		RestResourceEditor restResourceEditor = new RestResourceEditor( lonelyResource, new MutableBoolean() );

		assertThat( restResourceEditor.mouseListener, is(notNullValue()) );
	}

	@Test
	public void addingPathWithTemplateParametersAddsParametersToResource()
	{
		RestResourceEditor restResourceEditor = new RestResourceEditor( templateResource, new MutableBoolean() );
		dialogs.mockPromptWithReturnValue( "value" );
		restResourceEditor.scanForTemplateParameters();
		assertThat( templateResource.getParams().get( "param2" ).getValue(), is( "value" ) );
	}

	@Test
	public void existingParameterOnParentNotAddedToChildWithPathContainingThatParameter()
	{
		RestResourceEditor restResourceEditor = new RestResourceEditor( childResource, new MutableBoolean() );
		dialogs.mockPromptWithReturnValue( "value" );
		restResourceEditor.scanForTemplateParameters();
		assertThat( parentResource.getParams().get( "param1" ), parameterWithValue( "value" ) );
		assertThat( parentResource.getParams().get( "param2" ), is( nullValue() ) );
		assertThat( parentResource.getParams().get( "existingparam" ), parameterWithValue( "existingvalue" ) );
		assertThat( childResource.getParams().get( "param1" ), is( nullValue() ) );
		assertThat( childResource.getParams().get( "param2" ), parameterWithValue( "value" ) );
		assertThat( childResource.getParams().get( "existingparam" ), is( nullValue() ) );
	}

	private Matcher<RestParamProperty> parameterWithValue( final String value )
	{
		return new TypeSafeMatcher<RestParamProperty>()
		{

			@Override
			public boolean matchesSafely( RestParamProperty restParamsPropertyHolder )
			{
				if( restParamsPropertyHolder == null || restParamsPropertyHolder.getValue() == null )
				{
					return false;
				}
				return restParamsPropertyHolder.getValue().equals( value );
			}

			@Override
			public void describeTo( Description description )
			{
				description.appendText( String.format("Parameter with value %s", value) );
			}
		};
	}


}
