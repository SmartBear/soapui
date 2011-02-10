/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.check;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.commons.collections.ArrayStack;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.config.CheckedParametersListConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.boundary.SchemeTypeExtractor;
import com.eviware.soapui.security.boundary.SchemeTypeExtractor.NodeInfo;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormMultiSelectList;
import com.eviware.x.form.support.AField.AFieldType;

public class InvalidTypesSecurityCheck extends AbstractSecurityCheck
{

	public final static String TYPE = "InvalidTypesSecurityCheck";

	private SchemeTypeExtractor extractor;

	XFormDialog dialog;

	private CheckedParametersListConfig invalidTypesConfig;

	private boolean hasNext;

	private InvalidTypes invalidTypes;

	private ArrayList<String> result = new ArrayList<String>();

	public InvalidTypesSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );

		config.setConfig( CheckedParametersListConfig.Factory.newInstance() );
		if( config.getConfig() == null )
		{
			invalidTypesConfig = CheckedParametersListConfig.Factory.newInstance();
			config.setConfig( invalidTypesConfig );
		}
		invalidTypesConfig = ( CheckedParametersListConfig )config.getConfig();
		extractor = new SchemeTypeExtractor( testStep );
		if( invalidTypesConfig.getParametersList() == null || invalidTypesConfig.getParametersList().size() == 0 )
		{
			try
			{
				extractor.extract();
				TreeMap<String, NodeInfo> params = extractor.getParams();
				// what to do if request is changed????
//				for( String key : params.keySet() )
//				{
//					CheckedParameterConfig param = invalidTypesConfig.addNewParameters();
////					param.setParameterName( params.get( key ).getSimpleName() );
//					param.setXpath( params.get( key ).getXPath() );
//					param.setChecked( false );
//					param.setType( String.valueOf( params.get( key ).getType() ) );
//				}
			}
			catch( Exception e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * Do we need this?
	 */
	// XXX: add rest and http.
	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof WsdlTestRequestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public boolean configure()
	{
		if( dialog == null )
			buildDialog();
		if( dialog != null )
		{
			XFormMultiSelectList field = ( XFormMultiSelectList )dialog.getFormField( InvalidTypesConfigDialog.PARAMETERS );
			ArrayList<String> selected = new ArrayList<String>();
			for( CheckedParameterConfig param : invalidTypesConfig.getParametersList() )
				if( param.isSetChecked() )
					selected.add( param.getParameterName() );
			field.setSelectedOptions( selected.toArray( new String[0] ) );
			if( dialog.show() )
			{
				for( CheckedParameterConfig param : invalidTypesConfig.getParametersList() )
				{
					{
						for( Object key : field.getSelectedOptions() )
							if( param.getParameterName().equals( ( String )key ) )
							{
								param.setChecked( true );
								break;
							}
						param.setChecked( false );
					}
				}
			}
		}
		return true;
	}

	@Override
	protected void buildDialog()
	{
		dialog = ADialogBuilder.buildDialog( InvalidTypesConfigDialog.class );
		XFormMultiSelectList field = ( XFormMultiSelectList )dialog.getFormField( InvalidTypesConfigDialog.PARAMETERS );
		ArrayList<String> options = new ArrayList<String>();
		ArrayList<String> selected = new ArrayList<String>();
		for( CheckedParameterConfig param : invalidTypesConfig.getParametersList() )
		{
			options.add( param.getParameterName() );
			if( param.isSetChecked() )
				selected.add( param.getParameterName() );
		}
		field.setOptions( options.toArray( new String[0] ) );
		field.setSelectedOptions( selected.toArray( new String[0] ) );

	}

	@Override
	public boolean isConfigurable()
	{
		return true;
	}

	@AForm( description = "Configure Invalid Types Check", name = "Invalid Types Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface InvalidTypesConfigDialog
	{

		@AField( description = "Parameters to Check", name = "Select parameters to check", type = AFieldType.MULTILIST )
		public final static String PARAMETERS = "Select parameters to check";

	}

	@Override
	protected void execute( TestStep testStep, SecurityTestRunContext context )
	{
		updateRequestContent();

		WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( ( WsdlTestCase )this.testStep.getTestCase(),
				new StringToObjectMap() );

		testStep.run( testCaseRunner, testCaseRunner.getRunContext() );
	}

	private void updateRequestContent()
	{
		try
		{
			generateRequests();
			if( result.size() > 0 )
			{
				testStep.getProperty( "Request" ).setValue( result.get( 0 ) );
				result.remove( 0 );
			}
			if( result.size() == 0 )
				hasNext = false;

		}
		catch( XmlException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * generate set of requests with all variations
	 * 
	 * @throws XmlException
	 */
	private void generateRequests() throws XmlException
	{

		if( result.size() == 0 )
		{
			hasNext = true;
			String templateRequest = testStep.getProperty( "Request" ).getValue();
			for( CheckedParameterConfig param : invalidTypesConfig.getParametersList() )
			{
				if( param.isSetChecked() )
				{
					InvalidTypes invalidGenerator = new InvalidTypes( Integer.valueOf( param.getType() ) );
					while( invalidGenerator.hasNext() )
					{
						invalidGenerator.getNext();
						result.add( XmlUtils.setXPathContent( templateRequest, param.getXpath(), invalidGenerator.getNext()
								.toString() ) );
					}
				}
			}
		}
	}

	@Override
	protected boolean hasNext()
	{
		return hasNext;
	}

	private class InvalidTypes
	{

		private int type;
		private ArrayStack stack;

		public InvalidTypes( int type )
		{
			this.type = type;
			generateInvalidTypes();

		}

		private void generateInvalidTypes()
		{

			stack = new ArrayStack();
			ArrayList<InvalidType> invalidTypes = new ArrayList<InvalidType>();

			stack.push( new InvalidType( SchemaType.BTC_BOOLEAN, new Boolean( true ) ) );
			stack.push( new InvalidType( SchemaType.BTC_INTEGER, new Integer( 10 ) ) );
			stack.push( new InvalidType( SchemaType.BTC_STRING, "simple" ) );

		}

		public Object getNext()
		{
			InvalidType result = ( InvalidType )stack.pop();
			if( result.getType() == type )
				return ( ( InvalidType )stack.pop() ).getValue();
			else
				return result.getValue();
		}

		public boolean hasNext()
		{
			return !stack.isEmpty();
		}

		class InvalidType<T>
		{

			public int type;
			public T value;

			public InvalidType( int type, T value )
			{
				this.type = type;
				this.value = value;
			}

			public int getType()
			{
				return type;
			}

			public T getValue()
			{
				return value;
			}

		}

	}

}
