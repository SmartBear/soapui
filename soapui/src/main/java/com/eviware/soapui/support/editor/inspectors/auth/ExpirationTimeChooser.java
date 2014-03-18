package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ExpirationTimeChooser extends JPanel
{

	static final String SERVER_EXPIRATION_RADIO_NAME = "serverExpirationRadio";
	static final String MANUAL_EXPIRATION_RADIO_NAME = "manualExpirationRadio";
	static final String TIME_FIELD_NAME = "timeField";
	static final String TIME_UNIT_COMBO_NAME = "timeUnitCombo";
	private static final TimeUnitOption[] TIME_UNIT_OPTIONS = new TimeUnitOption[] {
			new TimeUnitOption( 1, "Seconds" ), new TimeUnitOption( 60, "Minutes" ), new TimeUnitOption( 3600, "Hours" ) };

	private JRadioButton serverExpirationTimeOption;
	private JRadioButton manualExpirationTimeOption;
	private JTextField timeTextField;
	private JComboBox timeUnitCombo;
	private OAuth2Profile profile;

	ExpirationTimeChooser( OAuth2Profile profile )
	{
		this.profile = profile;
		setLayout( new BorderLayout( 0, 0 ) );
		initializeRadioButtons();
		JPanel timeSelectionPanel = createTimeSelectionPanel();
		JPanel northPanel = new JPanel( new GridLayout( 3, 1, 0, 0 ) );
		northPanel.add( serverExpirationTimeOption );
		northPanel.add( manualExpirationTimeOption );
		northPanel.add( timeSelectionPanel );
		add( northPanel, BorderLayout.NORTH );

		JPanel centerPanel = new JPanel( new BorderLayout( 0, 0 ) );
		JLabel label = new JLabel( "<html>Here you can set an expiry time if the OAuth 2.0 server doesn't,<br/>so that the token retrieval can be automated.</html>" );
		label.setForeground( new Color( 143, 143, 143 ) );
		centerPanel.add( label, BorderLayout.NORTH );
		add( centerPanel, BorderLayout.CENTER );

		JLabel helpLink = UISupport.createLabelLink("http://www.soapui.org", "Learn how to use the token expiration time ");
		add( helpLink, BorderLayout.SOUTH );
	}

	public long getAccessTokenExpirationTimeInSeconds()
	{
		TimeUnitOption unit = ( TimeUnitOption )timeUnitCombo.getSelectedItem();
		try
		{
			return serverExpirationTimeOption.isSelected() ? -1 : Integer.parseInt( timeTextField.getText() ) * unit.seconds;
		}
		catch( NumberFormatException e )
		{
			return -1;
		}
	}

	private JPanel createTimeSelectionPanel()
	{
		JPanel timeSelectionPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 3, 0 ) );

		boolean enableManualTimeControls = profile.useManualAccessTokenExpirationTime();

		timeUnitCombo = new JComboBox( TIME_UNIT_OPTIONS );
		timeUnitCombo.setName( TIME_UNIT_COMBO_NAME );
		timeUnitCombo.setEnabled( enableManualTimeControls );

		timeTextField = new JTextField( 5 );
		timeTextField.setName( TIME_FIELD_NAME );
		timeTextField.setHorizontalAlignment( JTextField.RIGHT );

		Long manualAccessTokenExpirationTime = profile.getManualAccessTokenExpirationTime();
		if( manualAccessTokenExpirationTime == null )
		{
			timeTextField.setText( "" );
		}
		else
		{
			TimeUnitOption appropriateUnit = getAppropriateTimeUnitOption( manualAccessTokenExpirationTime );
			timeTextField.setText( String.valueOf( manualAccessTokenExpirationTime / appropriateUnit.seconds ) );
			timeUnitCombo.setSelectedItem( appropriateUnit );
		}
		timeTextField.setEnabled( enableManualTimeControls );

		timeSelectionPanel.add( timeTextField );
		timeSelectionPanel.add( timeUnitCombo );

		return timeSelectionPanel;
	}

	private TimeUnitOption getAppropriateTimeUnitOption( long seconds )
	{
		if( seconds % 3600 == 0 )
		{
			return TIME_UNIT_OPTIONS[2]; // hours
		}
		else if( seconds % 60 == 0 )
		{
			return TIME_UNIT_OPTIONS[1]; // minutes
		}
		else
		{
			return TIME_UNIT_OPTIONS[0]; // seconds
		}
	}

	private void initializeRadioButtons()
	{
		long serverIssuedExpirationTime = profile.getAccessTokenExpirationTime();
		String serverIssuedExpirationTimeLabel;
		if( serverIssuedExpirationTime > 0 )
		{
			serverIssuedExpirationTimeLabel = getMostLegibleTimeString( serverIssuedExpirationTime );
		}
		else
		{
			serverIssuedExpirationTimeLabel = "No expiration";
		}

		serverExpirationTimeOption = new JRadioButton( "Use expiration time from access token: " + serverIssuedExpirationTimeLabel );
		serverExpirationTimeOption.setName( SERVER_EXPIRATION_RADIO_NAME );
		ActionListener checkBoxMonitor = new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				timeTextField.setEnabled( manualExpirationTimeOption.isSelected() );
				timeUnitCombo.setEnabled( manualExpirationTimeOption.isSelected() );
			}
		};
		serverExpirationTimeOption.addActionListener( checkBoxMonitor );

		manualExpirationTimeOption = new JRadioButton( "Manual" );
		manualExpirationTimeOption.setName( MANUAL_EXPIRATION_RADIO_NAME );
		manualExpirationTimeOption.addActionListener( checkBoxMonitor );

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( serverExpirationTimeOption );
		buttonGroup.add( manualExpirationTimeOption );

		if( profile.useManualAccessTokenExpirationTime() )
		{
			manualExpirationTimeOption.setSelected( true );
		}
		else
		{
			serverExpirationTimeOption.setSelected( true );
		}
	}

	private String getMostLegibleTimeString( long seconds )
	{
		if( seconds % 3600 == 0 )
		{
			return seconds / 3600 + " hour(s)";
		}
		else if( seconds % 60 == 0 )
		{
			return seconds / 60 + " minute(s)";
		}
		else
		{
			return seconds + " second(s)";
		}
	}

	private static class TimeUnitOption
	{
		public final int seconds;
		public final String name;

		private TimeUnitOption( int seconds, String name )
		{
			this.seconds = seconds;
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}

		public int getValue()
		{
			if( name.equals( "Hours" ) )
			{
				return seconds / 3600;
			}
			else if( name.equals( "Minutes" ) )
			{
				return seconds / 60;
			}
			else
			{
				return seconds;
			}
		}
	}
}
