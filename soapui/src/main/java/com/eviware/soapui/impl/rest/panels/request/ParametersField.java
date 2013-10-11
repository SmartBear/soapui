package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestUtils;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A component that displays matrix and query string parameters for a REST request and provides a popup to edit them.
 */
class ParametersField extends JPanel
{

	private final RestRequestInterface request;
	private final JLabel textLabel;
	private final JTextField textField;
	private PopupComponent popupComponent;
	private Popup popup;

	ParametersField( RestRequestInterface request )
	{
		this.request = request;
		textLabel = new JLabel( "Parameters" );
		String paramsString = RestUtils.makeSuffixParameterString( request );
		textField = new JTextField( paramsString);
		//textField.setEditable( false );
		setToolTipText( paramsString );
		super.setLayout( new BorderLayout() );
		super.add( textLabel, BorderLayout.NORTH );
		super.add( textField, BorderLayout.SOUTH );
		addListeners();

	}

	private void addListeners()
	{
		textField.addMouseListener( new MouseAdapter()
		{

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (popup != null)
				{
					return;
				}
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						ParameterFinder finder = new ParameterFinder( textField.getText() );
						//TODO: determine which letter has been clicked, preferably using CaretListener if possible
						openPopup( finder.findParameterAt( 0 ));
					}
				} );
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

	private void openPopup( final String selectedParameter )
	{
		final RestParamsTable restParamsTable = new RestParamsTable( request.getParams(), false, new RestParamsTableModel( request.getParams(), RestParamsTableModel.Mode.MINIMAL ),
				NewRestResourceActionBase.ParamLocation.METHOD, true, true );
		restParamsTable.addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyPressed( KeyEvent e )
			{
				if( e.getKeyChar() == KeyEvent.VK_ESCAPE )
				{
					closePopup();
				}
			}
		} );
		popupComponent = new PopupComponent(restParamsTable);
		Point displayPoint = SwingUtilities.convertPoint( textField, 3, getHeight() + 2, SoapUI.getFrame() );
		popup = PopupFactory.getSharedInstance().getPopup( null, popupComponent, (int)displayPoint.getX(), (int)displayPoint.getY() );
		//TODO: We have to choose the parent component as destination to get the setLocation work properly
		popup.show();
		SwingUtilities.windowForComponent( restParamsTable.getParamsTable() ).setFocusableWindowState( true );
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				restParamsTable.focusParameter( selectedParameter );
				System.out.println( textField.hasFocus() + " - " + restParamsTable.hasFocus() + " - " + restParamsTable.getParamsTable().hasFocus() + "-" + SwingUtilities.windowForComponent( restParamsTable ).hasFocus());
			}
		} );
		restParamsTable.addFocusListener( new FocusAdapter()
		{
			@Override
			public void focusLost( FocusEvent e )
			{
				System.out.println("Focus lost");
			}
		} );
	}

	public void closePopup()
	{
		if( popup != null )
		{
			popup.hide();
			popup = null;
		popupComponent = null;
		}
	}

	private class PopupComponent extends JPanel
	{

		private PopupComponent( RestParamsTable restParamsTable )
		{
			super( new BorderLayout() );
			JPanel buttonPanel = new JPanel(new FlowLayout( FlowLayout.CENTER ));
			JButton closeButton = new JButton( "Close" );
			closeButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					closePopup();
				}
			} );
			buttonPanel.add( closeButton );
			add( restParamsTable, BorderLayout.CENTER );
			add( buttonPanel, BorderLayout.SOUTH );
		}
	}


}
