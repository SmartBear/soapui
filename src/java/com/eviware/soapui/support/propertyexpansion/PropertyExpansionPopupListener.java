/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.propertyexpansion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionImpl;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.GroovyEditorComponent;
import com.eviware.soapui.support.components.ShowPopupAction;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.XmlUtils;

public class PropertyExpansionPopupListener implements PopupMenuListener
{
	private final Container targetMenu;
	private final ModelItem modelItem;
	private final PropertyExpansionTarget target;

	public PropertyExpansionPopupListener( Container transferMenu, ModelItem modelItem, PropertyExpansionTarget target )
	{
		this.targetMenu = transferMenu;
		this.modelItem = modelItem;
		this.target = target;
	}

	public void popupMenuCanceled( PopupMenuEvent arg0 )
	{
	}

	public void popupMenuWillBecomeInvisible( PopupMenuEvent arg0 )
	{
	}

	public void popupMenuWillBecomeVisible( PopupMenuEvent arg0 )
	{
		// create transfer menus
		targetMenu.removeAll();

		WsdlTestStep testStep = null;
		WsdlTestCase testCase = null;
		WsdlTestSuite testSuite = null;
		WsdlProject project = null;
		WsdlMockService mockService = null;
		WsdlMockResponse mockResponse = null;

		if( modelItem instanceof WsdlTestStep )
		{
			testStep = ( WsdlTestStep )modelItem;
			testCase = testStep.getTestCase();
			testSuite = testCase.getTestSuite();
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlTestCase )
		{
			testCase = ( WsdlTestCase )modelItem;
			testSuite = testCase.getTestSuite();
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlTestSuite )
		{
			testSuite = ( WsdlTestSuite )modelItem;
			project = testSuite.getProject();
		}
		else if( modelItem instanceof WsdlMockService )
		{
			project = ( ( WsdlMockService )modelItem ).getProject();
		}
		else if( modelItem instanceof WsdlMockResponse )
		{
			mockResponse = ( WsdlMockResponse )modelItem;
			mockService = ( mockResponse ).getMockOperation().getMockService();
			project = mockService.getProject();
		}
		else if( modelItem instanceof WsdlProject )
		{
			project = ( WsdlProject )modelItem;
		}
		else if( modelItem instanceof AbstractHttpRequestInterface<?> )
		{
			project = ( ( AbstractHttpRequest<?> )modelItem ).getOperation().getInterface().getProject();
		}
		else if( modelItem instanceof Operation )
		{
			project = ( WsdlProject )( ( Operation )modelItem ).getInterface().getProject();
		}

		TestPropertyHolder globalProperties = PropertyExpansionUtils.getGlobalProperties();
		if( globalProperties.getProperties().size() > 0 )
			targetMenu.add( createPropertyMenu( "Global", globalProperties ) );

		if( project != null )
			targetMenu.add( createPropertyMenu( "Project: [" + project.getName() + "]", project ) );

		if( testSuite != null )
			targetMenu.add( createPropertyMenu( "TestSuite: [" + testSuite.getName() + "]", testSuite ) );

		if( mockService != null )
			targetMenu.add( createPropertyMenu( "MockService: [" + mockService.getName() + "]", mockService ) );

		if( mockResponse != null )
			targetMenu.add( createPropertyMenu( "MockResponse: [" + mockResponse.getName() + "]", mockResponse ) );

		if( testCase != null )
		{
			targetMenu.add( createPropertyMenu( "TestCase: [" + testCase.getName() + "]", testCase ) );

			for( int c = 0; c < testCase.getTestStepCount(); c++ )
			{
				testStep = testCase.getTestStepAt( c );
				if( testStep.getPropertyNames().length == 0 )
					continue;

				if( targetMenu.getComponentCount() == 3 )
					targetMenu.add( new JSeparator() );

				targetMenu.add( createPropertyMenu( "Step " + ( c + 1 ) + ": [" + testStep.getName() + "]", testStep ) );
			}
		}

		// if( targetMenu.getComponentCount() > 0 )
		// targetMenu.add( new JSeparator() );
		//		
		// targetMenu.add( new JMenuItem( new
		// TransferFromPropertyActionInvoker()));
	}

