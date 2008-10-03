/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.support.wsa;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.AnonymousTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Validating class for WS Addressing implemented according to WSDL 1.1
 * specification
 * 
 * @author dragica.soldo
 * @see {@link}http://www.w3.org/TR/2006/WD-ws-addr-wsdl-20060216/#WSDL11MEPS
 */
public class WsaValidator
{
	WsdlMessageExchange messageExchange;
	Element header;
	String wsaVersionNameSpace;
	StringBuilder cumulativeErrorMsg;

	public WsaValidator(WsdlMessageExchange messageExchange)
	{
		this.messageExchange = messageExchange;
		cumulativeErrorMsg = new StringBuilder();
	}

	private void getWsaVersion(String content) throws XmlException, AssertionException
	{
		SoapVersion soapVersion = messageExchange.getOperation().getInterface()
		        .getSoapVersion();

		XmlObject xmlObject = XmlObject.Factory.parse( content );
		XmlObject[] envS = xmlObject.selectChildren( soapVersion.getEnvelopeQName() );
		Element envelope = (Element) envS[0].getDomNode();

		header = (Element) SoapUtils.getHeaderElement( xmlObject, soapVersion, true ).getDomNode();

		if( !header.hasChildNodes() )
		{
		   throw new AssertionException( new AssertionError( "WS-A not enabled" ) );
		}

		String wsaNameSpace = header.getAttribute( "xmlns:wsa" );
		if( wsaNameSpace == null || wsaNameSpace.length() == 0)
		{
		   wsaNameSpace = envelope.getAttribute( "xmlns:wsa" );
		   if( wsaNameSpace == null || wsaNameSpace.length() == 0 )
		   {
		      throw new AssertionException( new AssertionError( "WS-A version is missing." ) );
		   }
		}
		if( wsaNameSpace.equals( WsaUtils.WS_A_VERSION_200508 ) )
		{
		   wsaVersionNameSpace = WsaUtils.WS_A_VERSION_200508;
		}
		else if( wsaNameSpace.equals( WsaUtils.WS_A_VERSION_200408 ) )
		{
		   wsaVersionNameSpace = WsaUtils.WS_A_VERSION_200408;
		}
		else
		{
		   throw new AssertionException( new AssertionError( "WS-A version is wrong" ) );
		}
	}
	private void validateWsAddressingCommon( String content ) 
   {
//         // Action is Mandatory
//         Element actionNode = XmlUtils.getFirstChildElementNS( header, wsaVersionNameSpace, "Action" );
//         if( actionNode == null )
//         {
//            throw new AssertionException( new AssertionError( "WS-A Action property is not specified" ) );
//         }
//         String actionValue = XmlUtils.getElementText( actionNode );
//         if( StringUtils.isNullOrEmpty( actionValue ) )
//         {
//            throw new AssertionException( new AssertionError( "WS-A Action property is empty" ) );
//         }
//
         // To is Mandatory
         Element toNode = XmlUtils.getFirstChildElementNS( header, wsaVersionNameSpace, "To" );
         if( toNode == null )
         {
//            throw new AssertionException( new AssertionError( "WS-A To property is not specified" ) );
         	cumulativeErrorMsg.append("WS-A To property is not specified. ");
         } else {
//	         Element addressNode = XmlUtils.getFirstChildElementNS( toNode, wsaVersionNameSpace, "Address" );
//	         if( addressNode == null )
//	         {
//	//            throw new AssertionException( new AssertionError( "WS-A To Address property is not specified" ) );
//	         	cumulativeErrorMsg.append("WS-A To Address property is not specified. ");
//	         } else {
		         String toAddressValue = XmlUtils.getElementText( toNode );
		         if( StringUtils.isNullOrEmpty( toAddressValue ) )
		         {
//		            throw new AssertionException( new AssertionError( "WS-A To Address property is empty" ) );
		         	cumulativeErrorMsg.append("WS-A To property is empty. ");
		         }
		         else
		         {
		            // check for anonymous - in case of mock response to=request.replyTo
		            if( AnonymousTypeConfig.PROHIBITED.toString().equals( messageExchange.getOperation().getAnonymous() )
		                    && WsaUtils.isAnonymousAddress(toAddressValue,wsaVersionNameSpace) )
		            {
//							throw new AssertionException( new AssertionError("WS-A InvalidAddressingHeader To , Anonymous addresses are prohibited") );
		            	cumulativeErrorMsg.append("WS-A InvalidAddressingHeader To , Anonymous addresses are prohibited. ");
		            }
		         }
//	         }
         }
         // if fault_to is specified check if anonymous allowed
         Element faultToNode = XmlUtils.getFirstChildElementNS( header, wsaVersionNameSpace, "FaultTo" );
         if( faultToNode != null )
         {
            Element addressNode = XmlUtils.getFirstChildElementNS( faultToNode, wsaVersionNameSpace, "Address" );
            if( addressNode != null )
            {
               String faultToAddressValue = XmlUtils.getElementText( addressNode );
               if( !StringUtils.isNullOrEmpty( faultToAddressValue ) )
               {
                  // check for anonymous
                  if( AnonymousTypeConfig.PROHIBITED.toString().equals( messageExchange.getOperation().getAnonymous() )
                          && WsaUtils.isAnonymousAddress(faultToAddressValue,wsaVersionNameSpace) )
                  {
//                     throw new AssertionException( new AssertionError( "WS-A InvalidAddressingHeader FaultTo , Anonymous addresses are prohibited" ) );
                  	cumulativeErrorMsg.append("WS-A InvalidAddressingHeader FaultTo , Anonymous addresses are prohibited. ");
                  } else if (AnonymousTypeConfig.REQUIRED.toString().equals( ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous() )
                        && !(WsaUtils.isAnonymousAddress(faultToAddressValue,wsaVersionNameSpace) || WsaUtils.isNoneAddress(faultToAddressValue,wsaVersionNameSpace)))
                  {
//                  	throw new AssertionException( new AssertionError( "WS-A InvalidAddressingHeader FaultTo , Anonymous addresses are required" ) );
                  	cumulativeErrorMsg.append("WS-A InvalidAddressingHeader FaultTo , Anonymous addresses are required. ");
                  }
               }
            }
         }

   }


