/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.request;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;

import java.awt.*;

import static com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle.QUERY;

public class RestRequestDesktopPanel extends
		AbstractRestRequestDesktopPanel<RestRequestInterface, RestRequestInterface>
{
	private JButton addToTestCaseButton;
	protected TextPanelWithTopLabel resourcePanel;
	protected TextPanelWithTopLabel queryPanel;

	public RestRequestDesktopPanel( RestRequestInterface modelItem )
	{
		super( modelItem, modelItem );
	}

	@Override
	protected void initializeFields()
	{
		String path = getRequest().getResource().getFullPath();
		resourcePanel = new TextPanelWithTopLabel( "Resource", path);

		String query = RestUtils.getQueryParamsString( getRequest().getParams(), getRequest() );
		queryPanel = new TextPanelWithTopLabel( "Query", query, false );
	}

	@Override
	protected void init( RestRequestInterface request )
	{
		addToTestCaseButton = createActionButton( SwingActionDelegate.createDelegate(
				AddRestRequestToTestCaseAction.SOAPUI_ACTION_ID, getRequest(), null, "/addToTestCase.gif" ), true );

		super.init( request );
	}

	protected String getHelpUrl()
	{
		return HelpUrls.RESTREQUESTEDITOR_HELP_URL;
	}

	public void setEnabled( boolean enabled )
	{
		super.setEnabled( enabled );
		addToTestCaseButton.setEnabled( enabled );
	}

	@Override
	protected JComponent buildToolbar()
	{
		if( getRequest().getResource() != null )
		{
			JPanel panel = new JPanel( new BorderLayout() );

			JXToolBar baseToolBar = UISupport.createToolbar();

			JComponent submitButton = super.getSubmitButton();
			baseToolBar.add( submitButton );
			baseToolBar.add( cancelButton );

			// insertButtons injects different buttons for different editors. It is overridden in other subclasses
			insertButtons( baseToolBar );
			JPanel methodPanel = new JPanel( new BorderLayout() );
			JComboBox<RestRequestInterface.RequestMethod> methodComboBox = new JComboBox<RestRequestInterface.RequestMethod>( new RestRequestMethodModel( getRequest() ) );
			methodComboBox.setSelectedItem( getRequest().getMethod() );

			JLabel methodLabel = new JLabel( "Method" );
			methodPanel.add( methodLabel, BorderLayout.NORTH );
			methodPanel.add( methodComboBox, BorderLayout.SOUTH );
			methodPanel.setMinimumSize( new Dimension( 75, STANDARD_TOOLBAR_HEIGHT ) );
			//TODO: remove hard coded height adjustment
			methodPanel.setMaximumSize( new Dimension( 75, STANDARD_TOOLBAR_HEIGHT + 10 ) );

			JPanel endpointPanel = new JPanel( new BorderLayout() );
			endpointPanel.setMinimumSize( new Dimension( 75, STANDARD_TOOLBAR_HEIGHT ) );

			JComponent endpointCombo = buildEndpointComponent();
			super.setEndpointComponent( endpointCombo );

			JLabel endPointLabel = new JLabel( "Endpoint" );String query = RestUtils.getQueryParamsString( getRequest().getParams(), getRequest() );
			queryPanel = new TextPanelWithTopLabel( "Query", query, false );

			endpointPanel.add( endPointLabel, BorderLayout.NORTH );
			endpointPanel.add( endpointCombo, BorderLayout.SOUTH );


			baseToolBar.addWithOnlyMinimumHeight( methodPanel );
			baseToolBar.add( Box.createHorizontalStrut( 4 ) );
			baseToolBar.addWithOnlyMinimumHeight( endpointPanel );
			baseToolBar.add( Box.createHorizontalStrut( 4 ) );

			addResourceAndQueryField( baseToolBar );

			baseToolBar.add( Box.createHorizontalGlue() );
			baseToolBar.add( tabsButton );
			baseToolBar.add( splitButton );
			baseToolBar.add( UISupport.createToolbarButton( new ShowOnlineHelpAction( getHelpUrl() ) ) );
			int maximumPreferredHeight = findMaximumPreferredHeight( baseToolBar ) + 6;
			baseToolBar.setPreferredSize( new Dimension( 600, Math.max( maximumPreferredHeight, STANDARD_TOOLBAR_HEIGHT ) ) );

			panel.add( baseToolBar, BorderLayout.NORTH );

			addMethodSelectorToolbar( panel );

			return panel;
		}
		else
		{
			//TODO: If we don't need special clause for empty resources then remove it
			return super.buildToolbar();
		}
	}

	@Override
	protected void updateUiValues()
	{
		resourcePanel.setText( getRequest().getResource().getFullPath() );
		resetQueryPanelText();

	}



	@Override
	protected void insertButtons( JXToolBar toolbar )
	{
		toolbar.add( addToTestCaseButton );
	}


	private void resetQueryPanelText()
	{
			queryPanel.setText( RestUtils.getQueryParamsString( getRequest().getParams(), getRequest() ) );

	}

	private class TextPanelWithTopLabel extends JPanel
	{
		private final Color MAC_DISABLED_BGCOLOR = new Color( 232, 232, 232 );

		JLabel textLabel;
		JTextField textField;



		TextPanelWithTopLabel( String label, String text )
		{
			textLabel = new JLabel( label );
			textField = new JTextField( text );
			setToolTipText( text );
			super.setLayout( new BorderLayout() );
			super.add( textLabel, BorderLayout.NORTH );
			super.add( textField, BorderLayout.SOUTH );
		}

		public TextPanelWithTopLabel( String label, String text, boolean isEditable )
		{
			this( label, text );
			textField.setEditable( isEditable );
			if( !isEditable && UISupport.isMac() )
			{
				textField.setBackground( MAC_DISABLED_BGCOLOR );
			}
		}

		public TextPanelWithTopLabel( String label, String text, DocumentListener documentListener )
		{
			this( label, text );
			textField.getDocument().addDocumentListener( documentListener );
		}


		public String getText()
		{
			return textField.getText();
		}

		public void setText( String text )
		{
			textField.setText( text );
			setToolTipText( text );
		}

		@Override
		public void setToolTipText( String text )
		{
			super.setToolTipText( text );
			textLabel.setToolTipText( text );
			textField.setToolTipText( text );
		}
	}

	private void addResourceAndQueryField( JXToolBar toolbar )
	{
		if( !( getRequest() instanceof RestTestRequestInterface ) )
		{

			toolbar.addWithOnlyMinimumHeight( resourcePanel );

			toolbar.add( Box.createHorizontalStrut( 4 ) );


			toolbar.addWithOnlyMinimumHeight( queryPanel );
		}
	}

	//TODO: Temporary fix resource panel should be moved to appropriate subclass
	private String getResourcePanelText()
	{
		if( resourcePanel == null )
			return "";
		else
			return resourcePanel.getText();
	}

	private void setResourcePanelText( String text )
	{
		if( resourcePanel != null )
		{
			resourcePanel.setText( text );
		}
	}
}
