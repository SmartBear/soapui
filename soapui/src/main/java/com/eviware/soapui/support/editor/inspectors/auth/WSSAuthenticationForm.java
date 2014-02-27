package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.types.StringList;

public class WSSAuthenticationForm extends BasicAuthenticationForm<WsdlRequest>
{
	protected WSSAuthenticationForm( WsdlRequest request )
	{
		super( request );
	}

	@Override
	protected void populateBasicForm( SimpleBindingForm basicForm )
	{
		super.populateBasicForm( basicForm );
		StringList outgoingNames = getOutgoingNames( request );
		StringList incomingNames = getIncomingNames( request );

		basicForm.addSpace( GROUP_SPACING );
		basicForm.appendComboBox( "outgoingWss", "Outgoing WSS", outgoingNames.toStringArray(),
				"The outgoing WS-Security configuration to use" );
		basicForm.appendComboBox( "incomingWss", "Incoming WSS", incomingNames.toStringArray(),
				"The incoming WS-Security configuration to use" );
	}

	private StringList getIncomingNames( WsdlRequest request )
	{
		StringList incomingNames = new StringList( request.getOperation().getInterface().getProject()
				.getWssContainer().getIncomingWssNames() );
		incomingNames.add( "" );
		return incomingNames;
	}

	private StringList getOutgoingNames( WsdlRequest request )
	{
		StringList outgoingNames = new StringList( request.getOperation().getInterface().getProject()
				.getWssContainer().getOutgoingWssNames() );
		outgoingNames.add( "" );
		return outgoingNames;
	}
}