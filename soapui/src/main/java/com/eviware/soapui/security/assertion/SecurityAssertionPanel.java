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
package com.eviware.soapui.security.assertion;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.eviware.soapui.impl.wsdl.panels.teststeps.AssertionsPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;

public class SecurityAssertionPanel extends AssertionsPanel
{
	public SecurityAssertionPanel( Assertable assertable )
	{
		super( assertable );
	}

	@Override
	protected void initListAndModel()
	{
		assertionListModel = new SecurityAssertionListModel();
		assertionList = new JList( assertionListModel );
		assertionList.setToolTipText( "Assertions for this security scan." );
		assertionList.setCellRenderer( new SecurityAssertionCellRenderer() );
	}

	protected class SecurityAssertionListModel extends AssertionListModel
	{
		protected void addAssertion( TestAssertion assertion )
		{
			assertion.addPropertyChangeListener( this );
			items.add( assertion );
		}
	}

	private class SecurityAssertionCellRenderer extends JLabel implements ListCellRenderer
	{

		@Override
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus )
		{
			setEnabled( list.isEnabled() );

			TestAssertion assertion = ( TestAssertion )value;
			setText( assertion.getLabel() );

			if( assertion.isDisabled() && isEnabled() )
				setEnabled( false );

			if( isSelected )
			{
				setBackground( list.getSelectionBackground() );
				setForeground( list.getSelectionForeground() );
			}
			else
			{
				setBackground( list.getBackground() );
				setForeground( list.getForeground() );
			}

			setFont( list.getFont() );
			setOpaque( true );

			return this;
		}

	}

	public String getHelpUrl()
	{
		return HelpUrls.SECURITY_ASSERTION_HELP;
	}

}
