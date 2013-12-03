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
package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.utils.ModelItemFactory;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * @author Joel
 */
public class RestParamsTableTest
{
	private JTable paramTable;
	private RestParamsPropertyHolder params;

	@Before
	public void setUp() throws Exception
	{
		params = ModelItemFactory.makeRestRequest().getParams();
		paramTable = new RestParamsTable( params, false, new RestParamsTableModel( params, RestParamsTableModel.Mode.FULL ), NewRestResourceActionBase.ParamLocation.RESOURCE, false, false ).paramsTable;
	}

	@Test
	public void disallowsTemplateParameterForMethodLevel() throws Exception
	{
		RestParamProperty prop = params.addProperty( "prop" );
		prop.setParamLocation( NewRestResourceActionBase.ParamLocation.METHOD );
		List<RestParamsPropertyHolder.ParameterStyle> availableStyles = getParameterStyles();
		assertThat( availableStyles, not( hasItem( RestParamsPropertyHolder.ParameterStyle.TEMPLATE ) ) );
	}

	@Test
	public void allowsTemplateParameterForResourceLevel() throws Exception
	{
		RestParamProperty prop = params.addProperty( "prop" );
		prop.setParamLocation( NewRestResourceActionBase.ParamLocation.RESOURCE );
		List<RestParamsPropertyHolder.ParameterStyle> availableStyles = getParameterStyles();
		assertThat( availableStyles, hasItem( RestParamsPropertyHolder.ParameterStyle.TEMPLATE ) );
	}

	@Test
	public void disallowsMethodLocationForTemplateParameter() throws Exception
	{
		RestParamProperty prop = params.addProperty( "prop" );
		prop.setParamLocation( NewRestResourceActionBase.ParamLocation.RESOURCE );
		prop.setStyle( RestParamsPropertyHolder.ParameterStyle.TEMPLATE );
		List<NewRestResourceActionBase.ParamLocation> availableLocations = getParameterLocations();
		assertThat( availableLocations, not( hasItem( NewRestResourceActionBase.ParamLocation.METHOD ) ) );
	}

	@Test
	public void disallowsMethodLocationForTemplateParameterOnMethodLevel() throws Exception
	{
		RestParamProperty prop = params.addProperty( "prop" );
		prop.setParamLocation( NewRestResourceActionBase.ParamLocation.METHOD );
		prop.setStyle( RestParamsPropertyHolder.ParameterStyle.TEMPLATE );
		List<NewRestResourceActionBase.ParamLocation> availableLocations = getParameterLocations();
		assertThat( availableLocations, not( hasItem( NewRestResourceActionBase.ParamLocation.METHOD ) ) );
	}

	@Test
	public void allowsMethodLocationForQueryParameter() throws Exception
	{
		RestParamProperty prop = params.addProperty( "prop" );
		prop.setParamLocation( NewRestResourceActionBase.ParamLocation.RESOURCE );
		prop.setStyle( RestParamsPropertyHolder.ParameterStyle.QUERY );
		List<NewRestResourceActionBase.ParamLocation> availableLocations = getParameterLocations();
		assertThat( availableLocations, hasItem( NewRestResourceActionBase.ParamLocation.METHOD ) );
	}

	private List<RestParamsPropertyHolder.ParameterStyle> getParameterStyles()
	{
		paramTable.editCellAt( 0, RestParamsTableModel.STYLE_COLUMN_INDEX );
		DefaultCellEditor cellEditor = ( DefaultCellEditor )paramTable.getCellEditor( 0, RestParamsTableModel.STYLE_COLUMN_INDEX );
		JComboBox<RestParamsPropertyHolder.ParameterStyle> comboBox = ( JComboBox )cellEditor.getComponent();
		return getSelectableValues( comboBox );
	}

	private List<NewRestResourceActionBase.ParamLocation> getParameterLocations()
	{
		paramTable.editCellAt( 0, RestParamsTableModel.LOCATION_COLUMN_INDEX );
		DefaultCellEditor cellEditor = ( DefaultCellEditor )paramTable.getCellEditor( 0, RestParamsTableModel.LOCATION_COLUMN_INDEX );
		JComboBox<NewRestResourceActionBase.ParamLocation> comboBox = ( JComboBox )cellEditor.getComponent();
		return getSelectableValues( comboBox );
	}

	private <T> List<T> getSelectableValues( JComboBox<T> comboBox )
	{
		List<T> availableStyles = new ArrayList<T>();
		for( int i = 0; i < comboBox.getItemCount(); i++ )
		{
			availableStyles.add( comboBox.getItemAt( i ) );
		}
		return availableStyles;
	}
}
