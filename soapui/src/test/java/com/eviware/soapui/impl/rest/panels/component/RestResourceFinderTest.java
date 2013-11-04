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
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Anders Jaensson
 */
public class RestResourceFinderTest
{

	@Test
	public void findLonelyResource() throws SoapUIException
	{

		RestResource resource = ModelItemFactory.makeRestResource();
		resource.setPath( "/resource" );

		RestResourceFinder finder = new RestResourceFinder( resource );

		assertThat( finder.findResourceAt( 1 ), is( resource ) );
	}

	@Test
	public void findParentInTwoLevelResource() throws SoapUIException
	{

		RestResource parent = ModelItemFactory.makeRestResource();
		RestResource child = parent.addNewChildResource( "child", "/child" );
		parent.setPath( "/parent" );

		RestResourceFinder finder = new RestResourceFinder( child );

		assertThat( finder.findResourceAt( 1 ), is( parent ) );
	}

	@Test
	public void findChildInTwoLevelResource() throws SoapUIException
	{

		RestResource parent = ModelItemFactory.makeRestResource();
		parent.setPath( "/parent" );
		RestResource child = parent.addNewChildResource( "child", "/child" );

		RestResourceFinder finder = new RestResourceFinder( child );

		assertThat( finder.findResourceAt( 10 ), is( child ) );
	}

	@Test
	public void findChildInTwoLevelResource2() throws SoapUIException
	{
		/*  / o n e / t w o / t h r e e / f o u r
		 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9
		 */

		RestResource one = ModelItemFactory.makeRestResource();
		one.setPath( "/one" );
		RestResource two = one.addNewChildResource( "two", "/two" );
		RestResource three = two.addNewChildResource( "three", "/three" );
		RestResource four = three.addNewChildResource( "four", "/four" );

		RestResourceFinder finder = new RestResourceFinder( four );

		assertThat( finder.findResourceAt( 0 ), is( one ) );
		assertThat( finder.findResourceAt( 1 ), is( one ) );
		assertThat( finder.findResourceAt( 2 ), is( one ) );
		assertThat( finder.findResourceAt( 3 ), is( one ) );
		assertThat( finder.findResourceAt( 4 ), is( one ) );
		assertThat( finder.findResourceAt( 5 ), is( two ) );
		assertThat( finder.findResourceAt( 6 ), is( two ) );
		assertThat( finder.findResourceAt( 7 ), is( two ) );
		assertThat( finder.findResourceAt( 8 ), is( two ) );
		assertThat( finder.findResourceAt( 9 ), is( three ) );
		assertThat( finder.findResourceAt( 10 ), is( three ) );
		assertThat( finder.findResourceAt( 11 ), is( three ) );
		assertThat( finder.findResourceAt( 12 ), is( three ) );
		assertThat( finder.findResourceAt( 13 ), is( three ) );
		assertThat( finder.findResourceAt( 14 ), is( three ) );
		assertThat( finder.findResourceAt( 15 ), is( four ) );
		assertThat( finder.findResourceAt( 16 ), is( four ) );
		assertThat( finder.findResourceAt( 17 ), is( four ) );
		assertThat( finder.findResourceAt( 18 ), is( four ) );
		assertThat( finder.findResourceAt( 19 ), is( four ) );
		assertThat( finder.findResourceAt( 20 ), is( four ) );
	}

	@Test
	public void returnsNullWhenBasePathIsClicked() throws Exception
	{
		RestResource parent = ModelItemFactory.makeRestResource();
		String basePath = "/base";
		parent.getInterface().setBasePath( basePath );
		RestResourceFinder finder = new RestResourceFinder( parent );

		assertThat(finder.findResourceAt( basePath.length()), is(nullValue()) );

	}
}