	public void validateWsAddressingRequest() throws AssertionException
	{
		String content = messageExchange.getRequestContent();
      try
		{
			getWsaVersion(content);
		}
		catch (XmlException e)
		{
			SoapUI.logError(e);
		}

		WsdlOperation operation = messageExchange.getOperation();

      // Action is Mandatory
      Element actionNode = XmlUtils.getFirstChildElementNS( header, wsaVersionNameSpace, "Action" );
      if( actionNode == null )
      {
//         throw new AssertionException( new AssertionError( "WS-A Action property is not specified" ) );
      	cumulativeErrorMsg.append("WS-A Action property is not specified. ");
      }
      String actionValue = XmlUtils.getElementText( actionNode );
      if( StringUtils.isNullOrEmpty( actionValue ) )
      {
//         throw new AssertionException( new AssertionError( "WS-A Action property is empty" ) );
         cumulativeErrorMsg.append("WS-A Action property is empty. ");
      }

		validateWsAddressingCommon(content);
		if (operation.isRequestResponse())
		{
			// MessageId is Mandatory
			Element msgNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "MessageID");
			if (msgNode == null)
			{
//				throw new AssertionException(new AssertionError("WS-A MessageID property is not specified"));
				cumulativeErrorMsg.append("WS-A MessageID property is not specified. ");
			}
			String msgValue = XmlUtils.getElementText(msgNode);
			if (StringUtils.isNullOrEmpty(msgValue))
			{
//				throw new AssertionException(new AssertionError("WS-A MessageID property is empty"));
				cumulativeErrorMsg.append("WS-A MessageID property is empty");
			}

			// ReplyTo is Mandatory
			Element replyToNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "ReplyTo");
			if (replyToNode == null)
			{
//				throw new AssertionException(new AssertionError("WS-A ReplyTo property is not specified"));
				cumulativeErrorMsg.append("WS-A ReplyTo property is not specified. ");
			}
			Element addressNode = XmlUtils.getFirstChildElementNS(replyToNode, wsaVersionNameSpace, "Address");
			if (addressNode == null)
			{
//				throw new AssertionException(new AssertionError("WS-A ReplyTo Address property is not specified"));
				cumulativeErrorMsg.append("WS-A ReplyTo Address property is not specified. ");
			}
			String replyToAddressValue = XmlUtils.getElementText(addressNode);
			if (StringUtils.isNullOrEmpty(replyToAddressValue))
			{
//				throw new AssertionException(new AssertionError("WS-A ReplyTo Address property is empty"));
				cumulativeErrorMsg.append("WS-A ReplyTo Address property is empty. ");
			} else {
            // check for anonymous
            if( AnonymousTypeConfig.PROHIBITED.toString().equals( ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous() )
                    && WsaUtils.isAnonymousAddress(replyToAddressValue,wsaVersionNameSpace) )
            {
//               throw new AssertionException( new AssertionError( "WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are prohibited" ) );
            	cumulativeErrorMsg.append("WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are prohibited. ");
            } else if (AnonymousTypeConfig.REQUIRED.toString().equals( ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous() )
                  && !(WsaUtils.isAnonymousAddress(replyToAddressValue,wsaVersionNameSpace)|| WsaUtils.isNoneAddress(replyToAddressValue,wsaVersionNameSpace)))
            {
//            	throw new AssertionException( new AssertionError( "WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are required" ) );
            	cumulativeErrorMsg.append("WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are required. ");
            }
			}
		}
      String cumulativeError = cumulativeErrorMsg.toString();
      if (!StringUtils.isNullOrEmpty(cumulativeError))
		{
         throw new AssertionException(new AssertionError(cumulativeError));
		}
	}

	public void validateWsAddressingResponse() throws AssertionException
	{
		String content = messageExchange.getResponseContent();
      try
		{
			getWsaVersion(content);
		}
		catch (XmlException e)
		{
			SoapUI.logError(e);
		}

      // Action is Mandatory
      Element actionNode = XmlUtils.getFirstChildElementNS( header, wsaVersionNameSpace, "Action" );
      if( actionNode == null )
      {
//         throw new AssertionException( new AssertionError( "WS-A Action property is not specified" ) );
      	cumulativeErrorMsg.append("WS-A Action property is not specified. ");
      } else {
	      String actionValue = XmlUtils.getElementText( actionNode );
	      if( StringUtils.isNullOrEmpty( actionValue ) )
	      {
	//         throw new AssertionException( new AssertionError( "WS-A Action property is empty" ) );
	         cumulativeErrorMsg.append("WS-A Action property is empty. ");
	      } else {
		      String defaultWsdlAction = WsdlUtils.getDefaultWsaAction(messageExchange.getOperation(), true);
		      if (!actionValue.equals(defaultWsdlAction))
				{
		//      	throw new AssertionException( new AssertionError( "WS-A Action property has the wrong value" ) );
		      	cumulativeErrorMsg.append("WS-A Action property should be " + defaultWsdlAction + ". ");
				}
	      }
      }
		validateWsAddressingCommon(content);

		// RelatesTo is Mandatory
		Element relatesToNode = XmlUtils.getFirstChildElementNS(header, wsaVersionNameSpace, "RelatesTo");
		if (relatesToNode == null)
		{
			cumulativeErrorMsg.append("WS-A RelatesTo property is not specified. ");
		} else {
			String relatesToAddressesValue = XmlUtils.getElementText(relatesToNode);
			if (StringUtils.isNullOrEmpty(relatesToAddressesValue))
			{
				cumulativeErrorMsg.append("WS-A RelatesTo property is empty. ");
			}
			//When absent, the implied value of this attribute is "http://www.w3.org/2005/08/addressing/reply".
//			String relationshipType = relatesToNode.getAttribute("RelationshipType");
//			if (StringUtils.isNullOrEmpty(relationshipType))
//			{
//				relationshipType = relatesToNode.getAttributeNS(WsaUtils.WS_A_VERSION_200508, "RelationshipType");
//				if (StringUtils.isNullOrEmpty(relationshipType))
//				{
//					relationshipType = relatesToNode.getAttributeNS(WsaUtils.WS_A_VERSION_200408, "RelationshipType");
//					if (StringUtils.isNullOrEmpty(relationshipType))
//					{
//						cumulativeErrorMsg.append("WS-A RelationshipType is not specified. ");
//					}
//				} 
//			} 
		}
      // if fault_to is specified check if anonymous allowed
      Element replyToNode = XmlUtils.getFirstChildElementNS( header, wsaVersionNameSpace, "ReplyTo" );
      if( replyToNode != null )
      {
         Element addressNode = XmlUtils.getFirstChildElementNS( replyToNode, wsaVersionNameSpace, "Address" );
         if( addressNode != null )
         {
            String replyToAddressValue = XmlUtils.getElementText( addressNode );
            if( !StringUtils.isNullOrEmpty( replyToAddressValue ) )
            {
               // check for anonymous
               if( AnonymousTypeConfig.PROHIBITED.toString().equals( ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous() )
                       && WsaUtils.isAnonymousAddress(replyToAddressValue,wsaVersionNameSpace) )
               {
//                  throw new AssertionException( new AssertionError( "WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are prohibited" ) );
               	cumulativeErrorMsg.append("WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are prohibited. ");
               } else if (AnonymousTypeConfig.REQUIRED.toString().equals( ((WsdlMessageExchange) messageExchange).getOperation().getAnonymous() )
                     && !(WsaUtils.isAnonymousAddress(replyToAddressValue,wsaVersionNameSpace) || WsaUtils.isNoneAddress(replyToAddressValue,wsaVersionNameSpace)))
               {
//               	throw new AssertionException( new AssertionError( "WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are required" ) );
               	cumulativeErrorMsg.append("WS-A InvalidAddressingHeader ReplyTo , Anonymous addresses are required. ");
               }
            }
         }
      }
      String cumulativeError = cumulativeErrorMsg.toString();
      if (!StringUtils.isNullOrEmpty(cumulativeError))
		{
         throw new AssertionException(new AssertionError(cumulativeError));
		}
	}
}
