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

package com.eviware.soapui.support.editor.inspectors.attachments;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;

public class AttachmentsInspector extends AbstractXmlInspector implements PropertyChangeListener
{
	private AttachmentContainer container;
	private AttachmentsPanel attachmentsPanel;

	public AttachmentsInspector( AttachmentContainer container )
	{
		super( "Attachments (" + container.getAttachmentCount() + ")", "Files attached to this message", true,
				AttachmentsInspectorFactory.INSPECTOR_ID );
		this.container = container;

		container.addAttachmentsChangeListener( this );
	}

	public JComponent getComponent()
	{
		if( attachmentsPanel == null )
		{
			attachmentsPanel = new AttachmentsPanel( container );
		}

		return attachmentsPanel;
	}

	@Override
	public void release()
	{
		super.release();
		attachmentsPanel.release();
		container.removeAttachmentsChangeListener( this );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		setTitle( "Attachments (" + container.getAttachmentCount() + ")" );
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}
}