	private JMenu createPropertyMenu( String string, TestPropertyHolder holder )
	{
		JMenu menu = new JMenu( string );
		if( holder instanceof TestModelItem )
			menu.setIcon( ( ( TestModelItem )holder ).getIcon() );

		String[] propertyNames = holder.getPropertyNames();

		for( String name : propertyNames )
		{
			menu.add( new TransferFromPropertyActionInvoker( holder, name ) );
		}

		if( holder instanceof MutableTestPropertyHolder )
		{
			if( menu.getMenuComponentCount() > 0 )
				menu.addSeparator();

			menu.add( new TransferFromPropertyActionInvoker( ( MutableTestPropertyHolder )holder ) );
		}

		return menu;
	}

	public class TransferFromPropertyActionInvoker extends AbstractAction
	{
		private TestPropertyHolder sourceStep;
		private String sourceProperty;

		public TransferFromPropertyActionInvoker( TestPropertyHolder sourceStep, String sourceProperty )
		{
			super( "Property [" + sourceProperty + "]" );
			this.sourceStep = sourceStep;
			this.sourceProperty = sourceProperty;
		}

		public TransferFromPropertyActionInvoker( MutableTestPropertyHolder testStep )
		{
			super( "Create new.." );
			this.sourceStep = testStep;
		}

		public void actionPerformed( ActionEvent arg0 )
		{
			if( sourceProperty == null && sourceStep instanceof MutableTestPropertyHolder )
			{
				MutableTestPropertyHolder step = ( MutableTestPropertyHolder )sourceStep;
				sourceProperty = target.getNameForCreation();

				sourceProperty = UISupport.prompt( "Specify name of source property to create", "Create source property",
						sourceProperty );
				while( sourceProperty != null && step.getProperty( sourceProperty ) != null )
				{
					sourceProperty = UISupport.prompt( "Name is taken, specify unique name of source property to create",
							"Create source property", sourceProperty );
				}

				if( sourceProperty == null )
				{
					return;
				}

				( ( MutableTestPropertyHolder )sourceStep ).addProperty( sourceProperty );
			}

			String sourceXPath = "";

			try
			{
				String val = sourceStep.getPropertyValue( sourceProperty );
				if( StringUtils.isNullOrEmpty( val ) )
				{
					String defaultValue = sourceStep.getProperty( sourceProperty ).getDefaultValue();
					if( StringUtils.hasContent( defaultValue ) )
					{
						if( UISupport.confirm( "Missing property value, use default value instead?", "Get Data" ) )
						{
							val = defaultValue;
						}
					}
				}

				if( XmlUtils.seemsToBeXml( val ) )
				{
					XmlObject.Factory.parse( val );
					sourceXPath = UISupport.selectXPath( "Select XPath", "Select source xpath for property transfer", val,
							null );
				}
			}
			catch( Throwable e )
			{
				// just ignore.. this wasn't xml..
			}

			if( StringUtils.hasContent( sourceXPath ) )
			{
				sourceXPath = XmlUtils.removeXPathNamespaceDeclarations( sourceXPath );
				if( sourceXPath.length() > 0 )
					sourceXPath = sourceXPath.replace( '\n', ' ' );
			}

			TestProperty property = sourceStep.getProperty( sourceProperty );
			PropertyExpansion pe = new PropertyExpansionImpl( property, sourceXPath );

			String valueForCreation = target.getValueForCreation();
			target.insertPropertyExpansion( pe, null );

			if( !StringUtils.hasContent( sourceXPath ) && StringUtils.hasContent( valueForCreation )
					&& !property.isReadOnly() )
			{
				valueForCreation = UISupport.prompt( "Init property value to", "Get Data", valueForCreation );
				if( valueForCreation != null )
				{
					property.setValue( valueForCreation );
				}
			}
		}
	}

