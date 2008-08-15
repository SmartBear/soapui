/**
 * 
 */
package com.eviware.soapui.support.editor.inspectors.wsa;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.editor.xml.XmlInspector;

public class WsdlRequestWsaInspector extends AbstractWsaInspector implements XmlInspector, PropertyChangeListener
{
	private final WsdlRequest request;

	public WsdlRequestWsaInspector( WsdlRequest request )
	{
		super( request );
		this.request = request;
	}

	public void propertyChange(PropertyChangeEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

}