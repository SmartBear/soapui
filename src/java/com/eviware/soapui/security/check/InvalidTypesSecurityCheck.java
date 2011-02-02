package com.eviware.soapui.security.check;

import java.util.ArrayList;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.boundary.SchemeTypeExtractor;
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

	private XFormDialog dialog;
	
	public final static String TYPE = "InvalidTypesSecurityCheck";
	
	private SchemeTypeExtractor extractor;

	private ArrayList<String> params;

	public InvalidTypesSecurityCheck( TestStep testStep, SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( testStep, config, parent, icon );

		extractor = new SchemeTypeExtractor( testStep );
		try
		{
			extractor.extract();
			
			params = extractor.getParams();
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
	//XXX: add rest and http.
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
		if ( dialog == null) 
			buildDialog();
		return dialog.show();
	}
	
	
	@Override
	protected void buildDialog()
	{
		dialog = ADialogBuilder.buildDialog( InvalidTypesConfigDialog.class );
		XFormMultiSelectList field = ( XFormMultiSelectList )dialog.getFormField( InvalidTypesConfigDialog.PARAMETERS );
		field.setOptions( extractor.getParamsAsArray() );
	}
	
	@Override
	public boolean isConfigurable()
	{
		return true;
	}
	
	@AForm( description = "Configure Invalid Types Check", name = "Invalid Types Security Check", helpUrl = HelpUrls.MOCKASWAR_HELP_URL )
	protected interface InvalidTypesConfigDialog {
		
		@AField( description = "Parameters to Check", name = "Select parameters to check", type = AFieldType.MULTILIST )
		public final static String PARAMETERS = "Select parameters to check";
		
	}

}
