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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.model.iface.SubmitContext;

/**
 * RequestFilter that expands scripts in request content - not used for now, we
 * need to fix validations first
 *
 * @author Ole.Matzura
 */

public class ScriptExpansionRequestFilter extends AbstractRequestFilter {
    public void filterRequest(SubmitContext context, AbstractHttpRequestInterface<?> wsdlRequest) {
        /*
		 * String content = (String) context.getProperty(
		 * BaseHttpRequestTransport.REQUEST_CONTENT );
		 * 
		 * content = expandScripts(context, content); if( content != null )
		 * context.setProperty( BaseHttpRequestTransport.REQUEST_CONTENT, content
		 * );
		 */
    }

    public static String expandScripts(SubmitContext context, String content) {
        return content;

		/*
		 * try { XmlObject obj = XmlObject.Factory.parse(content); XmlCursor
		 * cursor = obj.newCursor(); boolean replaced = false;
		 * 
		 * while (!cursor.isEnddoc()) { Node node = cursor.getDomNode(); if (
		 * node.getNodeType() == Node.ELEMENT_NODE) { if(
		 * node.getNamespaceURI().equals( "http://www.soapui.org/wsp" ) &&
		 * node.getNodeName().equals( "script")) { GroovyShell shell =
		 * ScriptingSupport.createGroovyShell( null ); String type =
		 * ((Element)node).getAttribute( "type" ); String result = shell.evaluate(
		 * cursor.getTextValue() ).toString();
		 * 
		 * if( type == null || type.length() == 0 || type.equals( "content")) {
		 * cursor.removeXml(); cursor.insertChars( result ); } else if(
		 * type.equals( "markup" )) { Node parent = node.getParentNode();
		 * XmlOptions options = new XmlOptions(); Map map = new HashMap();
		 * cursor.getAllNamespaces( map );
		 * 
		 * StringBuffer buf = new StringBuffer(); buf.append( "<result" );
		 * 
		 * for( Iterator i = map.keySet().iterator(); i.hasNext(); ) { buf.append(
		 * " xmlns" ); String next = (String) i.next(); if( next.length() > 0 )
		 * buf.append( ':' ).append( next);
		 * 
		 * buf.append( "=\"" ).append( map.get( next )).append( "\"" ); }
		 * 
		 * buf.append( ">" ).append( result ).append( "</result>" ); result =
		 * buf.toString();
		 * 
		 * XmlObject newObj = XmlObject.Factory.parse( result ); Element docElm =
		 * ((Document)newObj.getDomNode()).getDocumentElement();
		 * 
		 * parent.replaceChild( parent.getOwnerDocument().importNode(
		 * docElm.getFirstChild(), true ), node ); }
		 * 
		 * replaced = true; } }
		 * 
		 * cursor.toNextToken(); }
		 * 
		 * return replaced ? obj.toString() : null; } catch (Exception e) {
		 * UISupport.logError( e ); return null; }
		 */
    }
}
