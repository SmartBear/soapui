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
import java.util.TreeMap;

import javax.swing.tree.TreePath;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
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
	private TreeMap<String, NodeInfo> variableSet;

	public SchemeTypeExtractor( WsdlTestRequest request )
	{
		this.request = request;
	}

	public SchemeTypeExtractor( TestStep testStep )
	{
		if( testStep instanceof WsdlTestRequestStep )
		{
			request = ( ( WsdlTestRequestStep )testStep ).getHttpRequest();
		}

	}

	public String getTypeFor( String name ) throws Exception
	{
		NodeInfo node = variableSet.get( name );
		if( node == null )
			return null;
		Definition definition = request.getOperation().getInterface().getDefinitionContext().getDefinition();
		for( Object key : definition.getNamespaces().keySet() )
		{
			node.node.getSchemaType();
			
			node.node.getSchemaType().getTypeSystem();
			
			return node.node.getSchemaType().getShortJavaName();
		}
		return null;
	}

	public TreeMap<String, NodeInfo> getParams()
	{
		return variableSet;
	}

	public XmlTreeNode[] extract() throws XmlException, Exception
	{
		XmlObjectTreeModel model = new XmlObjectTreeModel( request.getOperation().getInterface().getDefinitionContext()
				.getSchemaTypeSystem(), XmlObject.Factory.parse( request.getRequestContent() ) );

		 TreeMap<String, NodeInfo> nodeList = getElements( model.getRootNode() );

		WsdlContext wsdl = request.getOperation().getInterface().getDefinitionContext();

		String operationName = request.getOperation().getBindingOperation().getName();

		variableSet = new TreeMap<String, NodeInfo>();
		Definition definition = wsdl.getDefinition();
		for( Object key : definition.getAllBindings().keySet() )
		{
			Binding binding = ( Binding )definition.getAllBindings().get( key );
			for( Object operation : binding.getPortType().getOperations() )
			{
				if( ( ( Operation )operation ).getName().equals( operationName ) )
				{
					QName messageName = ( ( Operation )operation ).getInput().getMessage().getQName();
					Message message = definition.getMessage( messageName );
					for( Object part : message.getParts().values() )
					{
						if( !( ( Part )part ).getName().equals( "parameters" ) )
						{
							variableSet.put( ( ( Part )part ).getName(), nodeList.get( ( ( Part )part ).getName() ));
						}
					}

				}
			}
		}
		// select desired node(s)
		// ArrayList<XmlTreeNode> nodes = getElements( model.getRootNode() );

		// for( XmlTreeNode node : nodes ) {
		//			
		// node.getNodeName();
		// getNextChild( node );
		// }

		return null;
	}

	public TreeMap<String, NodeInfo> getVariableSet()
	{
		return variableSet;
	}

	TreeMap<String, NodeInfo> getElements( XmlTreeNode rootXmlTreeNode )
	{
		TreeMap<String, NodeInfo> result = new TreeMap<String, NodeInfo>();
		for( int cnt = 0; cnt < rootXmlTreeNode.getChildCount(); cnt++ )
		{
			if( ( ( XmlTreeNode )rootXmlTreeNode.getChild( cnt ) ).getChildCount() > 0 )
				result.putAll( getElements( rootXmlTreeNode.getChild( cnt ) ) );
			else
				result.put( ( ( XmlTreeNode )rootXmlTreeNode.getChild( cnt ) ).getDomNode().getLocalName(), new NodeInfo(rootXmlTreeNode
						.getChild( cnt )) );
		}
		return result;
	}

	private String declareXPathNamespaces( Definition definition )
	{
		StringBuilder result = new StringBuilder();
		for( Object shortName : definition.getNamespaces().keySet() )
		{
			result.append( "declare namespace " ).append( shortName.toString() ).append( "=\'" ).append(
					definition.getNamespaces().get( shortName ).toString() ).append( "\';" );
		}
		return result.toString();
	}

	private void getNextChild( XmlTreeNode node )
	{
		for( int i = 0; i < node.getChildCount(); i++ )
		{
			XmlTreeNode mynode = node.getChild( i );

			System.out.println( mynode.getNodeName() );
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

	public class NodeInfo
	{

		private String name;
		private String text;
		private TreePath treePath;
		private XmlTreeNode node;
		private String xpath;
		private SchemaType type;
		private boolean selected = false;

		public boolean isSelected()
		{
			return selected;
		}

		public void setSelected( boolean selected )
		{
			this.selected = selected;
		}

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

		public NodeInfo( XmlTreeNode child )
		{
			this.name = child.getNodeName();
			this.text = child.getNodeText();
			this.treePath = child.getTreePath();
			this.type = child.getSchemaType();
			this.node = child;
			this.xpath = XmlUtils.createXPath( child.getDomNode() );
			this.type = child.getSchemaType();
		}

		public SchemaType getType()
		{
			return type;
		}

		public String getSimpleName()
		{
			return node.getDomNode().getLocalName();
		}
	}

}
