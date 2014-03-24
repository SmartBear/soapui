package com.eviware.soapui.impl.rest.actions.mock;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JTextFieldFormField;

public class AddEmptyRestMockResourceAction extends AbstractSoapUIAction<RestMockService>
{
	public static final String SOAPUI_ACTION_ID = "AddEmptyRestMockResourceAction";

	public AddEmptyRestMockResourceAction()
	{
		super( "Add new mock resource", "Add a new REST mock resource to this mock service" );
	}


	@Override
	public void perform( RestMockService mockService, Object param )
	{
		XFormDialog dialog = ADialogBuilder.buildDialog( Form.class );
		dialog.setOptions( Form.HTTP_METHOD, RestRequestInterface.HttpMethod.getMethodsAsStringArray() );
		dialog.setValue( Form.HTTP_METHOD, RestRequestInterface.HttpMethod.GET.name() );

		JTextFieldFormField formField = (JTextFieldFormField)dialog.getFormField( Form.RESOURCE_PATH );
		formField.getComponent().requestFocus();

		while( dialog.show())
		{
			String resourcePath = dialog.getValue( Form.RESOURCE_PATH );
			String httpMethod = dialog.getValue( Form.HTTP_METHOD );

			if( StringUtils.hasContent( resourcePath ))
			{
				mockService.addEmptyMockAction( RestRequestInterface.HttpMethod.valueOf( httpMethod ), resourcePath );
				break;
			}
			UISupport.showInfoMessage( "The resource path can not be empty" );
		}
	}

	@AForm( name = "Add new mock resource",
			description = "Enter path and HTTP method for your new mock resource",
			helpUrl = HelpUrls.MOCKOPERATION_HELP_URL )
	public interface Form
	{
		@AField( description = "Select HTTP method", type = AField.AFieldType.COMBOBOX )
		public final static String HTTP_METHOD = "Method";

		@AField( description = "Enter resource path", type = AField.AFieldType.STRING )
		public final static String RESOURCE_PATH = "Resource path";
	}

}
