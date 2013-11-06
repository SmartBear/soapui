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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import com.eviware.soapui.impl.wsdl.teststeps.ManualTestStep;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public class ManualTestStepDesktopPanel extends ModelItemDesktopPanel<ManualTestStep>
{
	private JSplitPane split;
	private JUndoableTextField nameField;
	private JUndoableTextArea descriptionField;
	private JUndoableTextArea expectedResultField;

	public ManualTestStepDesktopPanel( ManualTestStep modelItem )
	{
		super( modelItem );

		buildUI();
	}

	private void buildUI()
	{
		ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addFixed( new JLabel( "TestStep Name" ) );
		builder.addRelatedGap();
		nameField = new JUndoableTextField( getModelItem().getName() );
		nameField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{
			@Override
			public void update( Document document )
			{
				getModelItem().setName( nameField.getText() );
			}
		} );

		nameField.setPreferredSize( new Dimension( 200, 20 ) );
		builder.addFixed( nameField );

		builder.getPanel().setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		add( builder.getPanel(), BorderLayout.NORTH );

		split = UISupport.createVerticalSplit( createDescriptionField(), createExpectedResultField() );
		add( split, BorderLayout.CENTER );

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				split.setDividerLocation( 200 );
			}
		} );
	}

	protected JUndoableTextField getNameField()
	{
		return nameField;
	}

	protected JUndoableTextArea getDescriptionField()
	{
		return descriptionField;
	}

	protected JUndoableTextArea getExpectedResultField()
	{
		return expectedResultField;
	}

	private JPanel createDescriptionField()
	{
		JPanel panel = UISupport.createEmptyPanel( 3, 3, 3, 3 );

		ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addFixed( new JLabel( "<html><b>Description - Describe what actions to perform</b></html>" ) );
		panel.add( builder.getPanel(), BorderLayout.NORTH );

		descriptionField = new JUndoableTextArea( getModelItem().getDescription() );
		descriptionField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{
			@Override
			public void update( Document document )
			{
				getModelItem().setDescription( descriptionField.getText() );
			}
		} );

		panel.add( new JScrollPane( descriptionField ) );

		return panel;
	}

	private JPanel createExpectedResultField()
	{
		JPanel panel = UISupport.createEmptyPanel( 3, 3, 3, 3 );

		ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addFixed( new JLabel( "<html><b>Expected Result - Describe the expected outcome</b></html>" ) );
		panel.add( builder.getPanel(), BorderLayout.NORTH );

		expectedResultField = new JUndoableTextArea( getModelItem().getExpectedResult() );
		expectedResultField.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{
			@Override
			public void update( Document document )
			{
				getModelItem().setExpectedResult( expectedResultField.getText() );
			}
		} );

		panel.add( new JScrollPane( expectedResultField ) );

		return panel;
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{
		super.propertyChange( evt );

		String newValue = String.valueOf( evt.getNewValue() );
		if( evt.getPropertyName().equals( ManualTestStep.NAME_PROPERTY ) )
		{
			if( !newValue.equals( nameField.getText() ) )
				nameField.setText( newValue );
		}
		else if( evt.getPropertyName().equals( ManualTestStep.DESCRIPTION_PROPERTY ) )
		{
			if( !newValue.equals( descriptionField.getText() ) )
				descriptionField.setText( newValue );
		}
		else if( evt.getPropertyName().equals( "expectedResult" ) )
		{
			if( !newValue.equals( expectedResultField.getText() ) )
				expectedResultField.setText( newValue );
		}

	}

}
