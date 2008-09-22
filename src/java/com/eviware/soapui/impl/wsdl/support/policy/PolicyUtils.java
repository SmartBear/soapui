package com.eviware.soapui.impl.wsdl.support.policy;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws.x2004.x09.policy.ExactlyOneDocument;
import org.xmlsoap.schemas.ws.x2004.x09.policy.OperatorContentType;
import org.xmlsoap.schemas.ws.x2004.x09.policy.Policy;
import org.xmlsoap.schemas.ws.x2004.x09.policy.PolicyDocument;

import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.support.StringUtils;

public class PolicyUtils
{
	public static final String WSAM_NAMESPACE = "http://www.w3.org/2007/05/addressing/metadata";
	public final static String WS_POLICY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/policy";
	public final static String WS_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	public static List<Policy> getPolicies(WsdlContext wsdlContext) {
      
		List<Policy> policies = new ArrayList<Policy>();
		try
		{
			List<InterfaceDefinitionPart> parts = wsdlContext.getDefinitionCache().getDefinitionParts();
			for (int i = 0; i < parts.size(); i++)
			{
				InterfaceDefinitionPart part = parts.get(i);
				String content = part.getContent();
				XmlObject xml = XmlObject.Factory.parse(content);
				XmlObject[] paths = xml.selectPath( "declare namespace wsp='" + WS_POLICY_NAMESPACE + "';" + 
				         "//wsp:Policy");	
				
				for( XmlObject obj : paths )
				{
					String xx = obj.xmlText(new XmlOptions().setSaveOuter());
					PolicyDocument policyDocument = PolicyDocument.Factory.parse( xx );
					org.xmlsoap.schemas.ws.x2004.x09.policy.Policy polc = (org.xmlsoap.schemas.ws.x2004.x09.policy.Policy)policyDocument.getPolicy();
					policies.add(polc);
//					List<Addressing> addressingList = polc.getAddressingList();
//					Addressing a = null;
//					if (addressingList.size() > 0 )
//					{
//						a = addressingList.get(0);
//					}
//					AnonymousResponses ar = null;
//					List<AnonymousResponses> anList = polc.getAnonymousResponsesList();
//					if (anList.size() > 0)
//					{
//						ar = anList.get(0);
//					}
				
				}
				
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      
		return null;
	}
	public static boolean isAddressing(Policy policy) {
		
		if (policy.getAddressingList().size() > 0 )
		{
			return true;
		}
		
		return false;
	}
	public static List<Policy> getAddressingPolicies(WsdlContext wsdlContext) {
		List<Policy> addressingPolicies = new ArrayList<Policy>();
		List<Policy> policies = getPolicies(wsdlContext);
		for (Policy policy : policies)
		{
			if (isAddressing(policy))
			{
				addressingPolicies.add(policy);
			}
		}
		return addressingPolicies;
	}
	public static Policy normalize(Policy policy) {
		
//		1.Start with the Element Information Item E (as defined in the XML Information Set [XML Information Set]) of the policy expression. 
//		The [namespace name] of E is always "http://www.w3.org/ns/ws-policy". In the base case, the [local name] property of E is "Policy"; 
//		in the recursive case, the [local name] property of E is "Policy", "ExactlyOne", or "All".
//
//		2.Expand Element Information Items (as defined in the XML Information Set [XML Information Set]) in the [children] property of E that 
//		are policy references per Section 4.3.5 Policy Inclusion.
//
//		3.Convert each Element Information Item C in the [children] property of E into normal form.
		List<OperatorContentType> eoList = policy.getExactlyOneList();
		ExactlyOneDocument.Factory.newInstance();
//
//		3.1 If the [namespace name] property of C is "http://www.w3.org/ns/ws-policy" and the [local name] property of C is "Policy", 
//		"ExactlyOne", or "All", C is an expression of a policy operator; normalize C by recursively applying this procedure.
//
//		3.2 Otherwise the Element Information Item C is an assertion; normalize C per Sections 4.3.1 Optional Policy Assertions and 4.3.2 
//		Policy Assertion Nesting.
//
//		4.Apply the policy operator indicated by E to the normalized Element Information Items in its [children] property and co1.nstruct a 
//		normal form per Section 4.3.3 Policy Operators and 4.1 Normal Form Policy Expression.
//

	
		return policy;
	}
	public static Element normalize(Element policy)
	{
//		if (!StringUtils.isNullOrEmpty(nameSpace) && !StringUtils.isNullOrEmpty(localName))
//		{
		NodeList nl = policy.getChildNodes();
      List<Element> listElms = new ArrayList<Element>();
      for( int c = 0; c < nl.getLength(); c++ )
      {
         Node item = nl.item( c );
         if( item.getParentNode() == policy && item.getNodeType() == Node.ELEMENT_NODE )
            listElms.add( (Element) item );
      }
      
		for (int i = 0; i < listElms.size(); i++)
		{
			Element elm = listElms.get(i);
			Element newElm = null;
			String nameSpace = elm.getNamespaceURI();
			String localName = elm.getLocalName();
			if (nameSpace.equals(WS_POLICY_NAMESPACE)
					&& (localName.equals("Policy") || localName.equals("All") || localName.equals("ExactlyOne")))
			{
					newElm = normalize(elm);

			} else {
				
				Element allElm = elm.getOwnerDocument().createElementNS(WS_POLICY_NAMESPACE, "All");
				allElm.appendChild(elm);

				Element exactlyOneElm = elm.getOwnerDocument().createElementNS(WS_POLICY_NAMESPACE, "ExactlyOne");
				exactlyOneElm.appendChild(allElm);
				
				String optional = elm.getAttributeNS(WS_POLICY_NAMESPACE, "Optional");
				if (!StringUtils.isNullOrEmpty(optional) &&  optional.equals("true"))
				{
					Element allElmEmpty = elm.getOwnerDocument().createElementNS(WS_POLICY_NAMESPACE, "All");
					exactlyOneElm.appendChild(allElmEmpty);
				}
				
				newElm = exactlyOneElm;
			}
			elm.getParentNode().replaceChild(elm, newElm);
		}
//		}

		return policy;
	}
}
