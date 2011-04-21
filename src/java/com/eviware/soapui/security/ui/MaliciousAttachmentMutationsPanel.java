package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.model.security.MaliciousAttachmentsTableModel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JFormComponent;
import com.eviware.soapui.support.types.StringList;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JFormDialog;

public class MaliciousAttachmentMutationsPanel
{

	private JFormDialog dialog;
	private MaliciousAttachmentSecurityCheckConfig config;
	private MaliciousAttachmentsTableModel tableModel;

	public MaliciousAttachmentMutationsPanel( MaliciousAttachmentSecurityCheckConfig config )
	{
		this.config = config;
		dialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationSettings.class );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "component", createMutationsPanel() );
		initDialog();
	}

	private JComponent buildFilesList()
	{
		StringListFormComponent filesList = new StringListFormComponent( null, true, true, "" );

		filesList.setPreferredSize( new Dimension( 50, 400 ) );

		filesList.setData( new String[] { "file1", "file2", "file3" } );

		return filesList;
	}

	protected void updatePanel()
	{

	}

	private JComponent buildTables()
	{
		// JPanel panel = new JPanel( new BorderLayout() );
		// JXTable table = new JXTable( new TransfersTableModel() );
		//
		// //table.setColumnControlVisible( true );
		// table.setHorizontalScrollEnabled( true );
		// table.packAll();
		return new JPanel( new BorderLayout() );
	}

	private Object createMutationsPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		JComponent filesList = buildFilesList();
		JSplitPane mainSplit = UISupport.createHorizontalSplit( filesList, buildTables() );
		mainSplit.setResizeWeight( 0.4 );
		panel.add( mainSplit, BorderLayout.CENTER );

		panel.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );

		return panel;
	}

	private void initDialog()
	{

	}

	public JComponent getPanel()
	{
		return dialog.getPanel();
	}

	@AForm( description = "Malicious Attachment Mutations", name = "Malicious Attachment Mutations" )
	protected interface MutationSettings
	{
		@AField( description = "###Mutations panel", name = "###Mutations panel", type = AFieldType.COMPONENT )
		final static String MUTATIONS_PANEL = "###Mutations panel";
	}
	
 class StringListFormComponent extends JPanel implements JFormComponent, ActionListener
	{
		private DefaultListModel listModel;
		private String defaultValue = null;
		private JButton addButton;
		private JButton removeButton;
		private JList list;
		private JButton editButton;
		private Box buttonBox;
		private List<JButton> buttons = new ArrayList<JButton>();

		public StringListFormComponent( String tooltip )
		{
			this( tooltip, false, false, null );
		}

		public StringListFormComponent( String tooltip, boolean editOnly )
		{
			this( tooltip, editOnly, false, null );
		}

		public StringListFormComponent( String tooltip, boolean editOnly, boolean readOnly )
		{
			this( tooltip, editOnly, readOnly, null );
		}

		public StringListFormComponent( String tooltip, boolean editOnly, boolean readOnly, String defaultValue )
		{
			super( new BorderLayout() );

			this.defaultValue = defaultValue;
			listModel = new DefaultListModel();
			list = new JList( listModel );
			list.setToolTipText( tooltip );
			JScrollPane scrollPane = new JScrollPane( list );
			scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
			scrollPane.setPreferredSize( new Dimension( 300, 70 ) );
			add( scrollPane, BorderLayout.CENTER );
			buttonBox = new Box( BoxLayout.Y_AXIS );
			buttonBox.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 0 ) );

			if( !editOnly || !readOnly )
			{
				addButton = new JButton( "Add.." );
				addButton.addActionListener( this );
				buttonBox.add( addButton );
				buttonBox.add( Box.createVerticalStrut( 5 ) );
			}

			if( !readOnly )
			{
				editButton = new JButton( "Edit.." );
				editButton.addActionListener( this );
				buttons.add( editButton );
				buttonBox.add( editButton );
			}

			if( !editOnly || !readOnly )
			{
				buttonBox.add( Box.createVerticalStrut( 5 ) );
				removeButton = new JButton( "Remove.." );
				removeButton.addActionListener( this );
				buttonBox.add( removeButton );
				buttons.add( removeButton );
			}

			add( buttonBox, BorderLayout.EAST );

			list.addListSelectionListener( new ListSelectionListener()
			{

				public void valueChanged( ListSelectionEvent e )
				{
					setButtonState();
				}
			} );

			setButtonState();
		}

		public void addButton( Action action, boolean requireSelection )
		{
			buttonBox.add( Box.createVerticalStrut( 5 ) );
			JButton button = new JButton( action );
			buttonBox.add( button );

			if( requireSelection )
			{
				buttons.add( button );
				setButtonState();
			}
		}

		public void setValue( String value )
		{
			String[] oldData = getData();
			listModel.clear();

			try
			{
				StringList stringList = StringList.fromXml( value );

				String[] files = stringList.toStringArray();
				for( String file : files )
					if( file.trim().length() > 0 )
						listModel.addElement( file );

				firePropertyChange( "data", oldData, getData() );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		public String getValue()
		{
			StringList result = new StringList( listModel.toArray() );
			return result.toXml();
		}

		public JList getList()
		{
			return list;
		}

		public void actionPerformed( ActionEvent e )
		{
			String[] oldData = getData();

			if( e.getSource() == addButton )
			{
				String value = UISupport.prompt( "Specify value to add", "Add..", defaultValue );
				if( value != null )
				{
					listModel.addElement( value );
					firePropertyChange( "options", oldData, getData() );
				}
			}
			else
			{
				int selectedIndex = list.getSelectedIndex();

				if( e.getSource() == removeButton && selectedIndex != -1 )
				{
					Object elm = listModel.getElementAt( selectedIndex );
					if( UISupport.confirm( "Remove [" + elm.toString() + "] from list", "Remove" ) )
					{
						listModel.remove( selectedIndex );
						firePropertyChange( "options", oldData, getData() );
					}
				}
				else if( e.getSource() == editButton && selectedIndex != -1 )
				{
					String elm = ( String )listModel.getElementAt( selectedIndex );
					String value = UISupport.prompt( "Specify value", "Edit..", elm );

					if( value != null )
					{
						listModel.setElementAt( value, selectedIndex );
						firePropertyChange( "options", oldData, getData() );
					}
				}
			}
		}

		public void setButtonState()
		{
			boolean b = list.getSelectedIndex() != -1;
			for( JButton button : buttons )
				button.setEnabled( b );
		}

		public String[] getData()
		{
			String[] result = new String[listModel.size()];
			for( int c = 0; c < result.length; c++ )
				result[c] = ( String )listModel.get( c );

			return result;
		}

		public void setData( String[] strings )
		{
			String[] oldData = getData();

			listModel.clear();
			if( strings != null )
			{
				for( String str : strings )
				{
					listModel.addElement( str );
				}
			}

			firePropertyChange( "options", oldData, getData() );
		}

		public String[] getOptions()
		{
			return getData();
		}

		public void setOptions( String[] options )
		{
			setData( options );
		}

		public void setEnabled( boolean b )
		{
			addButton.setEnabled( b );
			list.setEnabled( b );
			if( b )
				setButtonState();
		}

		public void addItem( String valueOf )
		{
			listModel.addElement( valueOf );
		}}

}
