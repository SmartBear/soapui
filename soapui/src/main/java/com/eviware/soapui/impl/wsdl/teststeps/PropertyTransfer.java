/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eviware.soapui.config.PropertyTransferConfig;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.PropertyChangeNotifier;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.resolver.ChooseAnotherPropertySourceResolver;
import com.eviware.soapui.support.resolver.ChooseAnotherPropertyTargetResolver;
import com.eviware.soapui.support.resolver.CreateMissingPropertyResolver;
import com.eviware.soapui.support.resolver.DisablePropertyTransferResolver;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.ResolveContext.PathToResolve;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Class for transferring a property value between 2 test steps. This class is
 * relatively complex due to backwards compatibility issues and to gracefull
 * handling of references test steps and properties.
 *
 * @author Ole.Matzura
 */

public class PropertyTransfer implements PropertyChangeNotifier
{
	private final static Logger log = Logger.getLogger( PropertyTransfer.class );

	public final static String SOURCE_PATH_PROPERTY = PropertyTransfer.class.getName() + "@sourcePath";
	public final static String SOURCE_TYPE_PROPERTY = PropertyTransfer.class.getName() + "@sourceProperty";
	public final static String SOURCE_STEP_PROPERTY = PropertyTransfer.class.getName() + "@sourceStep";
	public final static String TARGET_PATH_PROPERTY = PropertyTransfer.class.getName() + "@targetPath";
	public final static String TARGET_TYPE_PROPERTY = PropertyTransfer.class.getName() + "@targetProperty";
	public final static String TARGET_STEP_PROPERTY = PropertyTransfer.class.getName() + "@targetStep";
	public final static String NAME_PROPERTY = PropertyTransfer.class.getName() + "@name";
	public final static String DISABLED_PROPERTY = PropertyTransfer.class.getName() + "@disabled";
	public final static String CONFIG_PROPERTY = PropertyTransfer.class.getName() + "@config";

	private TestStep testStep;

	// create local copies since a deleted/changed valuetransfer can be referred
	// to from a result
	private PropertyTransferConfig config;
	private String sourcePath;
	private String sourceType;
	private String targetPath;
	private String name;
	private String targetType;
	private String sourceStep;
	private String targetStep;

