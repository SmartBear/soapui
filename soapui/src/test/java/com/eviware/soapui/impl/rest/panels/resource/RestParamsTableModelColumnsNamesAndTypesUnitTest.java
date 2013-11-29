package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ModelItemFactory;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertThat;

/**
 * @author Prakash
 */
@RunWith( Parameterized.class )
public class RestParamsTableModelColumnsNamesAndTypesUnitTest
{
	private RestParamsTableModel restParamsTableModel;
	private int columnIndex;
	private String columnName;
	private Class columnType;

	public RestParamsTableModelColumnsNamesAndTypesUnitTest( int columnIndex, String columnName, Class columnType )
	{
		this.columnIndex = columnIndex;
		this.columnName = columnName;
		this.columnType = columnType;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> generateData()
	{
		return Arrays.asList( new Object[][] {
				{
						-1, null, null
				}, {
				0, RestParamsTableModel.COLUMN_NAMES[0], RestParamsTableModel.COLUMN_TYPES[0]
		},
				{
						1, RestParamsTableModel.COLUMN_NAMES[1], RestParamsTableModel.COLUMN_TYPES[1]
				},
				{
						2, RestParamsTableModel.COLUMN_NAMES[2], RestParamsTableModel.COLUMN_TYPES[2]
				},
				{
						3, RestParamsTableModel.COLUMN_NAMES[3], RestParamsTableModel.COLUMN_TYPES[3]
				},
				{
						4, null, null
				}
		} );
	}

	@Before
	public void setUp() throws SoapUIException
	{
		RestRequest restRequest = ModelItemFactory.makeRestRequest();
		RestParamsPropertyHolder params = restRequest.getParams();
		params.addProperty( "param" );
		restParamsTableModel = new RestParamsTableModel( params );
	}

	@Test
	public void verifyColumnCountNamesColumnTypesAndThatAllColumnsAreEditable()
	{
		assertThat( restParamsTableModel.getColumnCount(), Is.is( 4 ) );
		assertThat( restParamsTableModel.getColumnName( this.columnIndex ), IsEqual.equalTo( this.columnName ) );
		assertThat( restParamsTableModel.getColumnClass( this.columnIndex ), IsEqual.equalTo( this.columnType ) );

		assertThat( restParamsTableModel.isCellEditable( 0, this.columnIndex ), Is.is (true) );
	}

}
