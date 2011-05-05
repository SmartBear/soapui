/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
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
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.security.check.GroovySecurityCheck;
import com.eviware.soapui.support.components.GroovyEditorComponent;

public class GroovySecurityCheckPanel extends SecurityCheckConfigPanel
{
	protected static final String SCRIPT_FIELD = "Script";

	private GroovySecurityCheck groovyCheck;
	private GroovyEditorComponent groovyEditor;

	public GroovySecurityCheckPanel( GroovySecurityCheck securityCheck )
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
			return ( ( GroovySecurityCheck )getModelItem() ).getExecuteScript();
		}

		public void setScript( String text )
		{
			( ( GroovySecurityCheck )getModelItem() ).setExecuteScript( text );
		}
	}

	protected GroovyEditorComponent buildSetupScriptPanel( SecurityCheck securityCheck )
	{
		groovyEditor = new GroovyEditorComponent( new ScriptGroovyEditorModel( ( ( Assertable )securityCheck )
				.getModelItem() ), null );
		groovyEditor.setPreferredSize( new Dimension( 385, 150 ) );
		return groovyEditor;
	}

}
