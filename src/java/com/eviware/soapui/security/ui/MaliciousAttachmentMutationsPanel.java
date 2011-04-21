package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

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

	private Map<String, Object> tableMap = new HashMap<String, Object>();

	public MaliciousAttachmentMutationsPanel( MaliciousAttachmentSecurityCheckConfig config )
	{
		this.config = config;
		dialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationSettings.class );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "component", createMutationsPanel() );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "dimension", new Dimension( 620, 300 ) );
		initDialog();
	}

	private JComponent buildFilesList()
	{
		FileListFormComponent filesList = new FileListFormComponent();

		filesList.setData( new String[] { "workspace/soapUI-core-DEV/copyrightInXsd.groovy", "file2", "file3" } );

		return filesList;
	}

	protected void update( String key )
	{

	}

	private JComponent buildTables()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		JXTable generateTable = new JXTable( new GenerateMutationTableModel() );
		JScrollPane generateTableScrollpane = new JScrollPane( generateTable );
		generateTable.setPreferredScrollableViewportSize( new Dimension( 50, 50 ) );
		generateTable.setFillsViewportHeight( true );

		JXTable replaceTable = new JXTable( new ReplaceMutationTableModel() );
		JScrollPane replaceTableScrollPane = new JScrollPane( replaceTable );
		replaceTable.setPreferredScrollableViewportSize( new Dimension( 50, 50 ) );
		replaceTable.setFillsViewportHeight( true );

		panel.add( generateTableScrollpane, BorderLayout.NORTH );
		panel.add( new JSeparator( SwingConstants.HORIZONTAL ) );
		panel.add( replaceTableScrollPane, BorderLayout.SOUTH );
		panel.add( new JSeparator( SwingConstants.HORIZONTAL ) );

		panel.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );

		return panel;
	}

	private Object createMutationsPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );

		JSplitPane mainSplit = UISupport.createHorizontalSplit( buildFilesList(), buildTables() );
		mainSplit.setResizeWeight( 0.20 );
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

	class FileListFormComponent extends JPanel implements JFormComponent, ActionListener
	{
		private DefaultListModel listModel;
		private JList list;

		public FileListFormComponent()
		{
			super( new BorderLayout() );

			listModel = new DefaultListModel();
			list = new JList( listModel );
			list.setToolTipText( "Choose file" );
			JScrollPane scrollPane = new JScrollPane( list );
			scrollPane.setPreferredSize( new Dimension( 30, 50 ) );
			add( scrollPane, BorderLayout.CENTER );

			list.addListSelectionListener( new ListSelectionListener()
			{

				public void valueChanged( ListSelectionEvent e )
				{
					String key = null;
					MaliciousAttachmentMutationsPanel.this.update( key );
				}
			} );
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

			int selectedIndex = list.getSelectedIndex();

			String elm = ( String )listModel.getElementAt( selectedIndex );
			String value = UISupport.prompt( "Specify value", "Edit..", elm );

			if( value != null )
			{
				listModel.setElementAt( value, selectedIndex );
				firePropertyChange( "options", oldData, getData() );
			}
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

		public void addItem( String valueOf )
		{
			listModel.addElement( valueOf );
		}
	}

	private abstract class MutationTableModel extends AbstractTableModel
	{
		protected List<File> files = new ArrayList<File>();

		public synchronized int getRowCount()
		{
			return files.size();
		}

		public synchronized void clear()
		{
			files.clear();
			fireTableDataChanged();
		}

		public boolean isCellEditable( int row, int col )
		{
			if( col < 2 )
			{
				return false;
			}
			else
			{
				return true;
			}
		}

		public void addResult( File file )
		{
			int rowCount;
			synchronized( this )
			{
				rowCount = getRowCount();
				files.add( file );
			}

			fireTableRowsInserted( rowCount, rowCount++ );
		}

		public abstract int getColumnCount();

		public abstract String getColumnName( int column );

		public abstract Object getValueAt( int rowIndex, int columnIndex );
	}

	private class GenerateMutationTableModel extends MutationTableModel
	{
		public int getColumnCount()
		{
			return 3;
		}

		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Size";
			case 1 :
				return "Content type";
			case 2 :
				return "Enable";
			}

			return null;
		}

		public synchronized Object getValueAt( int rowIndex, int columnIndex )
		{
			File file = null;

			file = files.get( rowIndex );

			if( file != null )
			{
				switch( columnIndex )
				{
				case 0 :
					return file.length();
				case 1 :
					return new MimetypesFileTypeMap().getContentType( file );
				case 2 :
					return false;
				}
			}

			return null;
		}
	}

	private class ReplaceMutationTableModel extends MutationTableModel
	{
		public int getColumnCount()
		{
			return 4;
		}

		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "With";
			case 1 :
				return "Size";
			case 2 :
				return "Content type";
			case 3 :
				return "Enable";
			}

			return null;
		}

		public synchronized Object getValueAt( int rowIndex, int columnIndex )
		{
			File file = null;

			file = files.get( rowIndex );

			if( file != null )
			{
				switch( columnIndex )
				{
				case 0 :
					return "file";
				case 1 :
					return file.length();
				case 2 :
					return new MimetypesFileTypeMap().getContentType( file );
				case 3 :
					return false;
				}
			}

			return null;
		}
	}

}
