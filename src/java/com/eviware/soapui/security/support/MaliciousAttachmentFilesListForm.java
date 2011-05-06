package com.eviware.soapui.security.support;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.config.MaliciousAttachmentElementConfig;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel.MutationTables;

public class MaliciousAttachmentFilesListForm extends JPanel
{
	private DefaultListModel listModel;
	private JList list;
	private Attachment oldSelection;
	private Attachment currentSelection;
	private MaliciousAttachmentSecurityCheckConfig config;
	final MaliciousAttachmentListToTableHolder holder;

	public MaliciousAttachmentFilesListForm( MaliciousAttachmentSecurityCheckConfig config,
			MaliciousAttachmentListToTableHolder holder )
	{
		super( new BorderLayout() );

		this.config = config;
		this.holder = holder;

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
				currentSelection = ( list.getSelectedIndex() == -1 ) ? null : ( Attachment )listModel.get( list
						.getSelectedIndex() );
				MaliciousAttachmentFilesListForm.this.holder.refresh( oldSelection, currentSelection );
				oldSelection = currentSelection;
			}
		} );
	}

	public String getFirstItem()
	{
		if( list.getModel().getSize() != 0 )
		{
			list.setSelectedIndex( 0 );
			Attachment attachment = ( Attachment )list.getSelectedValue();
			return ( attachment != null ) ? attachment.getName() : "";
		}
		return "";
	}

	public JList getList()
	{
		return list;
	}

	public Attachment[] getData()
	{
		Attachment[] result = new Attachment[listModel.size()];
		for( int c = 0; c < result.length; c++ )
		{
			result[c] = ( Attachment )listModel.get( c );
		}
		return result;
	}

	public void setData( Attachment[] attachments )
	{
		Attachment[] oldData = getData();
		MaliciousAttachmentSecurityCheckConfig copy = ( MaliciousAttachmentSecurityCheckConfig )config.copy();

		listModel.clear();
		config.getElementList().clear();

		if( attachments != null )
		{
			for( Attachment att : attachments )
			{
				listModel.addElement( att );

				holder.getGenerateTableModel().clear();
				holder.getReplaceTableModel().clear();
				holder.getTablesDialog().setBooleanValue( MutationTables.REMOVE_FILE, new Boolean( false ) );

				// add empty element
				MaliciousAttachmentElementConfig newElement = config.addNewElement();

				for( MaliciousAttachmentElementConfig element : copy.getElementList() )
				{
					if( element.getKey().equals( att.getName() ) )
					{
						newElement.setKey( att.getName() );
						newElement.setRemove( element.getRemove() );
						holder.getTablesDialog().setBooleanValue( MutationTables.REMOVE_FILE, element.getRemove() );

						for( MaliciousAttachmentConfig el : element.getGenerateAttachmentList() )
						{
							MaliciousAttachmentConfig newEl = newElement.addNewGenerateAttachment();
							newEl.setFilename( el.getFilename() );
							newEl.setSize( el.getSize() );
							newEl.setContentType( el.getContentType() );
							newEl.setEnabled( el.getEnabled() );
							newEl.setCached( el.getCached() );

							holder.addResultToGenerateTable( newEl );
						}

						for( MaliciousAttachmentConfig el : element.getReplaceAttachmentList() )
						{
							MaliciousAttachmentConfig newEl = newElement.addNewReplaceAttachment();
							newEl.setFilename( el.getFilename() );
							newEl.setSize( el.getSize() );
							newEl.setContentType( el.getContentType() );
							newEl.setEnabled( el.getEnabled() );
							newEl.setCached( el.getCached() );

							holder.addResultToReplaceTable( newEl );
						}

						holder.refresh( att, null );
						break;
					}
				}
			}

			firePropertyChange( "attachments", oldData, getData() );
		}
	}

	public void updateConfig( MaliciousAttachmentSecurityCheckConfig config )
	{
		this.config = config;
	}
}