/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.ui;

import com.eviware.soapui.config.FuzzerScanConfig;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.impl.swing.JFormDialog;

public class FuzzerScanAdvancedConfigPanel
{
	private JFormDialog dialog;
	private FuzzerScanConfig fuzzerScanConfig;

	public FuzzerScanAdvancedConfigPanel( FuzzerScanConfig fuzzerScanConfig )
	{
		this.fuzzerScanConfig = fuzzerScanConfig;
		initDialog();
	}

	public JFormDialog getDialog()
	{
		return dialog;
	}

	private JFormDialog initDialog()
	{
		dialog = ( JFormDialog )ADialogBuilder.buildDialog( AdvancedSettings.class );
		minimalField( fuzzerScanConfig );
		maximalField( fuzzerScanConfig );
		numberOfRequestField( fuzzerScanConfig );
		return dialog;
	}

	private void minimalField( final FuzzerScanConfig fuzzerScanConfig )
	{
		XFormField minimal = dialog.getFormField( AdvancedSettings.MINIMAL );
		minimal.setValue( String.valueOf( fuzzerScanConfig.getMinimal() ) );

		minimal.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				try
				{
					if( "".equals( newValue ) )
						return;
					Integer.valueOf( newValue );
					fuzzerScanConfig.setMinimal( Integer.valueOf( newValue ) );
				}
				catch( Exception e )
				{
					UISupport.showErrorMessage( "Value must be integer number" );
				}
			}
		} );
	}

	private void maximalField( final FuzzerScanConfig fuzzerScanConfig )
	{
		XFormField maximal = dialog.getFormField( AdvancedSettings.MAXIMAL );
		maximal.setValue( String.valueOf( fuzzerScanConfig.getMaximal() ) );

		maximal.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				try
				{
					if( "".equals( newValue ) )
						return;
					Integer.valueOf( newValue );
					fuzzerScanConfig.setMaximal( Integer.valueOf( newValue ) );
				}
				catch( Exception e )
				{
					UISupport.showErrorMessage( "Value must be integer number" );
				}
			}
		} );
	}

	private void numberOfRequestField( final FuzzerScanConfig fuzzerScanConfig )
	{
		XFormField numberOfRequest = dialog.getFormField( AdvancedSettings.NUMBER_OF_REQUEST );
		numberOfRequest.setValue( String.valueOf( fuzzerScanConfig.getNumberOfRequest() ) );

		numberOfRequest.addFormFieldListener( new XFormFieldListener()
		{

			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				try
				{
					if( "".equals( newValue ) )
						return;
					Integer.valueOf( newValue );
					fuzzerScanConfig.setNumberOfRequest( Integer.valueOf( newValue ) );
				}
				catch( Exception e )
				{
					UISupport.showErrorMessage( "Value must be integer number" );
				}
			}
		} );
	}


	@AForm( description = "Fuzzer Scan", name = "Fuzzer Scan" )
	protected interface AdvancedSettings
	{

		@AField( description = "Minimal length of Fuzzed value", name = "Minimal length", type = AFieldType.INT )
		public final static String MINIMAL = "Minimal length";

		@AField( description = "Maximal length of Fuzzed value", name = "Maximal length", type = AFieldType.INT )
		public final static String MAXIMAL = "Maximal length";

		@AField( description = "Number of Fuzzed Requests to do", name = "Number of Requests", type = AFieldType.INT )
		public final static String NUMBER_OF_REQUEST = "Number of Requests";

	}

}
