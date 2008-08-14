package com.eviware.soapui.impl.wsdl.support.soap;

import java.text.Collator;
import java.util.Comparator;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.eviware.soapui.config.WsaVersionTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.BindingImporter;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;

public abstract class AbstractSoapBindingImporter implements BindingImporter
{

	protected static final class BindingOperationComparator implements Comparator<BindingOperation>
		{
			public int compare(BindingOperation o1, BindingOperation o2)
			{
			   return Collator.getInstance().compare( o1.getOperation().getName(), o2.getOperation().getName() );
			}
		}

	protected void initWsAddressing(Binding binding, WsdlInterface iface)
	{
		Element[] usingAddressingElements = WsdlUtils.getExentsibilityElements(binding, new QName("http://www.w3.org/2006/05/addressing/wsdl","UsingAddressing"));
	   if( usingAddressingElements.length == 0) {
	   	usingAddressingElements = WsdlUtils.getExentsibilityElements(binding, new QName("http://www.w3.org/2006/02/addressing/wsdl","UsingAddressing"));
	   }
	   
	   if (usingAddressingElements.length != 0) {
	   	//this should resolve wsdl version, not addressing version??? what is the connection?
	   	String addressingVersion = usingAddressingElements[0].getAttributeNS("http://schemas.xmlsoap.org/wsdl/","required");
	   	if (addressingVersion != null && addressingVersion.equals("true"))
			{
	         iface.setWsaVersion(WsaVersionTypeConfig.X_200508.toString());
			} 
	   	
	   } else {
	   	iface.setWsaVersion(WsaVersionTypeConfig.NONE.toString());
	   }
	}

	public AbstractSoapBindingImporter()
	{
		super();
	}

}