	private TestPropertyHolder currentTargetStep;
	private TestPropertyHolder currentSourceStep;
	private TestProperty currentTargetProperty;
	private TestProperty currentSourceProperty;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );
	private StepNameChangeListener stepNameChangeListener = new StepNameChangeListener();
	private InternalTestPropertyListener propertyNameChangeListener = new InternalTestPropertyListener();
	private TestCase testCase;

	private InternalTestSuiteListener testSuiteListener = new InternalTestSuiteListener();

	public PropertyTransfer( TestStep testStep )
	{
		this( testStep, PropertyTransferConfig.Factory.newInstance() );
	}

	public PropertyTransfer( TestStep testStep, PropertyTransferConfig config )
	{
		this.testStep = testStep;

		if( testStep != null )
		{
			this.testCase = testStep.getTestCase();
			testCase.getTestSuite().addTestSuiteListener( testSuiteListener );
		}

		setConfig( config );
	}

	void setConfigOnMove( PropertyTransferConfig config )
	{
		this.config = config;
	}

	void setConfig( PropertyTransferConfig config )
	{
		releaseListeners();

		this.config = config;

		if( !config.isSetSetNullOnMissingSource() )
		{
			config.setSetNullOnMissingSource( true );
		}

		if( !config.isSetTransferTextContent() )
		{
			config.setTransferTextContent( true );
		}

		sourceStep = config.getSourceStep();
		if( sourceStep == null )
		{
			sourceStep = getSourceStepName();
			if( sourceStep != null )
				config.setSourceStep( sourceStep );
		}
		else
			sourceStep = sourceStep.trim();

		currentSourceStep = getPropertyHolder( sourceStep );

		sourceType = config.getSourceType();
		currentSourceProperty = currentSourceStep == null || sourceType == null ? null : currentSourceStep
				.getProperty( sourceType );

		sourcePath = config.getSourcePath();

		targetStep = config.getTargetStep();
		if( targetStep == null )
		{
			targetStep = getTargetStepName();
			if( targetStep != null )
				config.setTargetStep( targetStep );
		}
		else
			targetStep = targetStep.trim();

		currentTargetStep = getPropertyHolder( targetStep );

		targetType = config.getTargetType();
		currentTargetProperty = currentTargetStep == null || targetType == null ? null : currentTargetStep
				.getProperty( targetType );

		targetPath = config.getTargetPath();

		name = config.getName();
		initListeners();

		propertyChangeSupport.firePropertyChange( CONFIG_PROPERTY, null, null );
	}

	private void initListeners()
	{
		if( currentSourceStep != null )
		{
			if( currentSourceStep instanceof TestStep )
				( ( TestStep )currentSourceStep )
						.addPropertyChangeListener( TestStep.NAME_PROPERTY, stepNameChangeListener );

			currentSourceStep.addTestPropertyListener( propertyNameChangeListener );
		}

		if( currentTargetStep != null )
		{
			if( currentTargetStep instanceof TestStep )
				( ( TestStep )currentTargetStep )
						.addPropertyChangeListener( TestStep.NAME_PROPERTY, stepNameChangeListener );

			currentTargetStep.addTestPropertyListener( propertyNameChangeListener );
		}
	}

	public void releaseListeners()
	{
		if( currentSourceStep != null )
		{
			if( currentSourceStep instanceof TestStep )
				( ( TestStep )currentSourceStep ).removePropertyChangeListener( TestStep.NAME_PROPERTY,
						stepNameChangeListener );

			currentSourceStep.removeTestPropertyListener( propertyNameChangeListener );
		}

		if( currentTargetStep != null )
		{
			if( currentTargetStep instanceof TestStep )
				( ( TestStep )currentTargetStep ).removePropertyChangeListener( TestStep.NAME_PROPERTY,
						stepNameChangeListener );

			currentTargetStep.removeTestPropertyListener( propertyNameChangeListener );
		}

		PropertyChangeListener[] listeners = propertyChangeSupport.getPropertyChangeListeners();
		for( PropertyChangeListener listener : listeners )
			propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public void release()
	{
		releaseListeners();
		testCase.getTestSuite().removeTestSuiteListener( testSuiteListener );
	}

	public PropertyTransferConfig getConfig()
	{
		return config;
	}

	public String getSourcePath()
	{
		return sourcePath;
	}

	public String getTargetPath()
	{
		return targetPath;
	}

	public TestProperty getSourceProperty()
	{
		if( sourceType == null )
			return null;

		if( currentSourceProperty != null )
			return currentSourceProperty;

		TestPropertyHolder actualSourceStep = getSourceStep();
		return actualSourceStep == null ? null : actualSourceStep.getProperty( sourceType );
	}

	public String[] transferProperties( SubmitContext context ) throws PropertyTransferException
	{
		TestProperty sourceProperty = getSourceProperty();
		TestProperty targetProperty = getTargetProperty();

		try
		{
			if( sourceProperty == null )
				throw new Exception( "Missing source property" );
			if( targetProperty == null )
				throw new Exception( "Missing target property" );
			if( sourceProperty.getValue() == null && !getSetNullOnMissingSource() && !getIgnoreEmpty() )
				throw new Exception( "Source property is null" );

			if( !hasSourcePath() && !hasTargetPath() )
			{
				if( !getIgnoreEmpty() || ( sourceProperty.getValue() != null && sourceProperty.getValue().length() > 0 ) )
					return transferStringToString( sourceProperty, targetProperty );
			}
			else if( hasSourcePath() && hasTargetPath() )
			{
				return transferXPathToXml( sourceProperty, targetProperty, context );
			}
			else if( hasSourcePath() && !hasTargetPath() )
			{
				return new String[] { transferXPathToString( sourceProperty, targetProperty, context ) };
			}
			else if( !hasSourcePath() && hasTargetPath() )
			{
				if( !getIgnoreEmpty() || ( sourceProperty.getValue() != null && sourceProperty.getValue().length() > 0 ) )
					return transferStringToXml( sourceProperty, targetProperty, context );
			}
		}
		catch( Exception e )
		{
			throw new PropertyTransferException( e.getMessage(), getSourceStepName(), sourceProperty, getTargetStepName(),
					targetProperty );
		}

		return new String[0];
	}

	private boolean hasTargetPath()
	{
		String path = getTargetPath();
		return path != null && path.trim().length() > 0;
	}

	private boolean hasSourcePath()
	{
		String path = getSourcePath();
		return path != null && path.trim().length() > 0;
	}

	protected String[] transferStringToString( TestProperty sourceProperty, TestProperty targetProperty )
	{
		String value = sourceProperty.getValue();

		if( StringUtils.hasContent( value ) && getEntitize() )
			value = XmlUtils.entitize( value );

		targetProperty.setValue( value );
		return new String[] { value };
	}

	protected String[] transferXPathToXml( TestProperty sourceProperty, TestProperty targetProperty,
														SubmitContext context ) throws Exception
	{
		XmlCursor sourceXml;
		try
		{
			String sourcePropertyValue = sourceProperty.getValue();
			// XmlObject sourceXmlObject = sourcePropertyValue == null ? null :
			// XmlObject.Factory.parse( sourcePropertyValue );
			XmlObject sourceXmlObject = sourcePropertyValue == null ? null : XmlUtils
					.createXmlObject( sourcePropertyValue );
			sourceXml = sourceXmlObject == null ? null : sourceXmlObject.newCursor();
		}
		catch( XmlException e )
		{
			throw new Exception( "Error parsing source property [" + e.getMessage() + "]" );
		}

		XmlObject targetXmlObject;
		XmlCursor targetXml;
		try
		{
			String targetPropertyValue = targetProperty.getValue();
			// targetXmlObject = XmlObject.Factory.parse( targetPropertyValue );
			targetXmlObject = XmlUtils.createXmlObject( targetPropertyValue );
			targetXml = targetXmlObject.newCursor();
		}
		catch( XmlException e )
		{
			throw new Exception( "Error parsing target property [" + e.getMessage() + "]" );
		}

		XmlCursor lastSource = null;

		try
		{
			List<String> result = new ArrayList<String>();

			String tp = PropertyExpander.expandProperties( context, getTargetPath() );
			targetXml.selectPath( tp );

			if( !targetXml.hasNextSelection() )
				throw new Exception( "Missing match for Target XPath [" + tp + "]" );

			if( sourceXml == null )
			{
				if( getSetNullOnMissingSource() )
				{
					while( targetXml.toNextSelection() )
					{
						result.add( setNodeValue( null, targetXml.getDomNode() ) );
						if( !getTransferToAll() )
							break;
					}
				}
			}
			else if( getUseXQuery() )
			{
				String sp = PropertyExpander.expandProperties( context, getSourcePath() );
				XmlCursor resultCursor = sourceXml.execQuery( sp );
				sourceXml.dispose();
				sourceXml = resultCursor;

				if( sourceXml.toNextToken() != TokenType.START )
				{
					if( getSetNullOnMissingSource() )
					{
						while( targetXml.toNextSelection() )
						{
							result.add( setNodeValue( null, targetXml.getDomNode() ) );
							if( !getTransferToAll() )
								break;
						}
					}
					else if( !getIgnoreEmpty() )
						throw new Exception( "Missing match for Source XQuery [" + sp + "]" );
				}

				boolean hasTarget = targetXml.toNextSelection();

				if( hasTarget )
				{
					lastSource = sourceXml.newCursor();
					result.add( transferXmlValue( sourceXml, targetXml ) );
				}
			}
			else
			{
				String sp = PropertyExpander.expandProperties( context, getSourcePath() );
				sourceXml.selectPath( sp );

				if( !sourceXml.hasNextSelection() )
				{
					if( getSetNullOnMissingSource() )
					{
						while( targetXml.toNextSelection() )
						{
							result.add( setNodeValue( null, targetXml.getDomNode() ) );
							if( !getTransferToAll() )
								break;
						}
					}
					else if( !getIgnoreEmpty() )
						throw new Exception( "Missing match for Source XPath [" + sp + "]" );
				}
				else
				{
					boolean hasSource = sourceXml.toNextSelection();
					boolean hasTarget = targetXml.toNextSelection();

					while( hasSource && hasTarget )
					{
						if( lastSource != null )
							lastSource.dispose();

						lastSource = sourceXml.newCursor();
						result.add( transferXmlValue( sourceXml, targetXml ) );

						hasSource = sourceXml.toNextSelection();
						hasTarget = targetXml.toNextSelection();
					}

					if( getTransferToAll() && !hasSource && hasTarget && lastSource != null )
					{
						while( hasTarget )
						{
							result.add( transferXmlValue( lastSource, targetXml ) );
							hasTarget = targetXml.toNextSelection();
						}
					}
				}
			}

			if( result.size() > 0 )
			{
				String value = targetXmlObject.xmlText( new XmlOptions().setSaveAggressiveNamespaces() );
				// if( getEntitize() )
				// value = XmlUtils.entitize( value );

				targetProperty.setValue( value );
			}

			return result.toArray( new String[result.size()] );
		} finally
		{
			if( sourceXml != null )
			{
				sourceXml.dispose();
			}
			if( targetXml != null )
			{
				targetXml.dispose();
			}
			if( lastSource != null )
			{
				lastSource.dispose();
			}
		}
	}

	protected String[] transferStringToXml( TestProperty sourceProperty, TestProperty targetProperty,
														 SubmitContext context ) throws XmlException, Exception
	{
		if( !StringUtils.hasContent( targetProperty.getValue() ) )
			throw new Exception( "Missing target property value" );

		XmlObject targetXml = XmlObject.Factory.parse( targetProperty.getValue() );
		XmlCursor targetCursor = targetXml.newCursor();

		try
		{
			List<String> result = new ArrayList<String>();

			String tp = PropertyExpander.expandProperties( context, getTargetPath() );
			targetCursor.selectPath( tp );

			if( !targetCursor.toNextSelection() )
				throw new Exception( "Missing match for Target XPath [" + tp + "]" );

			String value = sourceProperty.getValue();

			Node targetNode = targetCursor.getDomNode();
			setNodeValue( value, targetNode );

			result.add( value );

			if( getTransferToAll() )
			{
				while( targetCursor.toNextSelection() )
				{
					targetNode = targetCursor.getDomNode();
					setNodeValue( value, targetNode );

					result.add( value );
				}
			}

			targetProperty.setValue( targetXml.xmlText( new XmlOptions().setSaveAggressiveNamespaces() ) );

			return result.toArray( new String[result.size()] );
		} finally
		{
			targetCursor.dispose();
		}
	}

	private String setNodeValue( String value, Node node ) throws Exception
	{
		short targetNodeType = node.getNodeType();

		if( targetNodeType == Node.DOCUMENT_FRAGMENT_NODE )
		{
			node = node.getFirstChild();
			if( node != null )
				targetNodeType = node.getNodeType();
			else
				throw new Exception( "Missing source value for " + getSourcePropertyName() );
		}

		if( !XmlUtils.setNodeValue( node, value ) )
		// if( targetNodeType == Node.TEXT_NODE || targetNodeType ==
		// Node.ATTRIBUTE_NODE )
		// {
		// node.setNodeValue( value );
		// }
		// else if( targetNodeType == Node.ELEMENT_NODE )
		// {
		// XmlUtils.setElementText( (Element) node, value );
		// }
		// else
		{
			throw new Exception( "Failed to set value to node [" + node.toString() + "] of type [" + targetNodeType + "]" );
		}

		return value;
	}

	protected String transferXPathToString( TestProperty sourceProperty, TestProperty targetProperty,
														 SubmitContext context ) throws Exception
	{
		String sourceValue = sourceProperty.getValue();

		if( !StringUtils.hasContent( sourceValue ) )
		{
			if( !getIgnoreEmpty() )
				throw new Exception( "Missing source value" );

			if( getSetNullOnMissingSource() )
				targetProperty.setValue( null );

			return null;
		}

		// XmlObject sourceXml = sourceValue == null ? null :
		// XmlObject.Factory.parse( sourceValue );
		XmlObject sourceXml = sourceValue == null ? null : XmlUtils.createXmlObject( sourceValue );
		XmlCursor sourceCursor = sourceValue == null ? null : sourceXml.newCursor();

		try
		{
			String value = null;

			String xquery = PropertyExpander.expandProperties( context, getSourcePath() );
			if( getUseXQuery() )
			{
				XmlCursor resultCursor = sourceCursor.execQuery( xquery );
				sourceCursor.dispose();
				sourceCursor = resultCursor;
			}
			else
			{
				sourceCursor.selectPath( xquery );
			}

			if( !getUseXQuery() && !sourceCursor.toNextSelection() )
			{
				if( !getSetNullOnMissingSource() && !getIgnoreEmpty() )
					throw new Exception( "Missing match for Source XPath [" + xquery + "]" );
			}
			else if( getUseXQuery() && sourceCursor.toNextToken() != TokenType.START )
			{
				if( !getSetNullOnMissingSource() && !getIgnoreEmpty() )
					throw new Exception( "Missing match for Source XQuery [" + xquery + "]" );
			}
			else if( sourceCursor != null )
			{
				Node sourceNode = sourceCursor.getDomNode();
				short sourceNodeType = sourceNode.getNodeType();

				if( sourceNodeType == Node.DOCUMENT_FRAGMENT_NODE )
				{
					sourceNode = sourceNode.getFirstChild();
					if( sourceNode != null )
						sourceNodeType = sourceNode.getNodeType();
					else
						throw new Exception( "Missing source value for " + getSourcePropertyName() );
				}

				if( sourceNodeType == Node.TEXT_NODE || sourceNodeType == Node.ATTRIBUTE_NODE )
				{
					value = sourceNode.getNodeValue();
				}
				else if( sourceNodeType == Node.ELEMENT_NODE )
				{
					if( getTransferTextContent() )
					{
						value = XmlUtils.getElementText( ( Element )sourceNode );
					}

					if( value == null || !getTransferTextContent() )
					{
						value = sourceCursor.getObject().xmlText(
								new XmlOptions().setSaveOuter().setSaveAggressiveNamespaces() );
					}
				}
			}

			if( !getIgnoreEmpty() || ( value != null && value.length() > 0 )
					|| ( getSetNullOnMissingSource() && !StringUtils.hasContent( value ) ) )
			{
				if( StringUtils.hasContent( value ) && getEntitize() )
					value = XmlUtils.entitize( value );

				targetProperty.setValue( value );
			}
			else
				value = "";

			return value;
		} finally
		{
			if( sourceCursor != null )
			{
				sourceCursor.dispose();
			}
		}
	}

	/**
	 * Method called for transferring between 2 xml properties..
	 */

	private String transferXmlValue( XmlCursor source, XmlCursor dest ) throws Exception
	{
		// just copy if nodes are of same type
		Node destNode = dest.getDomNode();
		Node sourceNode = source.getDomNode();
		short destNodeType = destNode.getNodeType();
		short sourceNodeType = sourceNode.getNodeType();
		String value = null;

		if( getTransferChildNodes() )
		{
			while( destNode.hasChildNodes() )
			{
				destNode.removeChild( destNode.getFirstChild() );
			}

			NodeList childNodes = sourceNode.getChildNodes();
			for( int c = 0; c < childNodes.getLength(); c++ )
			{
				destNode.appendChild( destNode.getOwnerDocument().importNode( childNodes.item( c ), true ) );
			}

			return XmlUtils.serialize( destNode, false );
		}

		if( sourceNodeType == Node.DOCUMENT_FRAGMENT_NODE )
		{
			sourceNode = sourceNode.getFirstChild();
			if( sourceNode != null )
				sourceNodeType = sourceNode.getNodeType();
			else
				throw new Exception( "Missing source value for " + source );
		}

		// same type of node?
		if( destNodeType == sourceNodeType )
		{
			if( destNodeType == Node.TEXT_NODE || destNodeType == Node.ATTRIBUTE_NODE )
			{
				value = sourceNode.getNodeValue();
				if( !getIgnoreEmpty() || ( value != null && value.length() > 0 ) )
				{
					if( getEntitize() )
						value = XmlUtils.entitize( value );

					destNode.setNodeValue( value );
				}
			}
			else if( config.getTransferTextContent() && destNodeType == Node.ELEMENT_NODE )
			{
				value = XmlUtils.getElementText( ( Element )sourceNode );
				if( value == null && sourceNode.getFirstChild() != null )
				{
					value = source.getObject().xmlText( new XmlOptions().setSaveOuter().setSaveAggressiveNamespaces() );

					if( getEntitize() )
						value = XmlUtils.entitize( value );

					destNode.getParentNode().replaceChild( destNode.getOwnerDocument().importNode( sourceNode, true ),
							destNode );
				}
				else if( !getIgnoreEmpty() || ( value != null && value.length() > 0 ) )
				{
					if( getEntitize() )
						value = XmlUtils.entitize( value );

					XmlUtils.setElementText( ( Element )destNode, value );
				}
			}
			else
			{
				destNode = destNode.getParentNode().replaceChild(
						destNode.getOwnerDocument().importNode( sourceNode, true ), destNode );

				value = dest.xmlText();
			}
		}
		// text to attribute?
		else if( ( sourceNodeType == Node.TEXT_NODE && destNodeType == Node.ATTRIBUTE_NODE )
				|| ( sourceNodeType == Node.ATTRIBUTE_NODE && destNodeType == Node.TEXT_NODE ) )
		{
			value = sourceNode.getNodeValue();
			if( !getIgnoreEmpty() || ( value != null && value.length() > 0 ) )
			{
				if( getEntitize() )
					value = XmlUtils.entitize( value );

				destNode.setNodeValue( value );
			}
		}
		else if( sourceNodeType == Node.ELEMENT_NODE && destNodeType == Node.ATTRIBUTE_NODE
				|| destNodeType == Node.TEXT_NODE )
		{
			value = XmlUtils.getElementText( ( Element )sourceNode );
			if( !getIgnoreEmpty() || ( value != null && value.length() > 0 ) )
			{
				if( getEntitize() )
					value = XmlUtils.entitize( value );

				destNode.setNodeValue( value );
			}
		}
		else if( destNodeType == Node.ELEMENT_NODE && sourceNodeType == Node.ATTRIBUTE_NODE
				|| sourceNodeType == Node.TEXT_NODE )
		{
			// hmm.. not sure xmlbeans handles this ok
			value = sourceNode.getNodeValue();
			if( !getIgnoreEmpty() || ( value != null && value.length() > 0 ) )
			{
				if( getEntitize() )
					value = XmlUtils.entitize( value );

				XmlUtils.setElementText( ( Element )destNode, value );
			}
		}

		return value;
	}

	/**
	 * Returns the name of the source property.
	 */

	public String getSourcePropertyName()
	{
		if( sourceType == null )
			return null;

		if( currentSourceProperty != null )
			return currentSourceProperty.getName();

		TestPropertyHolder actualSourceStep = getSourceStep();
		if( actualSourceStep == null )
			return sourceType;

		TestProperty property = actualSourceStep.getProperty( sourceType );
		return property == null ? sourceType : property.getName();
	}

	public void setSourcePropertyName( String name )
	{
		String old = getSourcePropertyName();

		// check for change
		if( ( name == null && old == null ) || ( name != null && old != null && name.equals( old ) ) )
			return;

		// update
		sourceType = name;
		config.setSourceType( name );

		// update actual property
		TestPropertyHolder sourceStep2 = getSourceStep();
		currentSourceProperty = sourceStep2 != null && sourceType != null ? sourceStep2.getProperty( sourceType ) : null;

		// notify!
		propertyChangeSupport.firePropertyChange( SOURCE_TYPE_PROPERTY, old, name );
	}

	public TestProperty getTargetProperty()
	{
		if( targetType == null )
			return null;

		if( currentTargetProperty != null )
			return currentTargetProperty;

		TestPropertyHolder actualTargetStep = getTargetStep();
		return actualTargetStep == null ? null : actualTargetStep.getProperty( targetType );
	}

	public String getTargetPropertyName()
	{
		if( targetType == null )
			return null;

		if( currentTargetProperty != null )
			return currentTargetProperty.getName();

		TestPropertyHolder actualTargetStep = getTargetStep();
		TestProperty property = actualTargetStep == null ? null : actualTargetStep.getProperty( targetType );
		return actualTargetStep == null || property == null ? targetType : property.getName();
	}

	public void setTargetPropertyName( String name )
	{
		String old = getTargetPropertyName();

		// check for change
		if( ( name == null && old == null ) || ( name != null && old != null && name.equals( old ) ) )
			return;

		// update
		targetType = name;
		config.setTargetType( name );

		// update actual property
		TestPropertyHolder targetStep2 = getTargetStep();

		currentTargetProperty = targetStep2 != null && targetType != null ? targetStep2.getProperty( targetType ) : null;

		// notify!
		propertyChangeSupport.firePropertyChange( TARGET_TYPE_PROPERTY, old, name );
	}

	public String getName()
	{
		return config.getName();
	}

	public void setSourcePath( String path )
	{
		String old = sourcePath;
		sourcePath = path;
		config.setSourcePath( path );
		propertyChangeSupport.firePropertyChange( SOURCE_PATH_PROPERTY, old, path );
	}

	public void setTargetPath( String path )
	{
		String old = targetPath;
		targetPath = path;
		config.setTargetPath( path );
		propertyChangeSupport.firePropertyChange( TARGET_PATH_PROPERTY, old, path );
	}

	public void setName( String name )
	{
		String old = this.name;
		this.name = name;
		config.setName( name );
		propertyChangeSupport.firePropertyChange( NAME_PROPERTY, old, name );
	}

	public TestPropertyHolder getSourceStep()
	{
		return getPropertyHolder( getSourceStepName() );
	}

	public String getSourceStepName()
	{
		if( sourceStep != null )
			return sourceStep;

		if( testCase == null )
			return null;

		HttpRequestTestStep step = testCase.findPreviousStepOfType( this.testStep, HttpRequestTestStep.class );
		return step == null ? null : step.getName();
	}

	public void setSourceStepName( String sourceStep )
	{
		String old = getSourceStepName();

		// check for change
		if( ( sourceStep == null && old == null ) || ( sourceStep != null && old != null && sourceStep.equals( old ) ) )
			return;

		if( sourceStep == null )
			log.debug( "Setting sourceStep for transfer [" + getName() + "] to null" );

		this.sourceStep = sourceStep;
		config.setSourceStep( sourceStep );

		if( currentSourceStep != null )
		{
			if( currentSourceStep instanceof TestStep )
				( ( TestStep )currentSourceStep ).removePropertyChangeListener( TestStep.NAME_PROPERTY,
						stepNameChangeListener );

			currentSourceStep.removeTestPropertyListener( propertyNameChangeListener );
		}

		currentSourceStep = getPropertyHolder( sourceStep );
		if( currentSourceStep != null )
		{
			if( currentSourceStep instanceof TestStep )
				( ( TestStep )currentSourceStep )
						.addPropertyChangeListener( TestStep.NAME_PROPERTY, stepNameChangeListener );

			currentSourceStep.addTestPropertyListener( propertyNameChangeListener );
		}
		else
			log.warn( "Failed to get sourceStep [" + sourceStep + "]" );

		propertyChangeSupport.firePropertyChange( SOURCE_STEP_PROPERTY, old, sourceStep );
		setSourcePropertyName( null );
	}

	public TestPropertyHolder getTargetStep()
	{
		return getPropertyHolder( getTargetStepName() );
	}

	public String getTargetStepName()
	{
		if( targetStep != null )
			return targetStep;

		if( testCase == null )
			return null;

		HttpRequestTestStep step = testCase.findNextStepOfType( this.testStep, HttpRequestTestStep.class );
		return step == null ? null : step.getName();
	}

	public void setTargetStepName( String targetStep )
	{
		String old = getTargetStepName();

		// check for change
		if( ( targetStep == null && old == null ) || ( targetStep != null && old != null && targetStep.equals( old ) ) )
			return;

		if( targetStep == null )
			log.debug( "Setting targetStep for transfer [" + getName() + "] to null" );

		this.targetStep = targetStep;
		config.setTargetStep( targetStep );

		if( currentTargetStep != null )
		{
			if( currentTargetStep instanceof TestStep )
				( ( TestStep )currentTargetStep ).removePropertyChangeListener( TestStep.NAME_PROPERTY,
						stepNameChangeListener );

			currentTargetStep.removeTestPropertyListener( propertyNameChangeListener );
		}

		currentTargetStep = getPropertyHolder( targetStep );
		if( currentTargetStep != null )
		{
			if( currentTargetStep instanceof TestStep )
				( ( TestStep )currentTargetStep )
						.addPropertyChangeListener( TestStep.NAME_PROPERTY, stepNameChangeListener );

			currentTargetStep.addTestPropertyListener( propertyNameChangeListener );
		}
		else
			log.warn( "Failed to get targetStep [" + targetStep + "]" );

		propertyChangeSupport.firePropertyChange( TARGET_STEP_PROPERTY, old, targetStep );
		setTargetPropertyName( null );
	}

	private TestPropertyHolder getPropertyHolder( String name )
	{
		if( !StringUtils.hasContent( name ) || testCase == null )
			return null;

		if( name.charAt( 0 ) == PropertyExpansion.SCOPE_PREFIX )
		{
			if( name.equals( PropertyExpansion.GLOBAL_REFERENCE ) )
				return PropertyExpansionUtils.getGlobalProperties();

			if( name.equals( PropertyExpansion.PROJECT_REFERENCE ) )
				return testCase.getTestSuite().getProject();

			if( name.equals( PropertyExpansion.TESTSUITE_REFERENCE ) )
				return testCase.getTestSuite();

			if( name.equals( PropertyExpansion.TESTCASE_REFERENCE ) )
				return testCase;
		}

		return testStep.getTestCase().getTestStepByName( name );
	}

	public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
	}

	public boolean getFailOnError()
	{
		return config.getFailOnError();
	}

	public void setFailOnError( boolean failOnError )
	{
		config.setFailOnError( failOnError );
	}

	public boolean getTransferToAll()
	{
		return config.getTransferToAll();
	}

	public void setTransferToAll( boolean transferToAll )
	{
		config.setTransferToAll( transferToAll );
	}

	public boolean getUseXQuery()
	{
		return config.getUseXQuery();
	}

	public void setUseXQuery( boolean useXQuery )
	{
		config.setUseXQuery( useXQuery );
	}

	public boolean getEntitize()
	{
		return config.getEntitize();
	}

	public void setEntitize( boolean entitize )
	{
		config.setEntitize( entitize );
	}

	public boolean getIgnoreEmpty()
	{
		return config.getIgnoreEmpty();
	}

	public void setIgnoreEmpty( boolean ignoreEmpty )
	{
		config.setIgnoreEmpty( ignoreEmpty );
	}

	public boolean getSetNullOnMissingSource()
	{
		return config.getSetNullOnMissingSource();
	}

	public void setSetNullOnMissingSource( boolean setNullOnMissingSource )
	{
		config.setSetNullOnMissingSource( setNullOnMissingSource );
	}

	public boolean getTransferTextContent()
	{
		return config.getTransferTextContent();
	}

	public void setTransferTextContent( boolean transferTextContent )
	{
		config.setTransferTextContent( transferTextContent );
	}

	public boolean isDisabled()
	{
		return config.getDisabled();
	}

	public void setDisabled( boolean disabled )
	{
		config.setDisabled( disabled );
	}

	public boolean getTransferChildNodes()
	{
		return config.getTransferChildNodes();
	}

	public void setTransferChildNodes( boolean b )
	{
		config.setTransferChildNodes( b );
	}

	private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		public void testStepRemoved( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == testCase )
			{
				String stepName = testStep.getName();
				if( stepName.equals( sourceStep ) )
				{
					setSourceStepName( null );
				}

				if( stepName.equals( targetStep ) )
				{
					setTargetStepName( null );
				}
			}
		}
	}

	/**
	 * Handle changes to source/target testStep names
	 *
	 * @author Ole.Matzura
	 */

	private class StepNameChangeListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			String oldName = ( String )evt.getOldValue();
			String newValue = ( String )evt.getNewValue();

			if( newValue == null )
			{
				log.error( "Tried to change stepname to null!" );
				Thread.dumpStack();
				return;
			}

			if( oldName.equals( sourceStep ) && currentSourceStep instanceof TestStep )
			{
				sourceStep = newValue;
				config.setSourceStep( sourceStep );
				propertyChangeSupport.firePropertyChange( SOURCE_STEP_PROPERTY, oldName, sourceStep );
			}

			if( oldName.equals( targetStep ) && currentTargetStep instanceof TestStep )
			{
				targetStep = newValue;
				config.setTargetStep( targetStep );
				propertyChangeSupport.firePropertyChange( TARGET_STEP_PROPERTY, oldName, targetStep );
			}
		}
	}

	/**
	 * Handle changes to source/target property names
	 *
	 * @author Ole.Matzura
	 */

	private class InternalTestPropertyListener extends TestPropertyListenerAdapter
	{
		public void propertyRenamed( String oldName, String newName )
		{
			if( oldName.equals( sourceType ) )
			{
				sourceType = newName;
				config.setSourceType( sourceType );
				propertyChangeSupport.firePropertyChange( SOURCE_TYPE_PROPERTY, oldName, sourceType );
			}

			if( oldName.equals( targetType ) )
			{
				targetType = newName;
				config.setTargetType( targetType );
				propertyChangeSupport.firePropertyChange( TARGET_TYPE_PROPERTY, oldName, targetType );
			}
		}

		public void propertyRemoved( String name )
		{
			if( name.equals( sourceType ) )
			{
				log.warn( "source property for transfer [" + getName() + "] in teststep [" + testStep.getName() + "/"
						+ testStep.getTestCase().getName() + "/" + testStep.getTestCase().getTestSuite().getName()
						+ "] set to null, was [" + name + "]" );

				currentSourceProperty = null;
				setSourcePropertyName( null );
			}

			if( name.equals( targetType ) )
			{
				log.warn( "target property for transfer [" + getName() + "] in teststep [" + testStep.getName() + "/"
						+ testStep.getTestCase().getName() + "/" + testStep.getTestCase().getTestSuite().getName()
						+ "] set to null, was [" + name + "]" );

				currentTargetProperty = null;
				setTargetPropertyName( null );
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void resolve( ResolveContext<?> context, PropertyTransfersTestStep parent )
	{
		if( isDisabled() )
			return;

		if( getSourceProperty() == null )
		{
			if( context.hasThisModelItem( parent, "Resolve source property", getConfig().getSourceStep() ) )
				return;
			context.addPathToResolve( parent, "Resolve source property", getConfig().getSourceStep() ).addResolvers(
					new DisablePropertyTransferResolver( this ), new CreateMissingPropertyResolver( this, parent ),
					new ChooseAnotherPropertySourceResolver( this, parent ) );
		}
		else
		{
			if( context.hasThisModelItem( parent, "Resolve source property", getConfig().getSourceStep() ) )
			{
				PathToResolve path = context.getPath( parent, "Resolve source property", getConfig().getSourceStep() );
				path.setSolved( true );
			}
		}

		if( getTargetProperty() == null )
		{
			if( context.hasThisModelItem( parent, "Resolve target property", getConfig().getTargetStep() ) )
				return;
			context.addPathToResolve( parent, "Resolve target property", getConfig().getTargetStep() ).addResolvers(
					new DisablePropertyTransferResolver( this ), new CreateMissingPropertyResolver( this, parent ),
					new ChooseAnotherPropertyTargetResolver( this, parent ) );
		}
		else
		{
			if( context.hasThisModelItem( parent, "Resolve target property", getConfig().getTargetStep() ) )
			{
				PathToResolve path = context.getPath( parent, "Resolve target property", getConfig().getTargetStep() );
				path.setSolved( true );
			}
		}

	}
}
