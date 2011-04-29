package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.support.HelpActionMarker;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JFormComponent;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringList;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JFormDialog;
import com.eviware.x.impl.swing.JTextFieldFormField;

public class MaliciousAttachmentMutationsPanel
{

	private JFormDialog dialog;
	private MaliciousAttachmentSecurityCheckConfig config;

	private ListToTablesHolder holder = new ListToTablesHolder();

	public MaliciousAttachmentMutationsPanel( MaliciousAttachmentSecurityCheckConfig config )
	{
		this.config = config;
		dialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationSettings.class );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "component", createMutationsPanel() );
		dialog.getFormField( MutationSettings.MUTATIONS_PANEL ).setProperty( "dimension", new Dimension( 720, 320 ) );
		holder.refresh();
	}

	private JComponent buildFilesList()
	{
		FileListFormComponent filesList = new FileListFormComponent();
		holder.setFilesList( filesList );
		filesList.setData( new String[] { "workspace/soapUI-core-DEV/copyrightInXsd.groovy", "file2", "file3" } );
		JScrollPane scrollPane = new JScrollPane( filesList );
		return scrollPane;
	}

	private JComponent buildTables()
	{
		JFormDialog tablesDialog = ( JFormDialog )ADialogBuilder.buildDialog( MutationTables.class );

		MutationTableModel generateTableModel = new GenerateMutationTableModel();
		tablesDialog.getFormField( MutationTables.GENERATE_FILE ).setProperty( "dimension", new Dimension( 410, 120 ) );
		tablesDialog.getFormField( MutationTables.GENERATE_FILE ).setProperty( "component",
				buildTable( generateTableModel, false ) );

		MutationTableModel replaceTableModel = new ReplaceMutationTableModel();
		tablesDialog.getFormField( MutationTables.REPLACE_FILE ).setProperty( "dimension", new Dimension( 410, 120 ) );
		tablesDialog.getFormField( MutationTables.REPLACE_FILE ).setProperty( "component",
				buildTable( replaceTableModel, true ) );

		holder.setGenerateTableModel( generateTableModel );
		holder.setReplaceTableModel( replaceTableModel );
		holder.setTablesDialog( tablesDialog );

		return tablesDialog.getPanel();
	}

	protected JPanel buildTable( MutationTableModel tableModel, boolean add )
	{
		JPanel panel = new JPanel( new BorderLayout() );
		JXTable table = new JXTable( tableModel );
		setupTable( table );
		JScrollPane tableScrollPane = new JScrollPane( table );
		tableScrollPane.setBorder( BorderFactory.createEmptyBorder() );

		JXToolBar toolbar = UISupport.createToolbar();

		if( add )
		{
			toolbar.add( UISupport.createToolbarButton( new AddFileAction() ) );
		}
		else
		{
			toolbar.add( UISupport.createToolbarButton( new GenerateFileAction() ) );
		}

		toolbar.add( UISupport.createToolbarButton( new RemoveFileAction( tableModel, table ) ) );
		toolbar.add( UISupport.createToolbarButton( new HelpAction( "www.soapui.org" ) ) );

		panel.add( toolbar, BorderLayout.PAGE_START );
		panel.add( tableScrollPane, BorderLayout.CENTER );

		panel.setBorder( BorderFactory.createLineBorder( new Color( 0 ), 1 ) );

		return panel;
	}

	protected void setupTable( JXTable table )
	{
		table.setPreferredScrollableViewportSize( new Dimension( 50, 90 ) );
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		table.getTableHeader().setReorderingAllowed( false );
		table.setDefaultEditor( String.class, getDefaultCellEditor() );
		table.setSortable( false );
	}

	private Object createMutationsPanel()
	{
		JPanel panel = new JPanel( new BorderLayout() );
		JSplitPane mainSplit = UISupport.createHorizontalSplit( buildFilesList(), buildTables() );
		mainSplit.setResizeWeight( 1 );
		panel.add( mainSplit, BorderLayout.CENTER );
		return panel;
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

	@AForm( description = "Malicious Attachment Mutation Tables", name = "Malicious Attachment Mutation Tables" )
	protected interface MutationTables
	{
		@AField( description = "Selected", name = "Selected", type = AFieldType.LABEL )
		final static String SELECTED_FILE = "Selected";
		@AField( description = "Generate file", name = "Generate file", type = AFieldType.COMPONENT )
		final static String GENERATE_FILE = "Generate file";
		@AField( description = "Replace file", name = "Replace file", type = AFieldType.COMPONENT )
		final static String REPLACE_FILE = "Replace file";
		@AField( description = "Remove file", name = "Do not send attachment", type = AFieldType.BOOLEAN )
		final static String REMOVE_FILE = "Do not send attachment";
	}

	@AForm( description = "Generate File Mutation", name = "Generate File Mutation" )
	protected interface GenerateFile
	{
		@AField( description = "Size", name = "Size", type = AFieldType.INT )
		final static String SIZE = "Size";
		@AField( description = "Content type", name = "Content type", type = AFieldType.STRING )
		final static String CONTENT_TYPE = "Content type";
	}

	class FileListFormComponent extends JPanel implements JFormComponent, ActionListener
	{
		private DefaultListModel listModel;
		private JList list;
		private String oldSelection;
		private String currentSelection;

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
					currentSelection = ( String )listModel.get( list.getSelectedIndex() );
					holder.refresh( oldSelection, currentSelection );
					oldSelection = currentSelection;
				}
			} );
		}

		public String getFirstItem()
		{
			if( list.getModel().getSize() != 0 )
			{
				list.setSelectedIndex( 0 );
				return ( String )list.getSelectedValue();
			}
			return "";
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

	class ListToTablesHolder
	{
		FileListFormComponent filesList;
		MutationTableModel generateTableModel;
		MutationTableModel replaceTableModel;
		JFormDialog tablesDialog;

		Map<String, List<FileElement>> generateMap = new HashMap<String, List<FileElement>>();
		Map<String, List<FileElement>> replaceMap = new HashMap<String, List<FileElement>>();
		Map<String, Boolean> removeMap = new HashMap<String, Boolean>();

		public JFormDialog getTablesDialog()
		{
			return tablesDialog;
		}

		public void setTablesDialog( JFormDialog tablesDialog )
		{
			this.tablesDialog = tablesDialog;
		}

		public FileListFormComponent getFilesList()
		{
			return filesList;
		}

		public void setFilesList( FileListFormComponent filesList )
		{
			this.filesList = filesList;
		}

		public MutationTableModel getGenerateTableModel()
		{
			return generateTableModel;
		}

		public void setGenerateTableModel( MutationTableModel generateTableModel )
		{
			this.generateTableModel = generateTableModel;
		}

		public MutationTableModel getReplaceTableModel()
		{
			return replaceTableModel;
		}

		public void setReplaceTableModel( MutationTableModel replaceTableModel )
		{
			this.replaceTableModel = replaceTableModel;
		}

		public void refresh()
		{
			if( filesList != null )
			{
				String item = filesList.getFirstItem();
				tablesDialog.getFormField( MutationTables.SELECTED_FILE ).setValue( item );
			}
		}

		public void refresh( String oldItem, String newItem )
		{
			if( oldItem != null )
			{
				save( oldItem );
			}
			load( newItem );
		}

		public void addResultToGenerateTable( File file, String contentType, Boolean enabled )
		{
			generateTableModel.addResult( file, contentType, enabled );
		}

		public void addResultToReplaceTable( File file, String contentType, Boolean enabled )
		{
			replaceTableModel.addResult( file, contentType, enabled );
		}

		private void save( String item )
		{
			List<FileElement> generateList = new ArrayList<FileElement>();
			List<FileElement> replaceList = new ArrayList<FileElement>();

			for( int i = 0; i < generateTableModel.getRowCount(); i++ )
			{
				generateList.add( generateTableModel.getRowValue( i ) );
			}

			for( int i = 0; i < replaceTableModel.getRowCount(); i++ )
			{
				replaceList.add( replaceTableModel.getRowValue( i ) );
			}

			Boolean remove = tablesDialog.getBooleanValue( MutationTables.REMOVE_FILE );

			generateMap.put( item, generateList );
			replaceMap.put( item, replaceList );
			removeMap.put( item, remove );

		}

		private void load( String item )
		{
			List<FileElement> generateList = generateMap.get( item );
			List<FileElement> replaceList = replaceMap.get( item );
			Boolean remove = removeMap.get( item );

			tablesDialog.setValue( MutationTables.SELECTED_FILE, item );

			generateTableModel.clear();
			replaceTableModel.clear();
			tablesDialog.setBooleanValue( MutationTables.REMOVE_FILE, new Boolean( false ) );

			if( remove != null )
			{
				tablesDialog.setBooleanValue( MutationTables.REMOVE_FILE, remove );
			}

			if( generateList != null )
			{
				for( FileElement element : generateList )
				{
					generateTableModel.addResult( element.getFile(), element.getContentType(), element.isEnabled() );
				}
			}

			if( replaceList != null )
			{
				for( FileElement element : replaceList )
				{
					replaceTableModel.addResult( element.getFile(), element.getContentType(), element.isEnabled() );
				}
			}
		}
	}

	class FileElementHolder
	{
		List<FileElement> list;

		protected void addElement( File file, String contentType, Boolean enabled )
		{
			if( list == null )
			{
				list = new ArrayList<FileElement>();
			}

			list.add( new FileElement( file, contentType, enabled ) );
		}

		protected void removeElement( int i )
		{
			if( list != null )
			{
				list.remove( i );
			}
		}

		protected int size()
		{
			if( list != null )
			{
				return list.size();
			}
			else
			{
				return 0;
			}
		}

		protected void clear()
		{
			if( list != null )
			{
				list.clear();
			}
		}

		protected List<FileElement> getList()
		{
			return list;
		}
	}

	class FileElement
	{
		File file;
		String contentType;
		Boolean enabled;

		FileElement( File file, String contentType, Boolean enabled )
		{
			this.file = file;
			this.contentType = contentType;
			this.enabled = enabled;
		}

		public void setFile( File file )
		{
			this.file = file;
			this.contentType = new MimetypesFileTypeMap().getContentType( file );
		}

		public void setContentType( String contentType )
		{
			this.contentType = contentType;
		}

		public void setEnabled( Boolean enabled )
		{
			this.enabled = enabled;
		}

		public String getFileName()
		{
			return file.getAbsolutePath();
		}

		public long getFileLength()
		{
			return file.length();
		}

		public File getFile()
		{
			return file;
		}

		public String getContentType()
		{
			return this.contentType;
		}

		public Boolean isEnabled()
		{
			return enabled;
		}
	}

	private abstract class MutationTableModel extends AbstractTableModel
	{
		protected FileElementHolder holder = new FileElementHolder();

		public int getRowCount()
		{
			return holder.size();
		}

		public void addResult( File file, String contentType, Boolean enabled )
		{
			holder.addElement( file, contentType, enabled );
			fireTableDataChanged();
		}

		public void removeResult( int i )
		{
			holder.removeElement( i );
			fireTableDataChanged();
		}

		public void clear()
		{
			holder.clear();
			fireTableDataChanged();
		}

		public FileElement getRowValue( int rowIndex )
		{
			return holder.getList().get( rowIndex );
		}

		public abstract int getColumnCount();

		public abstract String getColumnName( int column );

		public abstract Object getValueAt( int rowIndex, int columnIndex );
	}

	private class GenerateMutationTableModel extends MutationTableModel
	{

		public Class<?> getColumnClass( int columnIndex )
		{
			return columnIndex == 2 ? Boolean.class : columnIndex == 1 ? String.class : String.class;
		}

		public boolean isCellEditable( int row, int col )
		{
			if( col > 0 )
			{
				return true;
			}
			else
			{
				return false;
			}
		}

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

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			FileElement element = holder.getList().get( rowIndex );

			if( element != null )
			{
				switch( columnIndex )
				{
				case 0 :
					return element.getFileLength();
				case 1 :
					return element.getContentType();
				case 2 :
					return element.isEnabled();
				}
			}

			return null;
		}

		public void setValueAt( Object aValue, int row, int column )
		{
			if( holder.getList().isEmpty() )
			{
				return;
			}
			FileElement element = holder.getList().get( row );

			switch( column )
			{
			case 0 :
				element.setFile( ( File )aValue );
				break;
			case 1 :
				element.setContentType( ( String )aValue );
				break;
			case 2 :
				element.setEnabled( ( Boolean )aValue );
				break;
			}
		}
	}

	private class ReplaceMutationTableModel extends MutationTableModel
	{
		public Class<?> getColumnClass( int columnIndex )
		{
			return columnIndex == 3 ? Boolean.class : columnIndex == 2 ? String.class : String.class;
		}

		public boolean isCellEditable( int row, int col )
		{
			if( col > 1 )
			{
				return true;
			}
			else
			{
				return false;
			}
		}

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

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			FileElement element = holder.getList().get( rowIndex );

			if( element != null )
			{
				switch( columnIndex )
				{
				case 0 :
					return element.getFileName();
				case 1 :
					return element.getFileLength();
				case 2 :
					return element.getContentType();
				case 3 :
					return element.isEnabled();
				}
			}

			return null;
		}

		public void setValueAt( Object aValue, int row, int column )
		{
			if( holder.getList().isEmpty() )
			{
				return;
			}
			FileElement element = holder.getList().get( row );

			switch( column )
			{
			case 2 :
				element.setContentType( ( String )aValue );
				break;
			case 3 :
				element.setEnabled( ( Boolean )aValue );
				break;
			}
		}
	}

	public class AddFileAction extends AbstractAction
	{
		private JFileChooser fileChooser;
		private String projectRoot = ProjectSettings.PROJECT_ROOT;

		public AddFileAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Add file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( fileChooser == null )
			{
				fileChooser = new JFileChooser();

			}

			fileChooser.setCurrentDirectory( new File( projectRoot ) );

			int returnVal = fileChooser.showOpenDialog( UISupport.getMainFrame() );
			if( returnVal == JFileChooser.APPROVE_OPTION )
			{
				// TODO: actually replace file
				holder.addResultToReplaceTable( fileChooser.getSelectedFile(),
						new MimetypesFileTypeMap().getContentType( fileChooser.getSelectedFile() ), true );

			}
		}
	}

	public class GenerateFileAction extends AbstractAction
	{
		private XFormDialog dialog;

		public GenerateFileAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Generate file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( dialog == null )
			{
				dialog = ADialogBuilder.buildDialog( GenerateFile.class );
				( ( JTextFieldFormField )dialog.getFormField( GenerateFile.CONTENT_TYPE ) ).setWidth( 30 );
			}

			dialog.show();

			if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
			{
				int newSizeInt = 0;
				String newSizeString = dialog.getValue( GenerateFile.SIZE );
				String contentType = dialog.getFormField( GenerateFile.CONTENT_TYPE ).getValue();

				try
				{
					newSizeInt = Integer.parseInt( newSizeString );
				}
				catch( NumberFormatException nfe )
				{
					UISupport.showErrorMessage( "Size must be integer number" );
					return;
				}

				// TODO: actually generate file
				holder.addResultToGenerateTable( new File( "file" ), contentType, true );
			}
		}
	}

	public class RemoveFileAction extends AbstractAction
	{
		private MutationTableModel tableModel;
		private JXTable table;

		public RemoveFileAction( MutationTableModel tableModel, JXTable table )
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Remove file" );

			this.tableModel = tableModel;
			this.table = table;
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = table.getSelectedRow();
			if( row >= 0 )
			{
				tableModel.removeResult( row );
			}

		}
	}

	public class HelpAction extends AbstractAction implements HelpActionMarker
	{
		private final String url;

		public HelpAction( String url )
		{
			this( "Online Help", url, UISupport.getKeyStroke( "F1" ) );
		}

		public HelpAction( String title, String url )
		{
			this( title, url, null );
		}

		public HelpAction( String title, String url, KeyStroke accelerator )
		{
			super( title );
			this.url = url;
			putValue( Action.SHORT_DESCRIPTION, "Show online help" );
			if( accelerator != null )
				putValue( Action.ACCELERATOR_KEY, accelerator );

			putValue( Action.SMALL_ICON, UISupport.HELP_ICON );
		}

		public void actionPerformed( ActionEvent e )
		{
			Tools.openURL( url );
		}
	}

	protected TableCellEditor getDefaultCellEditor()
	{
		return new XPathCellRender();
	}

}
