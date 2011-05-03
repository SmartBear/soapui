package com.eviware.soapui.security.support;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.security.tools.AttachmentElement;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel.MutationTables;
import com.eviware.x.impl.swing.JFormDialog;

public class MaliciousAttachmentListToTableHolder
{

	MaliciousAttachmentFilesListForm filesList;
	MaliciousAttachmentTableModel generateTableModel;
	MaliciousAttachmentTableModel replaceTableModel;
	JFormDialog tablesDialog;

	Map<String, List<AttachmentElement>> generateMap = new HashMap<String, List<AttachmentElement>>();
	Map<String, List<AttachmentElement>> replaceMap = new HashMap<String, List<AttachmentElement>>();
	Map<String, Boolean> removeMap = new HashMap<String, Boolean>();

	public JFormDialog getTablesDialog()
	{
		return tablesDialog;
	}

	public void setTablesDialog( JFormDialog tablesDialog )
	{
		this.tablesDialog = tablesDialog;
	}

	public MaliciousAttachmentFilesListForm getFilesList()
	{
		return filesList;
	}

	public void setFilesList( MaliciousAttachmentFilesListForm filesList )
	{
		this.filesList = filesList;
	}

	public MaliciousAttachmentTableModel getGenerateTableModel()
	{
		return generateTableModel;
	}

	public void setGenerateTableModel( MaliciousAttachmentTableModel generateTableModel )
	{
		this.generateTableModel = generateTableModel;
	}

	public MaliciousAttachmentTableModel getReplaceTableModel()
	{
		return replaceTableModel;
	}

	public void setReplaceTableModel( MaliciousAttachmentTableModel replaceTableModel )
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

	public void refresh( Attachment oldItem, Attachment newItem )
	{
		if( oldItem != null )
		{
			save( oldItem.getName() );
		}
		load( newItem.getName() );
	}

	public void addResultToGenerateTable( File file, String contentType, Boolean enabled, Boolean cached )
	{
		generateTableModel.addResult( file, contentType, enabled, cached );
	}

	public void addResultToReplaceTable( File file, String contentType, Boolean enabled, Boolean cached )
	{
		replaceTableModel.addResult( file, contentType, enabled, cached );
	}

	private void save( String item )
	{
		List<AttachmentElement> generateList = new ArrayList<AttachmentElement>();
		List<AttachmentElement> replaceList = new ArrayList<AttachmentElement>();

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
		List<AttachmentElement> generateList = generateMap.get( item );
		List<AttachmentElement> replaceList = replaceMap.get( item );
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
			for( AttachmentElement element : generateList )
			{
				generateTableModel.addResult( element.getAttachment(), element.getContentType(), element.isEnabled(),
						element.isCached() );
			}
		}

		if( replaceList != null )
		{
			for( AttachmentElement element : replaceList )
			{
				replaceTableModel.addResult( element.getAttachment(), element.getContentType(), element.isEnabled(),
						element.isCached() );
			}
		}
	}

}
