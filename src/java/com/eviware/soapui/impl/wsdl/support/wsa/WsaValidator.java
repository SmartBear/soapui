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
			NodeList actionList = header.getElementsByTagNameNS(wsaVersionNameSpace, "Action");
			if (actionList.getLength() == 0 || actionList.item(0).getFirstChild() == null)
			{
				throw new AssertionException( new AssertionError("WS-A Action property is not specified") );
			}
			
			//To is Mandatory
			NodeList destinationList = header.getElementsByTagNameNS(wsaVersionNameSpace, "To");
			if (destinationList.getLength() == 0 || destinationList.item(0).getFirstChild() == null) {
				throw new AssertionException( new AssertionError("WS-A To property is not specified") );
			}
			Element toNode = (Element)destinationList.item(0).getChildNodes();
			NodeList toAddresses = toNode.getElementsByTagNameNS(wsaVersionNameSpace, "Address");
			if (toAddresses.getLength() == 0)
			{
				throw new AssertionException( new AssertionError("WS-A To Address property is not specified") );
			}
			String toAddressValue = toAddresses.item(0).getFirstChild().getNodeValue();
			if (toAddressValue == null || toAddressValue.isEmpty())
			{
				throw new AssertionException( new AssertionError("WS-A To Address property is not specified") );
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
			NodeList msgIdsList = header.getElementsByTagNameNS(wsaVersionNameSpace, "MessageID");
			if (msgIdsList.getLength() == 0 || msgIdsList.item(0).getFirstChild() == null)
			{
				throw new AssertionException( new AssertionError("WS-A MessageID property is not specified") );
			}
			
			//ReplyTo is Mandatory
			NodeList repliesToList = header.getElementsByTagNameNS(wsaVersionNameSpace, "ReplyTo");
			if (repliesToList.getLength() == 0 || repliesToList.item(0).getFirstChild() == null) {
				throw new AssertionException( new AssertionError("WS-A ReplyTo property is not specified") );
			}
			Element replyToNode = (Element)repliesToList.item(0).getChildNodes();
			NodeList replyToAddresses = replyToNode.getElementsByTagNameNS(wsaVersionNameSpace, "Address");
			if (replyToAddresses.getLength() == 0 )
			{
				throw new AssertionException( new AssertionError("WS-A ReplyTo Address property is not specified") );
			}
			String replyToAddressValue = replyToAddresses.item(0).getFirstChild().getNodeValue();
			if (replyToAddressValue == null || replyToAddressValue.isEmpty())
			{
				throw new AssertionException( new AssertionError("WS-A ReplyTo Address property is not specified") );
			}
		}
	}
	public void validateWsAddressingResponse() throws AssertionException
	{
		validateWsAddressingCommon();
		
		//To is Mandatory
		NodeList relatesToList = header.getElementsByTagNameNS(wsaVersionNameSpace, "RelatesTo");
		if (relatesToList.getLength() == 0 || relatesToList.item(0).getFirstChild() == null) {
			throw new AssertionException( new AssertionError("WS-A RelatesTo property is not specified") );
		}
		Element relatesToNode = (Element)relatesToList.item(0).getChildNodes();
		String relationshipType = relatesToNode.getAttribute("RelationshipType");
		if (relationshipType == null || relationshipType.isEmpty())
		{
			throw new AssertionException( new AssertionError("WS-A RelationshipType is not specified") );
		}
		NodeList relatesToAddresses = relatesToNode.getElementsByTagNameNS(wsaVersionNameSpace, "Address");
		if (relatesToAddresses.getLength() == 0)
		{
			throw new AssertionException( new AssertionError("WS-A RelatesTo Address property is not specified") );
		}
		String relatesToAddressesValue = relatesToAddresses.item(0).getFirstChild().getNodeValue();
		if (relatesToAddressesValue == null || relatesToAddressesValue.isEmpty())
		{
			throw new AssertionException( new AssertionError("WS-A RelatesTo Address property is not specified") );
		}
	}
	

}
