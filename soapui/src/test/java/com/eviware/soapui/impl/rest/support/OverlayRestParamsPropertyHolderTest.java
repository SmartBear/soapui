/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 *
 */
package com.eviware.soapui.impl.rest.support;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests OverlayRestParamsPropertyHolder.
 *
 * @author : Shadid Chowdhury
 */
public class OverlayRestParamsPropertyHolderTest
{
	OverlayRestParamsPropertyHolder propertyHolder;

	@Before
	public void setUP()
	{
		RestRequestParamsPropertyHolder.InternalRestParamProperty mockedParamProperty =
				mock( RestRequestParamsPropertyHolder.InternalRestParamProperty.class );

		RestParamsPropertyHolder overlay = mock( RestParamsPropertyHolder.class );
		when( overlay.containsKey( "paramOverLay" ) ).thenReturn( true ).thenReturn( false );
		when( overlay.removeProperty( "paramOverLay" ) ).thenReturn( mockedParamProperty ).thenReturn( null );

		RestParamsPropertyHolder parent = mock( RestParamsPropertyHolder.class );
		when( parent.containsKey( "paramOverLay" ) ).thenReturn( true ).thenReturn( false );
		when( parent.removeProperty( "paramParent" ) ).thenReturn( mockedParamProperty ).thenReturn( null );

		propertyHolder = new OverlayRestParamsPropertyHolder( parent, overlay );
	}

	@Test
	public void testRemoveProperty() throws Exception
	{
		assertNotNull( propertyHolder.removeProperty( "paramOverLay" ) );
		assertNull( propertyHolder.removeProperty( "paramOverLay" ) );
		assertNotNull( propertyHolder.removeProperty( "paramParent" ) );
		assertNull( propertyHolder.removeProperty( "paramParent" ) );
	}

	@After
	public void tearDown()
	{
		propertyHolder = null;
	}

}