	public static void addMenu( JPopupMenu popup, String menuName, ModelItem item, PropertyExpansionTarget component )
	{
		JMenu menu = new JMenu( menuName );
		popup.add( menu );
		popup.addPopupMenuListener( new PropertyExpansionPopupListener( menu, item, component ) );
	}

	public static void enable( JTextComponent textField, ModelItem modelItem, JPopupMenu popup )
	{
		JTextComponentPropertyExpansionTarget target = new JTextComponentPropertyExpansionTarget( textField, modelItem );
		DropTarget dropTarget = new DropTarget( textField, new PropertyExpansionDropTarget( target ) );
		dropTarget.setDefaultActions( DnDConstants.ACTION_COPY_OR_MOVE );

		textField.setComponentPopupMenu( popup );

		if( popup != null )
		{
			PropertyExpansionPopupListener.addMenu( popup, "Get Data..", target.getContextModelItem(), target );
		}
	}

	public static JPanel addPropertyExpansionPopup( JTextField textField, JPopupMenu popup, ModelItem modelItem )
	{
		PropertyExpansionPopupListener.enable( textField, modelItem, popup );

		JButton popupButton = new JButton();
		popupButton.setAction( new ShowPopupAction( textField, popupButton ) );
		popupButton.setBackground( Color.WHITE );
		popupButton.setForeground( Color.WHITE );
		popupButton.setBorder( null );
		popupButton.setOpaque( true );
		JPanel panel = new JPanel( new BorderLayout() );
		panel.add( textField, BorderLayout.CENTER );
		panel.add( popupButton, BorderLayout.EAST );
		panel.setBorder( textField.getBorder() );
		textField.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

		return panel;
	}

	public static void enable( JXEditTextArea textField, ModelItem modelItem )
	{
		JXEditTextAreaPropertyExpansionTarget target = new JXEditTextAreaPropertyExpansionTarget( textField, modelItem );
		DropTarget dropTarget = new DropTarget( textField, new PropertyExpansionDropTarget( target ) );
		dropTarget.setDefaultActions( DnDConstants.ACTION_COPY_OR_MOVE );

		JPopupMenu popup = textField.getRightClickPopup();

		if( popup != null )
		{
			PropertyExpansionPopupListener.addMenu( popup, "Get Data..", target.getContextModelItem(), target );
		}
	}

	public static void enable( GroovyEditor groovyEditor, ModelItem modelItem )
	{
		GroovyEditorPropertyExpansionTarget target = new GroovyEditorPropertyExpansionTarget( groovyEditor, modelItem );
		DropTarget dropTarget = new DropTarget( groovyEditor.getEditArea(), new PropertyExpansionDropTarget( target ) );
		dropTarget.setDefaultActions( DnDConstants.ACTION_COPY_OR_MOVE );

		JPopupMenu popup = groovyEditor.getEditArea().getComponentPopupMenu();

		if( popup != null )
		{
			JMenu menu = new JMenu( "Get Data.." );
			popup.insert( menu, 0 );
			popup.addPopupMenuListener( new PropertyExpansionPopupListener( menu, target.getContextModelItem(), target ) );
			popup.insert( new JSeparator(), 1 );
		}
	}

	public static void enable( JTextComponent textField, ModelItem modelItem )
	{
		JPopupMenu popupMenu = textField.getComponentPopupMenu();
		if( popupMenu == null )
		{
			popupMenu = new JPopupMenu();
			textField.setComponentPopupMenu( popupMenu );
		}

		enable( textField, modelItem, popupMenu );
	}

	public static void disable( GroovyEditor editor )
	{
	}

	public static void enable( GroovyEditorComponent gec, ModelItem modelItem  )
	{
		enable( gec.getEditor(), modelItem );
	}
}