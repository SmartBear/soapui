/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.boundary;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreePath;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.xml.XmlObjectTreeModel;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.support.xml.XmlObjectTreeModel.XmlTreeNode;

public class SchemeTypeExtractor
{
	
	private WsdlRequest request;
	private List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();

	public SchemeTypeExtractor( WsdlTestRequest request )
	{
		this.request = request;
	}

	public SchemeTypeExtractor( TestStep testStep )
	{
		if ( testStep instanceof WsdlTestRequestStep ) {
			request = ((WsdlTestRequestStep)testStep).getHttpRequest();
		}
	}

	public void extract() throws XmlException, Exception
	{
		XmlObjectTreeModel model = new XmlObjectTreeModel( request.getOperation().getInterface().getDefinitionContext()
				.getSchemaTypeSystem(), XmlObject.Factory.parse( request.getOperation().getInterface()
				.getDefinitionContext().getDefinitionCache().getDefinitionParts().get( 0 ).getContent() ) );

		// select desired node(s)
		XmlTreeNode[] nodes = model
				.selectTreeNodes( "declare namespace xsd=\'http://www.w3.org/2001/XMLSchema\'; declare namespace wsdl=\'http://schemas.xmlsoap.org/wsdl/\'; //wsdl:definitions[1]/wsdl:types[1]/xsd:schema[1]" );
	

		
		
		SchemaType schemaType = null;
	}

	private void getNextChild( XmlTreeNode node )
	{
		for( int i = 0; i < node.getChildCount(); i++ )
		{
			XmlTreeNode mynode = node.getChild( i );

			if( "@type".equals( mynode.getNodeName() ) )
			{
				String xpath = XmlUtils.createXPath( mynode.getDomNode() );
				NodeInfo nodeInfo = new NodeInfo( mynode.getNodeName(), mynode.getNodeText(), mynode.getTreePath(), mynode,
						xpath );

				System.out.println( nodeInfo.getName() );
				System.out.println( nodeInfo.getText() );
				System.out.println( nodeInfo.getTreePath() );
				System.out.println( nodeInfo.getXPath() );
				nodeInfoList.add( nodeInfo );
			}
			getNextChild( mynode );
		}
	}

	class NodeInfo
	{

		private String name;
		private String text;
		private TreePath treePath;
		private XmlTreeNode node;
		private String xpath;

		public String getName()
		{
			return name;
		}

		public String getText()
		{
			return text;
		}

		public TreePath getTreePath()
		{
			return treePath;
		}

		public String getXPath()
		{
			return xpath;

		}

		public NodeInfo( String name, String text, TreePath treePath, XmlTreeNode node, String xpath )
		{
			this.name = name;
			this.text = text;
			this.treePath = treePath;
			this.node = node;
			this.xpath = xpath;

		}
	}
}
