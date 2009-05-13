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

package com.eviware.soapui.support.components;

import java.util.List;

import javax.swing.JComponent;

public interface JInspectorPanel
{
	public <T extends Inspector> T addInspector( final T inspector );

	JComponent getComponent();

	void setDefaultDividerLocation( float v );

	public void activate( Inspector inspector );

	void setCurrentInspector( String s );

	void setDividerLocation( int i );

	void setResizeWeight( double v );

	List<Inspector> getInspectors();

	Inspector getCurrentInspector();

	Inspector getInspectorByTitle( String title );

	void deactivate();

	void removeInspector( Inspector inspector );

	void setContentComponent( JComponent component );

	int getDividerLocation();

	Inspector getInspector( String inspectorId );

	void setInspectorVisible( Inspector inspector, boolean b );

	void setResetDividerLocation();

	void release();
}
