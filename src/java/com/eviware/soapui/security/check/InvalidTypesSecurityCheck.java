package com.eviware.soapui.security.check;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.config.CheckedParameterConfig;
import com.eviware.soapui.config.CheckedParametersListConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.boundary.SchemeTypeExtractor;
import com.eviware.soapui.security.boundary.SchemeTypeExtractor.NodeInfo;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
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

	private TreeMap<String, NodeInfo> params;

	XFormDialog dialog;

	private CheckedParametersListConfig invalidTypesConfig;

	public InvalidTypesSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );

		if( config.getConfig() == null )
		{
			// Does confiruration have sense if request changes?
			invalidTypesConfig = CheckedParametersListConfig.Factory.newInstance();

			config.setConfig( invalidTypesConfig );
		}
		invalidTypesConfig = ( CheckedParametersListConfig )config.getConfig();
		extractor = new SchemeTypeExtractor( testStep );
		try
		{
			extractor.extract();
			params = extractor.getParams();
			// what to do if request is changed????
			if( invalidTypesConfig.getParametersList() != null )
			{
				ArrayList<CheckedParameterConfig> newParams = new ArrayList<CheckedParameterConfig>();
				for( CheckedParameterConfig param : invalidTypesConfig.getParametersList() )
					if( params.containsKey( param.getParameterName() ) )
						newParams.add( param );
				invalidTypesConfig.setParametersArray( newParams.toArray( new CheckedParameterConfig[0] ) );
			}
		}
		catch( XmlException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public SecurityCheckRequestResult analyze( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityCheckResult )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SecurityCheckRequestResult execute( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityChekResult )
	{
		// TODO Auto-generated method stub
		return null;
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
			for( CheckedParameterConfig parm : invalidTypesConfig.getParametersList() )
				selected.add( parm.getParameterName() );
			field.setSelectedOptions( selected.toArray( new String[0] ) );
			if( dialog.show() )
			{

				invalidTypesConfig.getParametersList().clear();
				for( NodeInfo param : params.values() )
					param.setSelected( false );
				for( Object key : field.getSelectedOptions() )
				{
					CheckedParameterConfig configParam = invalidTypesConfig.addNewParameters();
					configParam.setParameterName( params.get( key ).getSimpleName() );
					params.get( key ).setSelected( true );
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
		field.setOptions( params.keySet().toArray( new String[0] ) );
		ArrayList<String> selected = new ArrayList<String>();
		for( CheckedParameterConfig parm : invalidTypesConfig.getParametersList() )
			selected.add( parm.getParameterName() );
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
	protected void executeNew( TestStep testStep, SecurityTestRunContext context )
	{
		// TODO Auto-generated method stub
		super.executeNew( testStep, context );
	}
	
	@Override
	protected void analyzeNew( TestStep testStep, SecurityTestRunContext context )
	{
		// TODO Auto-generated method stub
		super.analyzeNew( testStep, context );
	}

}
