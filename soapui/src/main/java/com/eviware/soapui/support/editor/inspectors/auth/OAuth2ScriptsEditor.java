package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.BrowserListenerAdapter;
import com.eviware.soapui.impl.rest.actions.oauth.JavaScriptValidationError;
import com.eviware.soapui.impl.rest.actions.oauth.JavaScriptValidator;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2Parameters;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TokenExtractor;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Component that allows a user to edit the JavaScript snippets associated with an OAuth 2 flow.
 */
public class OAuth2ScriptsEditor extends JPanel
{
	public static final String TEST_SCRIPTS_BUTTON_NAME = "testScriptsButton";
	public static final String ADD_SCRIPT_BUTTON_NAME = "addScriptButton";
	public static final String REMOVE_SCRIPT_BUTTON_NAME = "removeScriptButton";

	static final String[] DEFAULT_SCRIPT_NAMES = { "Page 1 (e.g. login screen)", "Page 2 (e.g. consent screen)" };
	private static final String HELP_LINK_TEXT = "How to automate the process of getting an access token";
	public static final String HELP_LINK_URL = "http://soapui.org";

	private List<InputPanel> inputPanels = new ArrayList<InputPanel>();
	private InputPanel selectedInputField = null;
	private List<RSyntaxTextArea> scriptFields = new ArrayList<RSyntaxTextArea>();
	private JavaScriptValidator javaScriptValidator = new JavaScriptValidator();
	private JPanel scriptsPanel;
	private JButton removeScriptButton;
	private OAuth2Profile profile;

	public OAuth2ScriptsEditor( final OAuth2Profile profile )
	{
		super( new BorderLayout() );
		this.profile = profile;
		add( buildToolbar( profile ), BorderLayout.NORTH );
		makeScriptsPanel( profile );
		add( new JScrollPane(scriptsPanel), BorderLayout.CENTER );
		JPanel linkPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		linkPanel.add( makeLabelLink( HELP_LINK_URL, HELP_LINK_TEXT));
		add( linkPanel, BorderLayout.SOUTH );
	}

	private JXToolBar buildToolbar( final OAuth2Profile profile )
	{
		JXToolBar toolbar = UISupport.createToolbar();
		JButton testScriptsButton = UISupport.createToolbarButton( new TestScriptsAction( profile ) );
		testScriptsButton.setName(TEST_SCRIPTS_BUTTON_NAME);
		toolbar.addFixed( testScriptsButton );
		JButton addScriptButton = UISupport.createToolbarButton( new AddScriptAction() );
		addScriptButton.setName( ADD_SCRIPT_BUTTON_NAME );
		toolbar.addFixed( addScriptButton );
		removeScriptButton = UISupport.createToolbarButton( new RemoveScriptAction() );
		removeScriptButton.setName( REMOVE_SCRIPT_BUTTON_NAME );
		toolbar.addFixed( removeScriptButton );
		return toolbar;
	}

	public List<String> getJavaScripts()
	{
		List<String> scripts = new ArrayList<String>();
		for( RSyntaxTextArea scriptField : scriptFields )
		{
			scripts.add( scriptField.getText() );
		}
		return scripts;
	}

	/*
	Helper methods
	 */

	protected OAuth2TokenExtractor getExtractor()
	{
		return new OAuth2TokenExtractor();
	}

	void selectField( InputPanel field )
	{
		selectedInputField = field;
		for( InputPanel inputPanel : inputPanels )
		{
			if (inputPanel == field)
			{
				inputPanel.highlight();
			}
			else
			{
				inputPanel.removeHighlight();
			}
		}
		removeScriptButton.setEnabled( selectedInputField != null );
	}

