/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

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

package com.eviware.soapui.impl.rest.panels.request.inspectors.representations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.panels.method.RestRepresentationsTable;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;

public abstract class AbstractRestRepresentationsInspector extends AbstractXmlInspector implements
		PropertyChangeListener
{
	private RestRepresentationsTable representationTable;
	private final RestMethod restMethod;
	private List<RestRepresentation.Type> types;

	protected AbstractRestRepresentationsInspector( RestMethod restMethod, String name, String description,
			RestRepresentation.Type[] types )
	{
		super( name, description, true, RestRepresentationsInspectorFactory.INSPECTOR_ID );
		this.restMethod = restMethod;
		this.types = Arrays.asList( types );

		restMethod.addPropertyChangeListener( "representations", this );
		updateLabel();
	}

	public JComponent getComponent()
	{
		if( representationTable == null )
		{
			buildUI();
		}

		return representationTable;
	}

	protected void addToToolbar( JXToolBar toolbar )
	{
	}

	protected void buildUI()
	{
		representationTable = new RestRepresentationsTable( restMethod,
				types.toArray( new RestRepresentation.Type[] {} ), true )
		{
			protected JXToolBar buildToolbar()
			{
				JXToolBar toolbar = super.buildToolbar();
				addToToolbar( toolbar );
				return toolbar;
			}
		};
	}

	public RestMethod getMethod()
	{
		return restMethod;
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}

	public boolean beforeSubmit( Submit submit, SubmitContext context )
	{
		return true;
	}

	@Override
	public void release()
	{
		representationTable.release();
		restMethod.removePropertyChangeListener( "representations", this );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		updateLabel();
	}

	private void updateLabel()
	{
		int cnt = 0;
		for( RestRepresentation representation : restMethod.getRepresentations() )
		{
			if( types.contains( representation.getType() ) )
				cnt++ ;
		}

		setTitle( "Representations (" + cnt + ")" );
	}
}