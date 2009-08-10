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

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.editor.xml.XmlInspector;

public class WsdlRequestWsrmPiggybackInspector extends AbstractWsrmInspector implements XmlInspector,
		PropertyChangeListener, SubmitListener
{

	private final WsdlRequest request;

	protected WsdlRequestWsrmPiggybackInspector( WsdlRequest request )
	{
		super( request );
		request.addSubmitListener( this );
		this.request = request;
	}

	public void propertyChange( PropertyChangeEvent arg0 )
	{
		// TODO Auto-generated method stub

	}

	public void afterSubmit( Submit submit, SubmitContext context )
	{

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

	}

	public boolean beforeSubmit( Submit submit, SubmitContext context )
	{

		return true;
	}

}
