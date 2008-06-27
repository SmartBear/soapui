/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.impl.wsdl.ResolveContext;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.ResolveContext.PathToResolve;
import com.eviware.soapui.impl.wsdl.ResolveContext.Resolver;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlProject
 * 
 * @author Ole.Matzura
 */

public class ResolveProjectAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "ResolveProjectAction";
	public static final MessageSupport messages = MessageSupport.getMessages( ResolveProjectAction.class );
	
	private JDialog dialog;
	private ResolveContextTableModel resolveContextTableModel;

	public ResolveProjectAction()
   {
      super( "Resolve", "Resolve local paths in this project" );
   }
	
   public void perform( WsdlProject project, Object param )
	{
   	ResolveContext context = new ResolveContext();
   	project.resolve( context );
   	if( context.isEmpty() )
   	{
   		UISupport.showInfoMessage( "No resolve problems found in project", "Resolve" );
   	}
   	else
   	{
   		if( dialog == null )
   		{
   			resolveContextTableModel = new ResolveContextTableModel( context );
   			buildDialog();
   		}
   		else
   		{
   			resolveContextTableModel.setContext( context );
   		}
   		
   		UISupport.centerDialog( dialog );
   		dialog.setVisible( true );
   	}
   }

	private void buildDialog()
	{
		dialog = new SimpleDialog( "Resolve Project", "Resolve unresolved paths in project", HelpUrls.RESOLVEPROJECT_HELP_URL )
		{
			@Override
			protected Component buildContent()
			{
				JPanel panel = new JPanel( new BorderLayout() );
				JXTable table = new JXTable( resolveContextTableModel );
				table.setHorizontalScrollEnabled(true);
				table.setDefaultRenderer(Resolver.class, new ResolverRenderer() );
				table.setDefaultEditor(Resolver.class, new ResolverEditor() );
				table.getColumn(2).setCellRenderer(new PathCellRenderer() );
				table.getColumn(3).setWidth(100);
				
				panel.add( new JScrollPane( table), BorderLayout.CENTER );
				panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
				
				return panel;
			}

			@Override
			protected boolean handleOk()
			{
				int cnt = resolveContextTableModel.getContext().apply();
				UISupport.showInfoMessage("Resolved " + cnt + " paths" );
				return true;
			}
		};
		
		dialog.setSize(550, 300);
	}
	
	private class ResolveContextTableModel extends AbstractTableModel
	{
		private ResolveContext context;

		public ResolveContextTableModel(ResolveContext context2)
		{
			context = context2;
		}

		public int getColumnCount()
		{
			return 4;
		}

		public void setContext(ResolveContext context)
		{
			this.context = context;
			fireTableDataChanged();
		}

		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
			case 0: return "Item";
			case 1: return "Description";
			case 2: return "Path";
			case 3: return "Action";
			}

			return super.getColumnName(column);
		}
		
		@Override
		public Class<?> getColumnClass(int arg0)
		{
			if( arg0 == 3 )
				return Resolver.class;
			else
				return String.class;
		}

		public int getRowCount()
		{
			return context.getPathsToResolve().size();
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 3 && context.getPathsToResolve().get( rowIndex ).getResolver() != null;
		}

		public Object getValueAt(int arg0, int arg1)
		{
			PathToResolve ptr = context.getPathsToResolve().get(arg0);
			switch( arg1 )
			{
			case 0 : return ptr.getOwner().getName();
			case 1 : return ptr.getDescription();
			case 2 : return ptr.getPath();
			case 3 : return ptr.getResolver();
			}
			
			return null;
		}

		public ResolveContext getContext()
		{
			return context;
		}
	}
	
	private class ResolverRenderer implements TableCellRenderer
	{
		private JButton button = new JButton( "Resolve..." );
		private JLabel label = new JLabel( "No Resolution" ); 
		
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
			return value == null ? label : button;
		}}
	
	private class ResolverEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
	{
		private Resolver resolver;
		private JButton button;

		private ResolverEditor()
		{
			button = new JButton( "Resolve..." );
			button.addActionListener(this);
		}

		public Component getTableCellEditorComponent(JTable arg0, Object arg1, boolean arg2, int arg3, int arg4)
		{
			this.resolver = (Resolver) arg1;
			return button;
		}

		public Object getCellEditorValue()
		{
			return resolver;
		}

		public void actionPerformed(ActionEvent arg0)
		{
			if( resolver.resolve() )
				resolveContextTableModel.fireTableDataChanged();
		}
	}
	
	private class PathCellRenderer extends DefaultTableCellRenderer
	{
		private Color greenColor = Color.GREEN.darker().darker();
		private Color redColor = Color.RED.darker().darker();
		
		@Override
		public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
				int arg5)
		{
			Component comp = super.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);
			
			PathToResolve ptr = resolveContextTableModel.getContext().getPathsToResolve().get(arg4);
			boolean resolved = ptr.getResolver() != null && ptr.getResolver().isResolved();
			
			if( resolved )
			{
				setForeground( greenColor );
				setText( ptr.getResolver().getResolvedPath() );
			}
			else
			{
				setForeground( redColor );
			}
			
			return comp;
		}
	}
}
