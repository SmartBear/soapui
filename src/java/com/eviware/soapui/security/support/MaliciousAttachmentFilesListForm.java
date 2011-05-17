package com.eviware.soapui.security.support;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.config.MaliciousAttachmentConfig;
import com.eviware.soapui.config.MaliciousAttachmentElementConfig;
import com.eviware.soapui.config.MaliciousAttachmentSecurityCheckConfig;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.security.tools.AttachmentElement;
import com.eviware.soapui.security.ui.MaliciousAttachmentMutationsPanel.MutationTables;
import com.eviware.soapui.support.UISupport;

public class MaliciousAttachmentFilesListForm extends JPanel
{
	private DefaultListModel listModel;
	private JList list;
	private AttachmentElement oldSelection;
	private AttachmentElement currentSelection;
	private MaliciousAttachmentSecurityCheckConfig config;
	final MaliciousAttachmentListToTableHolder holder;

	public MaliciousAttachmentFilesListForm( MaliciousAttachmentSecurityCheckConfig config,
			MaliciousAttachmentListToTableHolder holder )
	{
		super( new BorderLayout() );

		this.config = config;
		this.holder = holder;

		JPanel p = UISupport.createEmptyPanel( 3, 3, 3, 3 );
		p.add( new JLabel( "<html><b>Existing Attachments</b></html>" ), BorderLayout.WEST );
		add( p, BorderLayout.NORTH );

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
				currentSelection = ( list.getSelectedIndex() == -1 ) ? null : ( AttachmentElement )listModel.get( list
						.getSelectedIndex() );
				MaliciousAttachmentFilesListForm.this.holder.refresh( oldSelection, currentSelection );
				oldSelection = currentSelection;
			}
		} );

		setBorder( null );
	}

	public AttachmentElement getFirstItem()
	{
		if( list.getModel().getSize() != 0 )
		{
			list.setSelectedIndex( 0 );
			return ( AttachmentElement )list.getSelectedValue();
		}
		return null;
	}

	public JList getList()
	{
		return list;
	}

	public AttachmentElement[] getData()
	{
		AttachmentElement[] result = new AttachmentElement[listModel.size()];
		for( int c = 0; c < result.length; c++ )
		{
			result[c] = ( AttachmentElement )listModel.get( c );
		}
		return result;
	}

	public void setData( Attachment[] attachments )
	{
		MaliciousAttachmentSecurityCheckConfig copy = ( MaliciousAttachmentSecurityCheckConfig )config.copy();

		listModel.clear();
		config.getElementList().clear();
		holder.getGenerateTableModel().clear();
		holder.getReplaceTableModel().clear();
		holder.getTablesDialog().setBooleanValue( MutationTables.REMOVE_FILE, new Boolean( false ) );

		if( attachments != null )
		{
			for( Attachment att : attachments )
			{
				AttachmentElement attEl = new AttachmentElement( att, att.getId() );
				listModel.addElement( attEl );

				holder.getGenerateTableModel().clear();
				holder.getReplaceTableModel().clear();
				holder.getTablesDialog().setBooleanValue( MutationTables.REMOVE_FILE, new Boolean( false ) );

				// add empty element
				MaliciousAttachmentElementConfig newElement = config.addNewElement();

				newElement.setKey( attEl.getId() );

				for( MaliciousAttachmentElementConfig element : copy.getElementList() )
				{
					if( attEl.getId().equals( element.getKey() ) )
					{
						newElement.setKey( attEl.getId() );
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

						holder.refresh( attEl, null );
						break;
					}
				}
			}
		}
	}

	public void updateConfig( MaliciousAttachmentSecurityCheckConfig config )
	{
		this.config = config;
	}

	public void release()
	{
		list = null;
		config = null;
	}
}