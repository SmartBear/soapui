/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org. 
 */

package com.eviware.soapui.support.editor.inspectors.wsrm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.config.WsrmVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsmc.WsmcInjection;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmContainer;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmSequence;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmUtils;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.xml.XmlInspector;

/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

public class WsdlRequestWsrmInspector extends AbstractWsrmInspector implements XmlInspector, PropertyChangeListener,
		SubmitListener
{
	private final WsdlRequest request;

	public WsdlRequestWsrmInspector( WsdlRequest request )
	{
		super( request );

		request.addSubmitListener( this );
		this.request = request;
	}

	public void buildContent( SimpleBindingForm form )
	{
		form.addSpace( 5 );
		form.appendCheckBox( "wsrmEnabled", "Enable WS-Reliable Messaging", "Enable/Disable WS-Reliable Messaging" );
		form.addSpace( 5 );

		form.appendComboBox( "version", "WS-RM Version", new String[] { WsrmVersionTypeConfig.X_1_0.toString(),
				WsrmVersionTypeConfig.X_1_1.toString(), WsrmVersionTypeConfig.X_1_2.toString() },
				"The  property for managing WS-RM version" );

		form.appendTextField( "ackTo", "Acknowledgment to",
				"The acknowledgment endpoint reference, will be generated if left empty" );

		form.addSpace( 5 );
	}

	public void propertyChange( PropertyChangeEvent arg0 )
	{
		// TODO Auto-generated method stub

	}

	public void afterSubmit( Submit submit, SubmitContext context )
	{
		WsrmContainer container = ( WsrmContainer )submit.getRequest();
		if( request.getWsrmConfig().isWsrmEnabled() )
		{
			String content = submit.getResponse().getContentAsString();
			XmlOptions options = new XmlOptions();
			try
			{
				XmlObject xml = XmlObject.Factory.parse( content );

				String namespaceDeclaration = "declare namespace wsrm='" + request.getWsrmConfig().getVersionNameSpace()
						+ "';";
				XmlObject result[] = xml.selectPath( namespaceDeclaration + "//wsrm:AcknowledgementRange", options );

				if( result.length > 0 )
				{
					for( int i = 0; i < result.length; i++ )
					{
						String upper = result[i].selectAttribute( null, "Upper" ).getDomNode().getNodeValue();
						String lower = result[i].selectAttribute( null, "Lower" ).getDomNode().getNodeValue();

						if( lower == upper )
						{
							Logger.getLogger( "wsrm" ).info(
									"Acknowledgment for message " + upper + " received for identifier: "
											+ request.getWsrmConfig().getSequenceIdentifier() );
						}
						else
						{
							Logger.getLogger( "wsrm" ).info(
									"Acknowledgment for messages " + lower + " to " + upper + " received for identifier: "
											+ request.getWsrmConfig().getSequenceIdentifier() );
						}
					}
				}
			}
			catch( XmlException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if( container.getWsrmConfig().isWsrmEnabled() )
		{
			WsdlInterface iface = request.getOperation().getInterface();
			WsrmUtils utils = new WsrmUtils( iface.getSoapVersion() );
			utils.closeSequence( request.getEndpoint(), iface.getSoapVersion(), request.getWsrmConfig()
					.getVersionNameSpace(), request.getWsrmConfig().getUuid(), request.getWsrmConfig()
					.getSequenceIdentifier(), 1l, request.getOperation() );
		}
	}

	public boolean beforeSubmit( Submit submit, SubmitContext context )
	{
		WsrmContainer container = ( WsrmContainer )submit.getRequest();
		if( container.getWsrmConfig().isWsrmEnabled() )
		{
			WsdlInterface iface = request.getOperation().getInterface();
			WsrmUtils utils = new WsrmUtils( iface.getSoapVersion() );

			WsrmSequence sequence = utils.createSequence( request.getEndpoint(), iface.getSoapVersion(), request
					.getWsrmConfig().getVersionNameSpace(), request.getWsrmConfig().getAckTo(), 0l, request.getOperation(),
					( ( WsdlRequest )submit.getRequest() ).getWsaConfig().getTo() );

			request.getWsrmConfig().setSequenceIdentifier( sequence.getIdentifier() );
			request.getWsrmConfig().setUuid( sequence.getUuid() );

			if( request.getWsrmConfig().getVersion() != WsrmVersionTypeConfig.X_1_0.toString() )
			{
				WsmcInjection receiveInjection = new WsmcInjection( request.getEndpoint(), request.getOperation(), iface
						.getSoapVersion(), request.getWsrmConfig().getUuid() );
				request.setAfterRequestInjection( receiveInjection );
			}

		}
		return true;
	}

}
