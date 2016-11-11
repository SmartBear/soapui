/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
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

package com.eviware.soapui.impl.wsdl.support.policy;

import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3.x2007.x05.addressing.metadata.AddressingDocument.Addressing;
import org.w3c.dom.Element;
import org.xmlsoap.schemas.ws.x2004.x09.policy.OptionalType;
import org.xmlsoap.schemas.ws.x2004.x09.policy.Policy;
import org.xmlsoap.schemas.ws.x2004.x09.policy.PolicyDocument;

import javax.wsdl.Definition;
import javax.wsdl.extensions.ElementExtensible;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolicyUtils {
    public final static String WS_XMLSOAP_POLICY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public final static String WS_W3_POLICY_NAMESPACE = "http://www.w3.org/ns/ws-policy";
    public final static String WS_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    public static List<Policy> getPolicies(WsdlContext wsdlContext) {

        List<Policy> policies = new ArrayList<Policy>();
        try {
            List<InterfaceDefinitionPart> parts = wsdlContext.getDefinitionCache().getDefinitionParts();
            for (int i = 0; i < parts.size(); i++) {
                InterfaceDefinitionPart part = parts.get(i);
                String content = part.getContent();
                // XmlObject xml = XmlObject.Factory.parse( content );
                XmlObject xml = XmlUtils.createXmlObject(content);
                // include paths for both namespaces
                XmlObject[] paths = xml.selectPath("declare namespace wsp='" + WS_W3_POLICY_NAMESPACE + "';"
                        + "//wsp:Policy");
                List<XmlObject> listOfXmlObjcts = Arrays.asList(paths);

                XmlObject[] paths1 = xml.selectPath("declare namespace wsp='" + WS_XMLSOAP_POLICY_NAMESPACE + "';"
                        + "//wsp:Policy");
                listOfXmlObjcts.addAll(Arrays.asList(paths1));
                paths = (XmlObject[]) listOfXmlObjcts.toArray();

                for (XmlObject obj : paths) {
                    String xx = obj.xmlText(new XmlOptions().setSaveOuter());
                    PolicyDocument policyDocument = PolicyDocument.Factory.parse(xx);
                    org.xmlsoap.schemas.ws.x2004.x09.policy.Policy polc = (org.xmlsoap.schemas.ws.x2004.x09.policy.Policy) policyDocument
                            .getPolicy();
                    policies.add(polc);
                    // List<Addressing> addressingList = polc.getAddressingList();
                    // Addressing a = null;
                    // if (addressingList.size() > 0 )
                    // {
                    // a = addressingList.get(0);
                    // }
                    // AnonymousResponses ar = null;
                    // List<AnonymousResponses> anList =
                    // polc.getAnonymousResponsesList();
                    // if (anList.size() > 0)
                    // {
                    // ar = anList.get(0);
                    // }

                }

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isAddressing(Policy policy) {

        if (policy.getAddressingList().size() > 0) {
            return true;
        }

        return false;
    }

    public static List<Policy> getAddressingPolicies(WsdlContext wsdlContext) {
        List<Policy> addressingPolicies = new ArrayList<Policy>();
        List<Policy> policies = getPolicies(wsdlContext);
        for (Policy policy : policies) {
            if (isAddressing(policy)) {
                addressingPolicies.add(policy);
            }
        }
        return addressingPolicies;
    }

	/*
     * Functions currently not used, initially intended for policy to be
	 * normalized first
	 */
    // public static Policy normalize(Policy policy) {
    // 1.Start with the Element Information Item E (as defined in the XML
    // Information Set [XML Information Set]) of the policy expression.
    // The [namespace name] of E is always "http://www.w3.org/ns/ws-policy". In
    // the base case, the [local name] property of E is "Policy";
    // in the recursive case, the [local name] property of E is "Policy",
    // "ExactlyOne", or "All".
    //
    // 2.Expand Element Information Items (as defined in the XML Information Set
    // [XML Information Set]) in the [children] property of E that
    // are policy references per Section 4.3.5 Policy Inclusion.
    //
    // 3.Convert each Element Information Item C in the [children] property of E
    // into normal form.
    // List<OperatorContentType> eoList = policy.getExactlyOneList();
    // ExactlyOneDocument.Factory.newInstance();
    //
    // 3.1 If the [namespace name] property of C is
    // "http://www.w3.org/ns/ws-policy" and the [local name] property of C is
    // "Policy",
    // "ExactlyOne", or "All", C is an expression of a policy operator; normalize
    // C by recursively applying this procedure.
    //
    // 3.2 Otherwise the Element Information Item C is an assertion; normalize C
    // per Sections 4.3.1 Optional Policy Assertions and 4.3.2
    // Policy Assertion Nesting.
    //
    // 4.Apply the policy operator indicated by E to the normalized Element
    // Information Items in its [children] property and co1.nstruct a
    // normal form per Section 4.3.3 Policy Operators and 4.1 Normal Form Policy
    // Expression.
    //

    // return policy;
    // }
	/*
	 * Functions currently not used, initially intended for policy to be
	 * normalized first
	 */
    // public static Element normalize(Element policy)
    // {
    // // if (!StringUtils.isNullOrEmpty(nameSpace) &&
    // !StringUtils.isNullOrEmpty(localName))
    // // {
    // NodeList nl = policy.getChildNodes();
    // List<Element> listElms = new ArrayList<Element>();
    // for( int c = 0; c < nl.getLength(); c++ )
    // {
    // Node item = nl.item( c );
    // if( item.getParentNode() == policy && item.getNodeType() ==
    // Node.ELEMENT_NODE )
    // listElms.add( (Element) item );
    // }
    //
    // for (int i = 0; i < listElms.size(); i++)
    // {
    // Element elm = listElms.get(i);
    // Element newElm = null;
    // String nameSpace = elm.getNamespaceURI();
    // String localName = elm.getLocalName();
    // if (nameSpace.equals(WS_W3_POLICY_NAMESPACE)
    // && (localName.equals("Policy") || localName.equals("All") ||
    // localName.equals("ExactlyOne")))
    // {
    // newElm = normalize(elm);
    //
    // } else {
    //
    // Element allElm =
    // elm.getOwnerDocument().createElementNS(WS_W3_POLICY_NAMESPACE, "All");
    // allElm.appendChild(elm);
    //
    // Element exactlyOneElm =
    // elm.getOwnerDocument().createElementNS(WS_W3_POLICY_NAMESPACE,
    // "ExactlyOne");
    // exactlyOneElm.appendChild(allElm);
    //
    // String optional = elm.getAttributeNS(WS_W3_POLICY_NAMESPACE, "Optional");
    // if (!StringUtils.isNullOrEmpty(optional) && optional.equals("true"))
    // {
    // Element allElmEmpty =
    // elm.getOwnerDocument().createElementNS(WS_W3_POLICY_NAMESPACE, "All");
    // exactlyOneElm.appendChild(allElmEmpty);
    // }
    //
    // newElm = exactlyOneElm;
    // }
    // elm.getParentNode().replaceChild(elm, newElm);
    // }
    // // }
    //
    // return policy;
    // }
    public static Policy getAttachedPolicy(ElementExtensible item, Definition def) {

        Policy rtnPolicy = null;
        String usedPolicyNamespace = PolicyUtils.WS_W3_POLICY_NAMESPACE;
        Element[] policyReferences = WsdlUtils.getExentsibilityElements(item, new QName(
                PolicyUtils.WS_W3_POLICY_NAMESPACE, "PolicyReference"));
        if (policyReferences.length <= 0) {
            policyReferences = WsdlUtils.getExentsibilityElements(item, new QName(
                    PolicyUtils.WS_XMLSOAP_POLICY_NAMESPACE, "PolicyReference"));
            usedPolicyNamespace = PolicyUtils.WS_XMLSOAP_POLICY_NAMESPACE;
        }
        if (policyReferences.length > 0) {
            String policyId = policyReferences[0].getAttribute("URI");
            if (!StringUtils.isNullOrEmpty(policyId)) {
                Element[] policies = WsdlUtils.getExentsibilityElements(def, new QName(usedPolicyNamespace, "Policy"));
                Element policy = null;
                for (int i = 0; i < policies.length; i++) {
                    policy = policies[i];
                    String policyIdx = policy.getAttributeNS(WS_SECURITY_NAMESPACE, "Id");
                    if (policyId.equals("#" + policyIdx)) {
                        rtnPolicy = getPolicy(policy, usedPolicyNamespace);
                        continue;
                    }

                }
            }
        } else {
            // get policies of item itself
            Element[] itemPolicies = WsdlUtils.getExentsibilityElements(item, new QName(usedPolicyNamespace, "Policy"));
            if (itemPolicies.length > 0) {
                for (int i = 0; i < itemPolicies.length; i++) {
                    Element policy = itemPolicies[i];
                    rtnPolicy = getPolicy(policy, usedPolicyNamespace);

                }
            }
        }
        return rtnPolicy;
    }

    public static Policy getPolicy(Element policy, String usedPolicyNamespace) {
        // policy = PolicyUtils.normalize(policy);

        Policy newPolicy = null;
        // check for ExactlyOne and All
        // TODO ExactlyOne and All are idempotent all empty ones should be skipped
        // and found the real ones
        Element exactlyOne = XmlUtils.getFirstChildElementNS(policy, usedPolicyNamespace, "ExactlyOne");
        if (exactlyOne != null) {
            Element all = XmlUtils.getFirstChildElementNS(exactlyOne, usedPolicyNamespace, "All");
            if (all != null) {
                newPolicy = getAddressingPolicy(all, usedPolicyNamespace);
            }
        } else {
            newPolicy = getAddressingPolicy(policy, usedPolicyNamespace);
        }
        return newPolicy;
    }

    private static Policy getAddressingPolicy(Element wsamAddressingElm, String usedPolicyNamespace) {
        // check if found reference is addressing policy
        Element wsAddressing = XmlUtils.getFirstChildElementNS(wsamAddressingElm, WsaUtils.WS_A_NAMESPACE_200705,
                "Addressing");
        Element addressingPolicy = null;
        Policy newPolicy = PolicyDocument.Factory.newInstance().addNewPolicy();
        Addressing newAddressing = null;
        if (wsAddressing != null) {
            newAddressing = newPolicy.addNewAddressing();
            String optional = wsAddressing.getAttributeNS(usedPolicyNamespace, "Optional");
            if (!StringUtils.isNullOrEmpty(optional) && optional.equals(OptionalType.TRUE.toString())) {
                newAddressing.setOptional(OptionalType.TRUE);
            } else {
                newAddressing.setOptional(OptionalType.FALSE);
            }
            addressingPolicy = XmlUtils.getFirstChildElementNS(wsAddressing, usedPolicyNamespace, "Policy");
            if (addressingPolicy != null) {
                Element exactlyOne = XmlUtils.getFirstChildElementNS(addressingPolicy, usedPolicyNamespace, "ExactlyOne");
                if (exactlyOne != null) {
                    Element all = XmlUtils.getFirstChildElementNS(exactlyOne, usedPolicyNamespace, "All");
                    if (all != null) {
                        getAddressingAnonymous(all, newAddressing);
                    }
                } else {
                    getAddressingAnonymous(addressingPolicy, newAddressing);
                }

            }
        }
        Element usingAddressing = XmlUtils.getFirstChildElementNS(wsamAddressingElm, WsaUtils.WS_A_NAMESPACE_200605,
                "UsingAddressing");
        if (usingAddressing != null) {
            // add UsingAddressing to policy
            newPolicy.addNewUsingAddressing();
        }
        return newPolicy;
    }

    private static void getAddressingAnonymous(Element addressingPolicy, Addressing newAddressing) {
        Policy innerPolicy = newAddressing.addNewPolicy();
        // check if policy has Anonymous
        Element anonymousElm = XmlUtils.getFirstChildElementNS(addressingPolicy, new QName(
                WsaUtils.WS_A_NAMESPACE_200705, "AnonymousResponses"));
        if (anonymousElm != null) {
            innerPolicy.addNewAnonymousResponses();
        } else {
            Element nonAnonymousElement = XmlUtils.getFirstChildElementNS(addressingPolicy, new QName(
                    WsaUtils.WS_A_NAMESPACE_200705, "NonAnonymousResponses"));
            if (nonAnonymousElement != null) {
                innerPolicy.addNewNonAnonymousResponses();
            }
        }
    }
}
