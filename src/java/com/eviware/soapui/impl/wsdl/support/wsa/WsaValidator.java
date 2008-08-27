package com.eviware.soapui.impl.wsdl.support.wsa;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Validating class for WS Addressing
 * implemented according to WSDL 1.1 specification
 * @see {@link}http://www.w3.org/TR/2006/WD-ws-addr-wsdl-20060216/#WSDL11MEPS
 * @author dragica.soldo
 *
 */
public class WsaValidator
{
	MessageExchange messageExchange;
	Element header;
	String wsaVersionNameSpace;
	
	
	public WsaValidator(MessageExchange messageExchange) {
		this.messageExchange = messageExchange;
	}
	
	private void validateWsAddressingCommon() throws AssertionException
	{
		String responseContent = messageExchange.getResponseContent();
		try
		{
			SoapVersion soapVersion = ((WsdlMessageExchange) messageExchange).getOperation().getInterface()
					.getSoapVersion();

			XmlObject xmlObject = XmlObject.Factory.parse(responseContent);
			XmlObject[] envS = xmlObject.selectChildren(soapVersion.getEnvelopeQName());
			Element envelope = (Element) envS[0].getDomNode();
			
			header = (Element) SoapUtils.getHeaderElement(xmlObject, soapVersion, true).getDomNode();
			
			if (!header.hasChildNodes())
			{
				throw new AssertionException( new AssertionError("WS-A not enabled") );
			}
			
			String wsaNameSpace = header.getAttribute("xmlns:wsa");
			if (wsaNameSpace == null || wsaNameSpace.isEmpty())
			{
				wsaNameSpace = envelope.getAttribute("xmlns:wsa");
				if (wsaNameSpace == null || wsaNameSpace.isEmpty())
				{
					throw new AssertionException( new AssertionError("WS-A version is missing.") );
				}
			}
			if (wsaNameSpace.equals(WsaUtils.WS_A_VERSION_200508))
			{
				wsaVersionNameSpace = WsaUtils.WS_A_VERSION_200508;
			} else if (wsaNameSpace.equals(WsaUtils.WS_A_VERSION_200408))
			{
				wsaVersionNameSpace = WsaUtils.WS_A_VERSION_200408;
			} else {
				throw new AssertionException( new AssertionError("WS-A version is wrong") );
			}
			
			//Action is Mandatory
			Element actionNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "Action");
			if (actionNode == null)
			{
				throw new AssertionException( new AssertionError("WS-A Action property is not specified") );
			}
			String actionValue = XmlUtils.getElementText(actionNode);
			if (StringUtils.isNullOrEmpty(actionValue))
			{
				throw new AssertionException( new AssertionError("WS-A Action property is empty") );
			}
			
			//To is Mandatory
			Element toNode = XmlUtils.getFirstChildElementNS(header,wsaVersionNameSpace,"To");
			if (toNode == null) {
				throw new AssertionException( new AssertionError("WS-A To property is not specified") );
			}
			Element addressNode = XmlUtils.getFirstChildElementNS(toNode, wsaVersionNameSpace, "Address");
			if( addressNode == null )
			{
				throw new AssertionException( new AssertionError("WS-A To Address property is not specified") );
			}
			String toAddressValue = XmlUtils.getElementText(addressNode);
			if (StringUtils.isNullOrEmpty(toAddressValue))
			{
				throw new AssertionException( new AssertionError("WS-A To Address property is empty") );
			}
				
		}
		catch (XmlException e)
		{
			SoapUI.logError(e);
		}
	}
	public void validateWsAddressingRequest() throws AssertionException
	{
		validateWsAddressingCommon();
		Operation operation = messageExchange.getOperation();
		
		if (operation.isRequestResponse())
		{
			//MessageId is Mandatory
			Element msgNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "MessageID");
			if (msgNode == null)
			{
				throw new AssertionException( new AssertionError("WS-A MessageID property is not specified") );
			}
			String msgValue = XmlUtils.getElementText(msgNode);
			if (StringUtils.isNullOrEmpty(msgValue))
			{
				throw new AssertionException( new AssertionError("WS-A MessageID property is empty") );
			}
			
			//ReplyTo is Mandatory
			Element replyToNode = XmlUtils.getFirstChildElementNS(header,wsaVersionNameSpace, "ReplyTo");
			if (replyToNode == null)
			{
				throw new AssertionException( new AssertionError("WS-A ReplyTo property is not specified") );
			}
			Element addressNode = XmlUtils.getFirstChildElementNS(replyToNode, wsaVersionNameSpace, "Address");
			if (addressNode == null)
			{
				throw new AssertionException( new AssertionError("WS-A ReplyTo Address property is not specified") );
			}
			String replyToAddressValue = XmlUtils.getElementText(addressNode);
			if (StringUtils.isNullOrEmpty(replyToAddressValue))
			{
				throw new AssertionException( new AssertionError("WS-A ReplyTo Address property is empty") );
			}
		}
	}
	public void validateWsAddressingResponse() throws AssertionException
	{
		validateWsAddressingCommon();
		
		//To is Mandatory
		Element relatesToNode = XmlUtils.getFirstChildElementNS(header,wsaVersionNameSpace, "RelatesTo");
		if (relatesToNode == null) {
			throw new AssertionException( new AssertionError("WS-A RelatesTo property is not specified") );
		}
		String relationshipType = relatesToNode.getAttribute("RelationshipType");
		if (StringUtils.isNullOrEmpty(relationshipType))
		{
			throw new AssertionException( new AssertionError("WS-A RelationshipType is not specified") );
		}
		Element relatesToAddressNode = XmlUtils.getFirstChildElementNS(relatesToNode,wsaVersionNameSpace, "Address");
		if (relatesToAddressNode == null)
		{
			throw new AssertionException( new AssertionError("WS-A RelatesTo Address property is not specified") );
		}
		String relatesToAddressesValue = XmlUtils.getElementText(relatesToAddressNode);
		if (StringUtils.isNullOrEmpty(relatesToAddressesValue))
		{
			throw new AssertionException( new AssertionError("WS-A RelatesTo Address property is empty") );
		}
	}
	

}