	private JPanel makeScriptsPanel( final OAuth2Profile profile )
	{
		DocumentListener scriptUpdater = new DocumentListenerAdapter()
		{
			@Override
			public void update( Document document )
			{
				profile.setAutomationJavaScripts( getJavaScripts() );
			}
		};
		List<String> currentScripts = profile.getAutomationJavaScripts();
		scriptsPanel = new JPanel();
		scriptsPanel.setLayout( new BoxLayout( scriptsPanel, BoxLayout.Y_AXIS ) );
		scriptsPanel.setBackground( Color.WHITE );
		int numberOfFields = Math.max( 2, currentScripts.size() );
		for( int index = 0; index < numberOfFields; index++)
		{
			RSyntaxTextArea scriptField = SyntaxEditorUtil.createDefaultJavaScriptSyntaxTextArea();
			String scriptName = (index < DEFAULT_SCRIPT_NAMES.length ? DEFAULT_SCRIPT_NAMES[index] : "Page " + ( index + 1 ));
			scriptField.setName( scriptName );
			if( currentScripts.size() > index )
			{
				scriptField.setText( currentScripts.get( index ) );
			}
			scriptField.getDocument().addDocumentListener( scriptUpdater );
			scriptFields.add( scriptField );
			InputPanel inputPanel = new InputPanel( scriptName, scriptField );
			inputPanel.setName( "Input panel " + ( index + 1 ) );
			inputPanels.add( inputPanel );
			scriptsPanel.add( inputPanel );
		}
		JPanel parentPanel = new JPanel(new BorderLayout(  ));
		parentPanel.setBorder(new CompoundBorder(new LineBorder( Color.BLACK ),new EmptyBorder( 15, 15, 15, 15 )));
		parentPanel.add(scriptsPanel, BorderLayout.CENTER);
		return parentPanel;
	}

