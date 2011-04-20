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

import javax.swing.JPanel;

@SuppressWarnings( "serial" )
public class SensitiveInformationTablePanel extends JPanel
{

	// static final String SENSITIVE_INFORMATION_TOKEN =
	// "Sensitive Information Token";
	// protected SecurityParametersTableModel model;
	// protected JXToolBar toolbar;
	// protected JXTable table;
	// protected Map<String, TestProperty> properties;
	// protected DefaultActionList actionList;
	// protected JUndoableTextArea pathPane;
	// protected SimpleForm dialog;
	// protected AbstractSecurityCheckWithProperties securityCheck;
	//
	// private SimpleForm securityChecksForm;
	//
	//
	// public SimpleForm getForm()
	// {
	// if( securityChecksForm == null )
	// {
	// securityChecksForm = new SimpleForm();
	//
	// PropertyHolderTable propertyHolderTable =null;// new
	// PropertyHolderTable();
	//
	// propertyHolderTable.setPreferredSize( new Dimension( 200, 300 ) );
	//
	// securityChecksForm.append( new JLabel( SENSITIVE_INFORMATION_TOKEN ) );
	// securityChecksForm.addSpace();
	// securityChecksForm.addComponent( propertyHolderTable );
	// }
	//
	// return securityChecksForm;
	// }
	//
	// public SensitiveInformationTablePanel( SecurityParametersTableModel model,
	// Map<String, TestProperty> properties, AbstractSecurityCheckWithProperties
	// securityCheck )
	// {
	// this.securityCheck = securityCheck;
	// this.model = model;
	// initRequestPartProperties( properties );
	// init();
	// }
	//
	// private void initRequestPartProperties( Map<String, TestProperty>
	// properties )
	// {
	// this.properties = new HashMap<String, TestProperty>();
	// for( String key : properties.keySet() )
	// {
	// if( properties.get( key ).isRequestPart() )
	// this.properties.put( key, properties.get( key ) );
	// }
	// }
	//
	// protected void init()
	// {
	//
	// setLayout( new BorderLayout() );
	// toolbar = UISupport.createToolbar();
	//
	// toolbar.add( UISupport.createToolbarButton( new AddNewParameterAction() )
	// );
	// toolbar.add( UISupport.createToolbarButton( new RemoveParameterAction() )
	// );
	// toolbar.addGlue();
	//
	// add( toolbar, BorderLayout.NORTH );
	// table = new JXTable( model );
	//
	// table.setDefaultEditor( String.class, getDefaultCellEditor() );
	// add( new JScrollPane( table ), BorderLayout.CENTER );
	//
	// pathPane = new JUndoableTextArea();
	//
	// }
	//
	// /**
	// * this will return cell editor when editing xpath
	// *
	// * @return
	// */
	// protected TableCellEditor getDefaultCellEditor()
	// {
	// return new XPathCellRender();
	// }
	//
	// // public XFormDialog getDialog()
	// // {
	// // return dialog;
	// // }
	// //
	// // /*
	// // * Creates dialog
	// // */
	// // protected XFormDialog createAddParameterDialog()
	// // {
	// // actionList = new DefaultActionList();
	// // AddAction addAction = new AddAction();
	// // actionList.addAction( addAction, true );
	// // AddAndCopy addAndCopy = new AddAndCopy();
	// // actionList.addAction( addAndCopy );
	// // Close closeAction = new Close();
	// // actionList.addAction( closeAction );
	// //
	// // dialog = ADialogBuilder.buildDialog( AddParameterDialog.class,
	// actionList, false );
	// //
	// // dialog.getFormField( AddParameterDialog.PATH ).setProperty(
	// "component", buildPathSelector() );
	// //
	// // closeAction.setDialog( dialog );
	// // addAction.setDialog( dialog );
	// // addAndCopy.setDialog( dialog );
	// //
	// // final JTextFieldFormField labelField = ( JTextFieldFormField
	// )dialog.getFormField( AddParameterDialog.LABEL );
	// // labelField.getComponent().setColumns( 30 );
	// // labelField.setEnabled( false );
	// // JComboBoxFormField nameField = ( JComboBoxFormField
	// )dialog.getFormField( AddParameterDialog.NAME );
	// // enablePathField( false );
	// // nameField.addFormFieldListener( new XFormFieldListener()
	// // {
	// //
	// // @Override
	// // public void valueChanged( XFormField sourceField, String newValue,
	// String oldValue )
	// // {
	// // if( !newValue.equals( CHOOSE_TEST_PROPERTY ) )
	// // {
	// // labelField.setEnabled( true );
	// // enablePathField( true );
	// // }
	// // else
	// // {
	// // labelField.setEnabled( false );
	// // enablePathField( false );
	// // }
	// //
	// // }
	// // } );
	// // ArrayList<String> options = new ArrayList<String>();
	// // options.add( CHOOSE_TEST_PROPERTY );
	// // options.addAll( properties.keySet() );
	// // nameField.setOptions( options.toArray( new String[0] ) );
	// //
	// // ( ( JFormDialog )dialog ).getDialog().setResizable( false );
	// //
	// // return dialog;
	// // }
	//
	// protected JPanel buildPathSelector()
	// {
	// JPanel sourcePanel = new JPanel( new BorderLayout() );
	// sourcePanel.add( new JScrollPane( pathPane ), BorderLayout.CENTER );
	// sourcePanel.setBorder( BorderFactory.createEmptyBorder( 0, 3, 3, 3 ) );
	// return sourcePanel;
	// }
	//
	// /**
	// * @param pathField
	// */
	// protected void enablePathField( boolean enable )
	// {
	// pathPane.setEnabled( enable );
	// }
	//
	// class AddNewParameterAction extends AbstractAction
	// {
	//
	// public AddNewParameterAction()
	// {
	// putValue( Action.SMALL_ICON, UISupport.createImageIcon(
	// "/add_property.gif" ) );
	// putValue( Action.SHORT_DESCRIPTION, "Adds a parameter to security check"
	// );
	// }
	//
	// @Override
	// public void actionPerformed( ActionEvent arg0 )
	// {
	// XFormDialog dialog = createAddParameterDialog();
	// dialog.show();
	// model.fireTableDataChanged();
	// }
	//
	// }
	//
	// class RemoveParameterAction extends AbstractAction
	// {
	//
	// public RemoveParameterAction()
	// {
	// putValue( Action.SMALL_ICON, UISupport.createImageIcon(
	// "/remove_property.gif" ) );
	// putValue( Action.SHORT_DESCRIPTION,
	// "Removes parameter from security check" );
	// }
	//
	// @Override
	// public void actionPerformed( ActionEvent e )
	// {
	// model.removeRows( table.getSelectedRows() );
	// model.fireTableDataChanged();
	// }
	//
	// }
	//
	// public class AddAndCopy extends AbstractAction
	// {
	//
	// private XFormDialog dialog;
	//
	// public AddAndCopy()
	// {
	// super( "Add&Copy" );
	// }
	//
	// public void setDialog( XFormDialog dialog )
	// {
	// this.dialog = dialog;
	// }
	//
	// @Override
	// public void actionPerformed( ActionEvent e )
	// {
	// if( dialog.getValue( AddParameterDialog.LABEL ) == null
	// || dialog.getValue( AddParameterDialog.LABEL ).trim().length() == 0 )
	// {
	// UISupport.showErrorMessage( "Label is required!" );
	// }
	// else
	// {
	// if( !model.addParameter( dialog.getValue( AddParameterDialog.LABEL ),
	// dialog
	// .getValue( AddParameterDialog.NAME ), pathPane.getText() ) )
	// UISupport.showErrorMessage( "Label have to be unique!" );
	// }
	// }
	//
	// }
	//
	// private class Close extends AbstractAction
	// {
	//
	// private XFormDialog dialog;
	//
	// public Close()
	// {
	// super( "Close" );
	// }
	//
	// public void setDialog( XFormDialog dialog )
	// {
	// this.dialog = dialog;
	// }
	//
	// @Override
	// public void actionPerformed( ActionEvent e )
	// {
	// if( dialog != null )
	// {
	// ( ( SwingXFormDialog )dialog ).setReturnValue( XFormDialog.CANCEL_OPTION
	// );
	//
	// JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField(
	// AddParameterDialog.NAME );
	// nameField.setSelectedOptions( new Object[] { nameField.getOptions()[0] }
	// );
	// dialog.setValue( AddParameterDialog.LABEL, "" );
	// pathPane.setText( "" );
	//
	// dialog.setVisible( false );
	// }
	//
	// }
	//
	// }
	//
	// class CopyParameterAction extends AbstractAction
	// {
	//
	// public CopyParameterAction()
	// {
	// putValue( Action.SMALL_ICON, UISupport.createImageIcon(
	// "/clone_request.gif" ) );
	// putValue( Action.SHORT_DESCRIPTION, "Copies parameter" );
	// }
	//
	// @Override
	// public void actionPerformed( ActionEvent e )
	// {
	// if( table.getSelectedRow() > -1 )
	// {
	// XFormDialog dialog = createAddParameterDialog();
	//
	// int row = table.getSelectedRow();
	// initDialogForCopy( dialog, row );
	//
	// dialog.show();
	// model.fireTableDataChanged();
	// }
	// }
	//
	// }
	//
	// private void initDialogForCopy( XFormDialog dialog, int row )
	// {
	// dialog.setValue( AddParameterDialog.LABEL, ( String )model.getValueAt(
	// row, 0 ) );
	// dialog.setValue( AddParameterDialog.NAME, ( String )model.getValueAt( row,
	// 1 ) );
	// pathPane.setText( ( String )model.getValueAt( row, 2 ) );
	// }
	//
	// public JUndoableTextArea getPathPane()
	// {
	// return pathPane;
	// }
	//
	// private class AddAction extends AbstractAction
	// {
	//
	// private XFormDialog dialog;
	//
	// public AddAction()
	// {
	// super( "Add" );
	// }
	//
	// public void setDialog( XFormDialog dialog )
	// {
	// this.dialog = dialog;
	// }
	//
	// @Override
	// public void actionPerformed( ActionEvent arg0 )
	// {
	// if( dialog.getValue( AddParameterDialog.LABEL ) == null
	// || dialog.getValue( AddParameterDialog.LABEL ).trim().length() == 0 )
	// {
	// UISupport.showErrorMessage( "Label is required!" );
	// }
	// else
	// {
	// if( model.addParameter( dialog.getValue( AddParameterDialog.LABEL ),
	// dialog
	// .getValue( AddParameterDialog.NAME ), pathPane.getText() ) )
	// {
	// JComboBoxFormField nameField = ( JComboBoxFormField )dialog.getFormField(
	// AddParameterDialog.NAME );
	// nameField.setSelectedOptions( new Object[] { nameField.getOptions()[0] }
	// );
	// dialog.setValue( AddParameterDialog.LABEL, "" );
	// pathPane.setText( "" );
	// }
	// else
	// UISupport.showErrorMessage( "Label have to be unique!" );
	// }
	// }
	//
	// }
	//
	// public JUndoableTextField getLabel()
	// {
	// return ( ( JTextFieldFormField )dialog.getFormField(
	// AddParameterDialog.LABEL ) ).getComponent();
	// }
	//
	// @AForm( description = "Add New Security Test Step Parameter", name =
	// "Configure Security Test Step Parameters" )
	// interface AddParameterDialog
	// {
	// @AField( description = "Parameter Name", name = "Parameter Name", type =
	// AFieldType.ENUMERATION )
	// static String NAME = "Parameter Name";
	//
	// @AField( description = "Parameter Label", name = "Parameter Label", type =
	// AFieldType.STRING )
	// static String LABEL = "Parameter Label";
	//
	// @AField( description = "Parameter XPath", name = "XPath", type =
	// AFieldType.COMPONENT )
	// static String PATH = "XPath";
	// }

}
