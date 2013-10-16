package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Prakash
 * Date: 2013-10-15
 * Time: 13:02
 * To change this template use File | Settings | File Templates.
 */
public class AddParamAction extends AbstractAction
{
	private MutableTestPropertyHolder propertyHolder;
	private JTable parameterTable;
	private JPanel parent;


	private AddParamAction( String iconImage, String shortDescription )
	{
	}

	public static Builder builder()
	{
		return new Builder();
	}


	public void actionPerformed( ActionEvent e )
	{
		String name = "";
		propertyHolder.addProperty( name );

		final int row = propertyHolder.getPropertyNames().length - 1;
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				editTableCell( row, 0 );
				parameterTable.getModel().addTableModelListener( new TableModelListener()
				{
					@Override
					public void tableChanged( TableModelEvent e )
					{
						editTableCell( row, 1 );
						parameterTable.getModel().removeTableModelListener( this );
					}
				} );
			}
		} );
	}

	private void editTableCell( final int row, final int column )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				parent.requestFocusInWindow();
				parent.scrollRectToVisible( parameterTable.getCellRect( row, column, true ) );
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						parameterTable.editCellAt( row, column );
						Component editorComponent = parameterTable.getEditorComponent();
						if( editorComponent != null )
							editorComponent.requestFocusInWindow();
					}
				} );
			}
		} );
	}


	public static class Builder
	{
		private AddParamAction instance = new AddParamAction( "", "" );

		private Builder()
		{

		}

		public Builder withSmallIcon( String smallIcon )
		{
			instance.putValue( Action.SMALL_ICON, UISupport.createImageIcon( smallIcon ) );
			return this;
		}

		public Builder withShortDescription( String shortDescription )
		{
			instance.putValue( Action.SHORT_DESCRIPTION, shortDescription );
			return this;
		}

		public Builder forTable( JTable paramTable )
		{
			instance.parameterTable = paramTable;
			return this;
		}

		public Builder withParent( JPanel parentPanel )
		{
			instance.parent = parentPanel;
			return this;
		}

		public Builder withPropertyHolder( MutableTestPropertyHolder propertyHolder )
		{
			instance.propertyHolder = propertyHolder;
			return this;
		}

		public AddParamAction build()
		{
			assertNotEmpty( "iconImage", instance.getValue( SMALL_ICON ) );
			assertNotEmpty( "shortDescription", (String) instance.getValue( SHORT_DESCRIPTION ) );
			assertNotEmpty( "parameterTable", instance.parameterTable );
			assertNotEmpty( "parent", instance.parent );
			assertNotEmpty( "propertyHolder", instance.propertyHolder );

			return instance;
		}

		private void assertNotEmpty( String name, String value )
		{
			if( StringUtils.isNullOrEmpty( value ) )
			{
				throw new IllegalArgumentException( name + " should not be null/empty" );
			}
		}

		private void assertNotEmpty( String name, Object value )
		{
			if( value == null )
			{
				throw new IllegalArgumentException( name + " should not be null" );
			}
		}
	}
}
