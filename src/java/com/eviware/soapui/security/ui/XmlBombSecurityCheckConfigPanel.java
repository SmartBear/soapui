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

package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eviware.soapui.config.DTDTypeConfig;
import com.eviware.soapui.security.check.XmlBombSecurityCheck;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleForm;

@SuppressWarnings( "serial" )
public class XmlBombSecurityCheckConfigPanel extends JPanel
{
	private static final String USE_EXTERNAL_DTD_SCAN = "Use External DTD Scan";
	private static final String USE_INTERNAL_DTD_SCAN = "Use Internal DTD Scan";
	private static final String ATTACHMENT_PREFIX_FIELD = "Attachment Prefix Field";
	private static final String ENABLE_ATTACHMENT_FIELD = "Send bomb as attachment";

	private List<String> xmlBombList;
	private JTextArea attachementArea;
	private List<DTDTypeConfig> internalDTDList;
	private List<DTDTypeConfig> externalDTDList;
	private JTextArea internalDTDArea;
	private JTextArea externalDTDArea;

	private int xmlBombPosition = -1;
	private PreviousAttachement previous;
	private NextAttachement next;
	private JLabel current;
	private JLabel max;
	private JComboBox internalCombo;
	private JComboBox externalCombo;
	protected int internalPosition;
	protected int externalPosition;
	private SimpleForm form;
	private XmlBombSecurityCheck xmlChk;

