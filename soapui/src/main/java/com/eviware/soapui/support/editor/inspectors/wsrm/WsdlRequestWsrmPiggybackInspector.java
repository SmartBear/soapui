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

package com.eviware.soapui.support.editor.inspectors.wsrm;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.editor.xml.XmlInspector;
import com.eviware.soapui.support.xml.XmlUtils;

public class WsdlRequestWsrmPiggybackInspector extends AbstractWsrmInspector implements XmlInspector, SubmitListener
{

	private final WsdlRequest request;

	protected WsdlRequestWsrmPiggybackInspector( WsdlRequest request )
	{
		super( request );
		request.addSubmitListener( this );
		this.request = request;
	}

	@Override
	public void release()
	{
		super.release();
		request.removeSubmitListener( this );
	}

	public void afterSubmit( Submit submit, SubmitContext context )
	{

		if( request.getWsrmConfig().isWsrmEnabled() && submit.getResponse() != null )
		{
			String content = submit.getResponse().getContentAsString();
			XmlOptions options = new XmlOptions();
			try
			{
				// XmlObject xml = XmlObject.Factory.parse( content );
				XmlObject xml = XmlUtils.createXmlObject( content );

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
