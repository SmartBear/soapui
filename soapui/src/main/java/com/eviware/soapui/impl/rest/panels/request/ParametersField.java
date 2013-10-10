package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.BorderLayout;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 10/9/13
 * Time: 9:46 AM
 * To change this template use File | Settings | File Templates.
 */
class ParametersField extends JPanel
{

	private final RestRequest request;
	private final JLabel textLabel;
	private final JTextField textField;
	private RestParamsTable paramsTable;
	private JPopupMenu popup;

	ParametersField( RestRequest request )
	{
		this.request = request;
		textLabel = new JLabel( "Parameters" );
		String queryParamsString = RestUtils.getQueryParamsString( request.getParams(), request );
		textField = new JTextField( queryParamsString );
		setToolTipText( queryParamsString );
		super.setLayout( new BorderLayout() );
		super.add( textLabel, BorderLayout.NORTH );
		super.add( textField, BorderLayout.SOUTH );
		addListeners();

	}

	private void addListeners()
	{
		//TODO: probably change to CaretListener, to select correct parameter
		textField.addCaretListener( new CaretListener()
		{

			@Override
			public void caretUpdate( CaretEvent e )
			{
				ParameterFinder finder = new ParameterFinder(textField.getText());
				openPopup( finder.findParameterAt( e.getDot() ) );
			}



		} );
	}

	public String getText()
	{
		return textField.getText();
	}

	public void setText( String text )
	{
		textField.setText( text );
		setToolTipText( text );
	}

	@Override
	public void setToolTipText( String text )
	{
		super.setToolTipText( text );
		textLabel.setToolTipText( text );
		textField.setToolTipText( text );
	}

	private void openPopup( String selectedParameter )
	{
		paramsTable = new RestParamsTable( request.getParams(), false, new RestParamsTableModel( request.getParams(), RestParamsTableModel.Mode.MINIMAL ),
				NewRestResourceActionBase.ParamLocation.METHOD, true, true );
		paramsTable.focusParameter(selectedParameter);
		popup = new JPopupMenu();
		popup.setLayout( new BorderLayout() );
		popup.add( paramsTable, BorderLayout.CENTER );
		popup.setInvoker( textField );
		popup.setLocation( SwingUtilities.convertPoint( textField, 3, getHeight() + 2, SoapUI.getFrame() ) );
		popup.setVisible( true );
	}

	public void closePopup()
	{
		if( popup != null )
		{
			popup.setVisible( false );
			popup = null;
			paramsTable = null;
		}
	}


}