	public XmlBombSecurityCheckConfigPanel( XmlBombSecurityCheck xmlCheck )
	{
		super( new BorderLayout() );

		this.xmlChk = xmlCheck;
		this.xmlBombList = xmlCheck.getXmlBombList();
		this.internalDTDList = xmlCheck.getInternalDTDList();
		this.externalDTDList = xmlCheck.getExternalDTDList();
		form = new SimpleForm();
		form.setBorder( BorderFactory.createEmptyBorder( 3, 0, 0, 3 ) );
		form.addSpace( 5 );

		form.addComponent( new JLabel( "Xml Bomb Attacments" ) );

		JCheckBox attachXml = form.appendCheckBox( ENABLE_ATTACHMENT_FIELD, null, xmlCheck.isAttachXmlBomb() );
		attachXml.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent arg0 )
			{
				form.getComponent( ATTACHMENT_PREFIX_FIELD ).setEnabled(
						( ( JCheckBox )form.getComponent( ENABLE_ATTACHMENT_FIELD ) ).isSelected() );
				xmlChk.setAttachXmlBomb( ( ( JCheckBox )form.getComponent( ENABLE_ATTACHMENT_FIELD ) ).isSelected() );
			}
		} );

		JTextField attachmentPrefixField = form.appendTextField( ATTACHMENT_PREFIX_FIELD, "Attachment Prefix Field" );
		attachmentPrefixField.setMaximumSize( new Dimension( 80, 10 ) );
		attachmentPrefixField.setColumns( 20 );
		attachmentPrefixField.setText( xmlCheck.getAttachmentPrefix() );
		attachmentPrefixField.setEnabled( xmlCheck.isAttachXmlBomb() );
		attachmentPrefixField.addKeyListener( new KeyListener()
		{

			@Override
			public void keyTyped( KeyEvent arg0 )
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased( KeyEvent arg0 )
			{
				xmlChk.setAttachmentPrefix( ( ( JTextField )form.getComponent( ATTACHMENT_PREFIX_FIELD ) ).getText() );
			}

			@Override
			public void keyPressed( KeyEvent arg0 )
			{
				// TODO Auto-generated method stub

			}
		} );
		JXToolBar toolbar = UISupport.createSmallToolbar();
		toolbar.add( previous = new PreviousAttachement() );
		previous.setEnabled( false );
		toolbar.add( next = new NextAttachement() );
		toolbar.add( new AddXmlAttachement() );
		toolbar.add( new RemoveAttachement() );
		toolbar.add( new SaveAttachement() );
		toolbar.add( current = new JLabel( "  current:0" ) );
		toolbar.add( max = new JLabel( "  max:" + xmlBombList.size() ) );
		form.append( toolbar );
		attachementArea = new JTextArea( 10, 15 );
		xmlBombPosition = 0;
		if( xmlBombList.size() > 0 )
		{
			attachementArea.setText( xmlBombList.get( xmlBombPosition ) );
			if( xmlBombList.size() == 1 )
				next.setEnabled( false );
		}
		else
			next.setEnabled( false );
		form.append( new JScrollPane( attachementArea ) );

		form.addSpace( 10 );
		form.append( new JLabel( "Internal DTD" ) );
		JCheckBox useInternalDTD = form.appendCheckBox( USE_INTERNAL_DTD_SCAN, null, xmlCheck.useInternalDTD() );
		useInternalDTD.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent arg0 )
			{
				xmlChk.setUseInternalDTD( ( ( JCheckBox )form.getComponent( USE_INTERNAL_DTD_SCAN ) ).isSelected() );
			}
		} );
		JXToolBar toolbarInternal = UISupport.createSmallToolbar();
		internalCombo = createComboBox( internalDTDList );
		toolbarInternal.add( internalCombo );
		toolbarInternal.add( new AddInternalDTD() );
		toolbarInternal.add( new RemoveInternalDTD() );
		toolbarInternal.add( new SaveInternalDTD() );
		form.append( toolbarInternal );
		internalDTDArea = new JTextArea( 10, 15 );
		internalCombo.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				if( !internalDTDList.isEmpty() && internalCombo.getSelectedIndex() != -1 )
				{
					internalDTDArea.setText( internalDTDList.get( internalCombo.getSelectedIndex() ).getValue() );
					internalPosition = internalCombo.getSelectedIndex();
				}
				// changed reference, so element is not in list.
				if( !internalDTDList.isEmpty() & internalCombo.getSelectedItem() != null
						& externalCombo.getSelectedIndex() == -1 )
				{
					internalDTDList.get( internalPosition ).setReference( ( String )internalCombo.getSelectedItem() );
					internalCombo.setModel( createComboBox( internalDTDList ).getModel() );
					internalCombo.setSelectedIndex( internalPosition );
				}
			}
		} );
		internalDTDArea.setText( internalDTDList.get( internalCombo.getSelectedIndex() ).getValue() );
		form.append( new JScrollPane( internalDTDArea ) );

		form.addSpace( 10 );
		form.append( new JLabel( "External DTD" ) );
		JCheckBox useExternalDTD = form.appendCheckBox( USE_EXTERNAL_DTD_SCAN, null, xmlCheck.useExternalDTD() );
		useExternalDTD.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent arg0 )
			{
				xmlChk.setUseExternalDTD( ( ( JCheckBox )form.getComponent( USE_EXTERNAL_DTD_SCAN ) ).isSelected() );
			}
		} );
		JXToolBar toolbarExternal = UISupport.createSmallToolbar();
		externalCombo = createComboBox( externalDTDList );
		toolbarExternal.add( externalCombo );
		toolbarExternal.add( new AddExternalDTD() );
		toolbarExternal.add( new RemoveExternalDTD() );
		toolbarExternal.add( new SaveExternalDTD() );
		form.append( toolbarExternal );
		externalDTDArea = new JTextArea( 10, 15 );
		externalCombo.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				if( !externalDTDList.isEmpty() && externalCombo.getSelectedIndex() != -1 )
				{
					externalDTDArea.setText( externalDTDList.get( externalCombo.getSelectedIndex() ).getValue() );
					externalPosition = externalCombo.getSelectedIndex();
				}
				// changed reference, so element is not in list.
				if( !externalDTDList.isEmpty() & externalCombo.getSelectedItem() != null
						& externalCombo.getSelectedIndex() == -1 )
				{
					externalDTDList.get( externalPosition ).setReference( ( String )externalCombo.getSelectedItem() );
					externalCombo.setModel( createComboBox( externalDTDList ).getModel() );
					externalCombo.setSelectedIndex( externalPosition );
				}
			}
		} );

		externalDTDArea.setText( externalDTDList.get( externalCombo.getSelectedIndex() ).getValue() );
		form.append( new JScrollPane( externalDTDArea ) );

		form.addSpace( 5 );

		add( form.getPanel() );
	}

	private JComboBox createComboBox( List<DTDTypeConfig> list )
	{
		JComboBox combo = new JComboBox();
		combo.setMaximumSize( ( new Dimension( 200, combo.getMaximumSize().height ) ) );
		for( DTDTypeConfig config : list )
			combo.addItem( config.getReference() );

		combo.setEditable( true );
		return combo;
	}

	private class PreviousAttachement extends AbstractAction
	{

		public PreviousAttachement()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/left_arrow.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Previous Xml Bomb" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			xmlBombPosition-- ;
			next.setEnabled( true );
			attachementArea.setText( xmlBombList.get( xmlBombPosition ) );
			if( xmlBombPosition == 0 )
				setEnabled( false );
			current.setText( "  current:" + xmlBombPosition );
		}

	}

	private class NextAttachement extends AbstractAction
	{

		public NextAttachement()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/right_arrow.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Next Xml Bomb" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			xmlBombPosition++ ;
			previous.setEnabled( true );
			attachementArea.setText( xmlBombList.get( xmlBombPosition ) );
			if( xmlBombPosition == xmlBombList.size() - 1 )
				setEnabled( false );
			current.setText( "  current:" + xmlBombPosition );
		}

	}

	private class SaveAttachement extends AbstractAction
	{

		public SaveAttachement()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/save_all.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Save Xml Bomb" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			xmlBombList.set( xmlBombPosition, attachementArea.getText() );
		}

	}

	private class AddXmlAttachement extends AbstractAction
	{

		public AddXmlAttachement()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Add new Xml Bomb" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			xmlBombList.add( "" );
			attachementArea.setText( "" );
			max.setText( "  max:" + xmlBombList.size() );
			xmlBombPosition = xmlBombList.size() - 1;
			current.setText( "  current:" + xmlBombPosition );
			next.setEnabled( false );
			if( xmlBombList.size() > 1 )
				previous.setEnabled( true );
		}

	}

	private class RemoveAttachement extends AbstractAction
	{

		public RemoveAttachement()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Remove current Xml Bomb" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			if( !xmlBombList.isEmpty() )
			{

				xmlBombList.remove( xmlBombPosition );
				if( xmlBombPosition >= xmlBombList.size() && !xmlBombList.isEmpty() )
					xmlBombPosition = xmlBombList.size() - 1;
				else
				{
					if( xmlBombList.isEmpty() )
					{
						xmlBombPosition = 0;
						next.setEnabled( false );
						previous.setEnabled( false );
					}

				}
				if( xmlBombList.size() == 1 )
				{
					xmlBombPosition = 0;
					next.setEnabled( false );
					previous.setEnabled( false );
				}
				if( xmlBombList.size() - 1 == xmlBombPosition )
					next.setEnabled( false );
				if( xmlBombList.isEmpty() )
					attachementArea.setText( "" );
				else
					attachementArea.setText( xmlBombList.get( xmlBombPosition ) );
				current.setText( "  current:" + xmlBombPosition );
				max.setText( "  max:" + xmlBombList.size() );
			}
		}
	}

	private class AddInternalDTD extends AbstractAction
	{

		public AddInternalDTD()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Add new Internal DTD" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			DTDTypeConfig dtd = DTDTypeConfig.Factory.newInstance();
			dtd.setReference( "newInternalDTD" + internalDTDList.size() );
			dtd.setValue( "<!DOCTYPE lolz [\n<!ENTITY lol \"lol\">\n<!ENTITY newInternalDTD \"&lol;\">\n]>" );
			internalDTDList.add( dtd );
			internalCombo.addItem( dtd.getReference() );
			internalDTDArea.setText( dtd.getValue() );
			internalCombo.setSelectedItem( dtd.getReference() );
		}
	}

	private class RemoveInternalDTD extends AbstractAction
	{

		public RemoveInternalDTD()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Remove Internal DTD" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			int deleteIndex = internalCombo.getSelectedIndex();
			if( deleteIndex > -1 )
			{
				internalDTDList.remove( deleteIndex );
				internalCombo.setModel( createComboBox( internalDTDList ).getModel() );
			}
			if( internalDTDList.isEmpty() )
			{
				internalDTDArea.setText( "" );
				internalCombo.setModel( new JComboBox().getModel() );
				internalPosition = -1;
			}
			else
			{
				internalCombo.setSelectedIndex( 0 );
				internalPosition = 0;
			}
		}

	}

	private class SaveInternalDTD extends AbstractAction
	{

		public SaveInternalDTD()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/save_all.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Save Internal DTD" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			internalDTDList.get( internalCombo.getSelectedIndex() ).setValue( internalDTDArea.getText() );
		}

	}

	private class AddExternalDTD extends AbstractAction
	{

		public AddExternalDTD()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Add new External DTD" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			DTDTypeConfig dtd = DTDTypeConfig.Factory.newInstance();
			dtd.setReference( "newExternalDTD" + externalDTDList.size() );
			dtd.setValue( "<!DOCTYPE lolz [\n<!ENTITY newExternalDTD SYSTEM \"http://www.soapui.org\">\n]>" );
			externalDTDList.add( dtd );
			externalCombo.addItem( dtd.getReference() );
			externalCombo.setSelectedItem( dtd.getReference() );
			externalDTDArea.setText( dtd.getValue() );
		}
	}

	private class RemoveExternalDTD extends AbstractAction
	{

		public RemoveExternalDTD()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Remove External DTD" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			int deleteIndex = externalCombo.getSelectedIndex();
			if( deleteIndex > -1 )
			{
				externalDTDList.remove( deleteIndex );
				externalCombo.setModel( createComboBox( externalDTDList ).getModel() );
			}
			if( externalDTDList.isEmpty() )
			{
				externalDTDArea.setText( "" );
				externalCombo.setModel( new JComboBox().getModel() );
				externalPosition = -1;
			}
			else
			{
				externalCombo.setSelectedIndex( 0 );
				externalPosition = 0;
			}
		}

	}

	private class SaveExternalDTD extends AbstractAction
	{

		public SaveExternalDTD()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/save_all.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Save External DTD" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			externalDTDList.get( externalCombo.getSelectedIndex() ).setValue( externalDTDArea.getText() );
		}

	}

}
