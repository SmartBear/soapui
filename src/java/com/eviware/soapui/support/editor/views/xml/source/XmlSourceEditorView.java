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

package com.eviware.soapui.support.editor.views.xml.source;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.LineNumbersPanel;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.PreviewCorner;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.editor.xml.XmlLocation;
import com.eviware.soapui.support.editor.xml.support.ValidationError;
import com.eviware.soapui.support.swing.SoapUISplitPaneUI;
import com.eviware.soapui.support.xml.JXEditTextArea;
import com.eviware.soapui.support.xml.actions.FormatXmlAction;
import com.eviware.soapui.support.xml.actions.LoadXmlTextAreaAction;
import com.eviware.soapui.support.xml.actions.SaveXmlTextAreaAction;

/**
 * Default "XML" source editor view in soapUI
 * 
 * @author ole.matzura
 */

public class XmlSourceEditorView<T extends ModelItem> extends AbstractXmlEditorView<XmlDocument> implements
		PropertyChangeListener
{
	private JXEditTextArea editArea;
	private ValidateMessageXmlAction validateXmlAction;
	private JSplitPane splitter;
	private JScrollPane errorScrollPane;
	private DefaultListModel errorListModel;
	private FormatXmlAction formatXmlAction;
	private SaveXmlTextAreaAction saveXmlTextAreaAction;
	private boolean updating;
	private JPopupMenu editorPopup;
	public boolean isLocating;
	private JScrollPane editorScrollPane;
	private LoadXmlTextAreaAction loadXmlTextAreaAction;
	private JPopupMenu inputPopup;
	private LineNumbersPanel lineNumbersPanel;
	private JCheckBoxMenuItem toggleLineNumbersMenuItem;
	private PreviewCorner previewCorner;
	private final T modelItem;

	public XmlSourceEditorView( XmlEditor<XmlDocument> xmlEditor, T modelItem )
	{
		super( "XML", xmlEditor, XmlSourceEditorViewFactory.VIEW_ID );
		this.modelItem = modelItem;
	}

	protected void buildUI()
	{
		editArea = JXEditTextArea.createXmlEditor( false );
		editArea.setMinimumSize( new Dimension( 50, 50 ) );
		editArea.setCaretPosition( 0 );
		editArea.setDiscardEditsOnSet( false );
		editArea.setEnabled( false );
		editArea.setBorder( BorderFactory.createMatteBorder( 0, 2, 0, 0, Color.WHITE ) );

		errorListModel = new DefaultListModel();
		JList list = new JList( errorListModel );
		list.addMouseListener( new ValidationListMouseAdapter( list, editArea ) );
		errorScrollPane = new JScrollPane( list );
		errorScrollPane.setVisible( false );

		splitter = new JSplitPane( JSplitPane.VERTICAL_SPLIT )
		{
			public void requestFocus()
			{
				SwingUtilities.invokeLater( new Runnable()
				{

					public void run()
					{
						editArea.requestFocusInWindow();
					}
				} );
			}

			public boolean hasFocus()
			{
				return editArea.hasFocus();
			}
		};

		splitter.setUI( new SoapUISplitPaneUI() );
		splitter.setDividerSize( 0 );
		splitter.setOneTouchExpandable( true );

		lineNumbersPanel = new LineNumbersPanel( editArea );
		lineNumbersPanel.setVisible( SoapUI.getSettings().getBoolean( UISettings.SHOW_XML_LINE_NUMBERS ) );

		editorPopup = new JPopupMenu();
		buildPopup( editorPopup, editArea );

		editArea.setRightClickPopup( editorPopup );
		editArea.getDocument().addDocumentListener( new DocumentListenerAdapter()
		{

			public void update( Document document )
			{
				if( !updating && getDocument() != null )
				{
					updating = true;
					getDocument().setXml( editArea.getText() );
					updating = false;
				}
			}
		} );

		editArea.getInputHandler().addKeyBinding( "A+V", validateXmlAction );
		editArea.getInputHandler().addKeyBinding( "A+F", formatXmlAction );
		editArea.getInputHandler().addKeyBinding( "C+S", saveXmlTextAreaAction );
		editArea.getInputHandler().addKeyBinding( "ALT+L", new ActionListener()
		{

			public void actionPerformed( ActionEvent e )
			{
				lineNumbersPanel.setVisible( !lineNumbersPanel.isVisible() );
				toggleLineNumbersMenuItem.setSelected( lineNumbersPanel.isVisible() );
			}
		} );

		JPanel p = new JPanel( new BorderLayout() );
		p.add( editArea, BorderLayout.CENTER );
		p.add( lineNumbersPanel, BorderLayout.WEST );

		editorScrollPane = new JScrollPane( p );
		splitter.setTopComponent( editorScrollPane );
		splitter.setBottomComponent( errorScrollPane );
		splitter.setDividerLocation( 1.0 );
		splitter.setBorder( null );

		previewCorner = UISupport.addPreviewCorner( getEditorScrollPane(), true );
	}

	public JScrollPane getEditorScrollPane()
	{
		return editorScrollPane;
	}

	public T getModelItem()
	{
		return modelItem;
	}

	protected void buildPopup( JPopupMenu inputPopup, JXEditTextArea editArea )
	{
		this.inputPopup = inputPopup;
		validateXmlAction = new ValidateMessageXmlAction();
		formatXmlAction = new FormatXmlAction( editArea );
		saveXmlTextAreaAction = new SaveXmlTextAreaAction( editArea, "Save" );
		loadXmlTextAreaAction = new LoadXmlTextAreaAction( editArea, "Load" );

		toggleLineNumbersMenuItem = new JCheckBoxMenuItem( "Show Line Numbers", lineNumbersPanel.isVisible() );
		toggleLineNumbersMenuItem.setAccelerator( UISupport.getKeyStroke( "alt L" ) );
		toggleLineNumbersMenuItem.addActionListener( new ActionListener()
		{

			public void actionPerformed( ActionEvent e )
			{
				lineNumbersPanel.setVisible( toggleLineNumbersMenuItem.isSelected() );
			}
		} );

		inputPopup.add( validateXmlAction );
		inputPopup.add( formatXmlAction );
		inputPopup.addSeparator();
		inputPopup.add( editArea.getUndoAction() );
		inputPopup.add( editArea.getRedoAction() );
		inputPopup.add( editArea.createCopyAction() );
		inputPopup.add( editArea.createCutAction() );
		inputPopup.add( editArea.createPasteAction() );
		inputPopup.addSeparator();
		inputPopup.add( editArea.getFindAndReplaceAction() );
		inputPopup.addSeparator();
		inputPopup.add( editArea.getGoToLineAction() );
		inputPopup.add( toggleLineNumbersMenuItem );

		inputPopup.addSeparator();
		inputPopup.add( saveXmlTextAreaAction );
		inputPopup.add( loadXmlTextAreaAction );
	}

	@Override
	public void release()
	{
		super.release();
		inputPopup.removeAll();
		previewCorner.release();
	}

	private final static class ValidationListMouseAdapter extends MouseAdapter
	{
		private final JList list;

		private final JXEditTextArea textArea;

		public ValidationListMouseAdapter( JList list, JXEditTextArea textArea )
		{
			this.list = list;
			this.textArea = textArea;
		}

		public void mouseClicked( MouseEvent e )
		{
			if( e.getClickCount() < 2 )
				return;

			int ix = list.getSelectedIndex();
			if( ix == -1 )
				return;

			Object obj = list.getModel().getElementAt( ix );
			if( obj instanceof ValidationError )
			{
				ValidationError error = ( ValidationError )obj;
				if( error.getLineNumber() >= 0 )
				{
					textArea.setCaretPosition( textArea.getLineStartOffset( error.getLineNumber() - 1 ) );
					textArea.requestFocus();
				}
				else
					Toolkit.getDefaultToolkit().beep();
			}
			else
				Toolkit.getDefaultToolkit().beep();
		}
	}

	public JXEditTextArea getInputArea()
	{
		getComponent();
		return editArea;
	}

	public void setEditable( boolean enabled )
	{
		getComponent();
		editArea.setEditable( enabled );
	}

	protected ValidationError[] validateXml( String xml )
	{
		try
		{
			XmlObject.Factory.parse( xml, new XmlOptions().setLoadLineNumbers() );
		}
		catch( XmlException e )
		{
			List<ValidationError> result = new ArrayList<ValidationError>();

			if( e.getErrors() != null )
			{
				for( Object error : e.getErrors() )
				{
					if( error instanceof XmlError )
						result.add( new com.eviware.soapui.model.testsuite.AssertionError( ( XmlError )error ) );
					else
						result.add( new com.eviware.soapui.model.testsuite.AssertionError( error.toString() ) );
				}
			}

			if( result.isEmpty() )
				result.add( new com.eviware.soapui.model.testsuite.AssertionError( e.toString() ) );

			return result.toArray( new ValidationError[result.size()] );
		}

		return null;
	}

	public class ValidateMessageXmlAction extends AbstractAction
	{
		public ValidateMessageXmlAction()
		{
			super( "Validate" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "alt V" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( validate() )
				UISupport.showInfoMessage( "Validation OK" );
		}
	}

	public boolean activate( XmlLocation location )
	{
		super.activate( location );

		if( location != null )
			setLocation( location );

		editArea.requestFocus();

		return true;
	}

	public JComponent getComponent()
	{
		if( splitter == null )
			buildUI();

		return splitter;
	}

	public XmlLocation getEditorLocation()
	{
		return new XmlLocation( getCurrentLine() + 1, getCurrentColumn() );
	}

	public void setLocation( XmlLocation location )
	{
		int line = location.getLine() - 1;
		if( location != null && line >= 0 )
		{
			int caretLine = editArea.getCaretLine();
			int offset = editArea.getLineStartOffset( line );

			try
			{
				editArea.setCaretPosition( offset + location.getColumn() );
				int scrollLine = line + ( line > caretLine ? 3 : -3 );
				if( scrollLine >= editArea.getLineCount() )
					scrollLine = editArea.getLineCount() - 1;
				else if( scrollLine < 0 )
					scrollLine = 0;

				editArea.scrollTo( scrollLine, location.getColumn() );
			}
			catch( RuntimeException e )
			{
			}
		}
	}

	public int getCurrentLine()
	{
		if( editArea == null )
			return -1;
		return editArea.getCaretLine();
	}

	public int getCurrentColumn()
	{
		if( editArea == null )
			return -1;
		return editArea.getCaretColumn();
	}

	public String getText()
	{
		if( editArea == null )
			return null;
		return editArea.getText();
	}

	public boolean validate()
	{
		ValidationError[] errors = validateXml( PropertyExpansionUtils.expandProperties( getModelItem(), editArea
				.getText() ) );

		errorListModel.clear();
		if( errors == null || errors.length == 0 )
		{
			splitter.setDividerLocation( 1.0 );
			splitter.setDividerSize( 0 );
			errorScrollPane.setVisible( false );
			return true;
		}
		else
		{
			Toolkit.getDefaultToolkit().beep();
			for( int c = 0; c < errors.length; c++ )
			{
				errorListModel.addElement( errors[c] );
			}
			errorScrollPane.setVisible( true );
			splitter.setDividerLocation( 0.8 );
			splitter.setDividerSize( 10 );
			return false;
		}
	}

	public void setXml( String xml )
	{
		if( !updating )
		{
			updating = true;

			if( xml == null )
			{
				editArea.setText( "" );
				editArea.setEnabled( false );
			}
			else
			{
				int caretPosition = editArea.getCaretPosition();

				editArea.setEnabled( true );
				editArea.setText( xml );

				editArea.setCaretPosition( caretPosition < xml.length() ? caretPosition : 0 );
			}

			updating = false;
		}
	}

	public boolean saveDocument( boolean validate )
	{
		return validate ? validate() : true;
	}

	public void locationChanged( XmlLocation location )
	{
		isLocating = true;
		setLocation( location );
		isLocating = false;
	}

	public JPopupMenu getEditorPopup()
	{
		return editorPopup;
	}

	public boolean hasFocus()
	{
		return editArea.hasFocus();
	}

	public boolean isInspectable()
	{
		return true;
	}

	public ValidateMessageXmlAction getValidateXmlAction()
	{
		return validateXmlAction;
	}
}