	// TODO: create a common utility method for this after merging to next
	private JLabel makeLabelLink( final String url, String labelText )
	{
		JLabel oAuthDocumentationLink = new JLabel( labelText );
		oAuthDocumentationLink.setForeground( Color.BLUE );
		oAuthDocumentationLink.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( MouseEvent e )
			{
				Tools.openURL( url );
			}
		} );
		oAuthDocumentationLink.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		return oAuthDocumentationLink;
	}

	private void showErrorMessage( String message )
	{
		if( message.length() > UISupport.EXTENDED_ERROR_MESSAGE_THRESHOLD )
		{
			UISupport.showErrorMessage( message.replaceAll( "\r\n", "<br/>" ) );
		}
		else
		{
			UISupport.showErrorMessage( message );
		}
	}

	/*
	Private helper classes
	 */

	private class AddScriptAction extends AbstractAction
	{
		private AddScriptAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Add script field" );
			putValue( LONG_DESCRIPTION, "Adds a new script input field" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			RSyntaxTextArea scriptField = SyntaxEditorUtil.createDefaultJavaScriptSyntaxTextArea();
			int index = scriptFields.size() + 1;
			String fieldName = "Page " + index;
			scriptField.setName( fieldName );
			scriptFields.add(scriptField);
			InputPanel inputPanel = new InputPanel( fieldName, scriptField );
			inputPanel.setName( "Input panel " + index );
			inputPanels.add(inputPanel);
			scriptsPanel.add( inputPanel, -1 );
			scriptsPanel.revalidate();
			scriptsPanel.repaint();
		}
	}

	private class RemoveScriptAction extends AbstractAction
	{
		private RemoveScriptAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Remove script field" );
			putValue( LONG_DESCRIPTION, "Removes the last script input field" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( UISupport.confirm("Do you really want to remove the script '" + selectedInputField.scriptField.getName() + "'",
					"Remove script", OAuth2ScriptsEditor.this ) )
			{
				scriptFields.remove(selectedInputField.scriptField);
				inputPanels.remove(selectedInputField);
				scriptsPanel.remove(selectedInputField);
				selectedInputField = null;
				scriptsPanel.revalidate();
				scriptsPanel.repaint();
				selectField( null );
				profile.setAutomationJavaScripts( getJavaScripts() );
			}
		}

		@Override
		public boolean isEnabled()
		{
			return selectedInputField != null;
		}
	}

	private class TestScriptsAction extends AbstractAction
	{
		private OAuth2Profile profile;

		private TestScriptsAction( OAuth2Profile profile )
		{
			this.profile = profile;
			putValue( SMALL_ICON, UISupport.createImageIcon( "/submit_request.gif" ) );
			putValue( SHORT_DESCRIPTION,  "Test scripts");
			putValue( LONG_DESCRIPTION, "Validates the scripts and tries to execute them in a browser");
		}

		public void actionPerformed( ActionEvent e )
		{
			boolean errorsFound = false;
			for( RSyntaxTextArea scriptField : scriptFields )
			{
				String script = scriptField.getText();
				JavaScriptValidationError validate = javaScriptValidator.validate( script );
				if( validate != null )
				{
					showErrorMessage( "The following script is invalid:\r\n" + script +
							"\r\n\r\nError:<br/>" + validate.getErrorMessage() );
					errorsFound = true;
				}
			}
			if( !errorsFound )
			{
				OAuth2TokenExtractor extractor = getExtractor();
				extractor.addBrowserListener( new JavaScriptErrorReporter() );
				OAuth2Parameters parameters = new OAuth2Parameters( profile );
				try
				{
					extractor.extractAccessToken( parameters );
				}
				catch( Exception ignore )
				{

				}


			}
		}
	}


	class InputPanel extends JPanel
	{
		private RSyntaxTextArea scriptField;

		private final Color originalBackground;

		public InputPanel( String scriptName, RSyntaxTextArea scriptField )
		{
			super( new BorderLayout(20, 20) );
			this.scriptField = scriptField;
			add( new JLabel( scriptName ), BorderLayout.NORTH );
			add( new JScrollPane( scriptField ), BorderLayout.CENTER );
			MouseAdapter selectionHandler = new MouseAdapter()
			{
				@Override
				public void mouseClicked( MouseEvent e )
				{
					if( selectedInputField == InputPanel.this )
					{
						if( e.getSource() == InputPanel.this )
						{
							selectField( null );
						}
					}
					else
					{
						selectField( InputPanel.this );
					}
				}
			};
			addMouseListener( selectionHandler );
			setBorder( BorderFactory.createLineBorder( Color.WHITE ) );
			originalBackground = getBackground();
		}

		@Override
		public Dimension getMaximumSize()
		{
			Dimension size = super.getMaximumSize();
			size.height = 300;
			return size;
		}

		public void highlight()
		{
			setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
			setBackground( aDarkerShadeThan( originalBackground ) );
		}

		private Color aDarkerShadeThan( Color color )
		{
			return new Color((int)( color.getRed() * .9),
					(int)( color.getBlue() * .9), (int)( color.getGreen() * .9));
		}

		public void removeHighlight()
		{
			setBorder( BorderFactory.createLineBorder( Color.WHITE ) );
			setBackground( originalBackground );
		}
		@Override
		public void setBorder( Border border )
		{
			super.setBorder( new CompoundBorder( border, new EmptyBorder( 20, 20, 20, 20 ) ) );
		}

	}


	private class JavaScriptErrorReporter extends BrowserListenerAdapter
	{

		private boolean hasErrors = false;

		@Override
		public void javaScriptErrorOccurred( final String script, final String location, final Exception error )
		{
			hasErrors = true;
			// invokeLater() is necessary, because the call comes from the JavaFX invoker thread
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					showErrorMessage( "The following script failed:\r\n" + script + "\r\nPage URL: " + location + "\r\nError:\r\n" +
							error.getMessage() + "]" );
				}
			} );
		}

		@Override
		public void browserClosed()
		{
			if( !hasErrors )
			{
				// invokeLater() is necessary, because the call comes from the JavaFX invoker thread
				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						UISupport.showInfoMessage( "All scripts executed correctly." );
					}
				} );
			}
		}
	}
}
