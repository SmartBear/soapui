/*
 * Copyright 2004-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
*/

package com.eviware.soapui.support.editor.xml.support;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Default XmlDocument that works on an existing XmlObject
 * 
 * @author ole.matzura
 */

public class XmlObjectXmlDocument extends AbstractXmlDocument
{
	private XmlObject xmlObject;

	public XmlObjectXmlDocument( XmlObject xmlObject )
	{
		this.xmlObject = xmlObject;
	}

	public SchemaTypeSystem getTypeSystem()
	{
		return xmlObject == null ? XmlBeans.getBuiltinTypeSystem() : xmlObject.schemaType().getTypeSystem();
	}

	public String getXml()
	{
		return xmlObject.toString();
	}

	public void setXml( String xml )
	{
		try
		{
			String old = getXml();
			// xmlObject = XmlObject.Factory.parse( xml );
			xmlObject = XmlUtils.createXmlObject( xml );
			fireXmlChanged( old, getXml() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public void release()
	{
		xmlObject = null;
	}
}
