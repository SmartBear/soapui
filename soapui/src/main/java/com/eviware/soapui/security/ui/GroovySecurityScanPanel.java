/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.AbstractGroovyEditorModel;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.security.scan.GroovySecurityScan;
import com.eviware.soapui.support.components.GroovyEditorComponent;

public class GroovySecurityScanPanel extends SecurityScanConfigPanel
{
	protected static final String SCRIPT_FIELD = "Script";

	private GroovySecurityScan groovyCheck;
	private GroovyEditorComponent groovyEditor;

	public GroovySecurityScanPanel( GroovySecurityScan securityCheck )
	{
		super( new BorderLayout() );

		groovyCheck = securityCheck;

		add( buildSetupScriptPanel( securityCheck ) );
	}

	@Override
	public void save()
	{

	}

	private class ScriptGroovyEditorModel extends AbstractGroovyEditorModel
	{
		@Override
		public Action createRunAction()
		{
			return new AbstractAction()
			{

				public void actionPerformed( ActionEvent e )
				{
					// nothing happens!
				}
			};
		}

		public ScriptGroovyEditorModel( ModelItem modelItem )
		{
			super( new String[] { "parameters", "log", "context", "securityScan", "testStep" }, modelItem, "" );
		}

		public String getScript()
		{
			return ( ( GroovySecurityScan )getModelItem() ).getExecuteScript();
		}

		public void setScript( String text )
		{
			( ( GroovySecurityScan )getModelItem() ).setExecuteScript( text );
		}
	}

	protected GroovyEditorComponent buildSetupScriptPanel( SecurityScan securityCheck )
	{
		groovyEditor = new GroovyEditorComponent( new ScriptGroovyEditorModel( securityCheck.getModelItem() ), null );
		groovyEditor.setPreferredSize( new Dimension( 385, 150 ) );
		return groovyEditor;
	}

}
