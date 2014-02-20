package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.BrowserListenerAdapter;
import com.eviware.soapui.impl.rest.actions.oauth.JavaScriptValidationError;
import com.eviware.soapui.impl.rest.actions.oauth.JavaScriptValidator;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2Parameters;
import com.eviware.soapui.impl.rest.actions.oauth.OAuth2TokenExtractor;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	private List<RSyntaxTextArea> scriptFields = new ArrayList<RSyntaxTextArea>();
	private JavaScriptValidator javaScriptValidator = new JavaScriptValidator();
	private JPanel scriptsPanel;

	public OAuth2ScriptsEditor( final OAuth2Profile profile )
	{
		super( new BorderLayout() );
		JPanel buttonPanel = new JPanel(new FlowLayout( FlowLayout.RIGHT ));
		JButton testScriptsButton = new JButton( "Test scripts" );
		testScriptsButton.setName( TEST_SCRIPTS_BUTTON_NAME );
		testScriptsButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				testScripts( profile );
			}
		} );
		JButton addScriptButton = new JButton( "Add script" );
		addScriptButton.setName( ADD_SCRIPT_BUTTON_NAME );
		addScriptButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				addScript();
			}
		} );
		JButton removeScriptButton = new JButton( "Remove script" );
		removeScriptButton.setName( REMOVE_SCRIPT_BUTTON_NAME );
		removeScriptButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				removeScript();
			}
		} );
		buttonPanel.add( addScriptButton );
		buttonPanel.add( removeScriptButton );
		buttonPanel.add( testScriptsButton );
		add( buttonPanel, BorderLayout.NORTH );
		scriptsPanel = makeScriptsPanel( profile );
		add( new JScrollPane(scriptsPanel), BorderLayout.CENTER );
	}

	private void removeScript()
	{
		scriptFields.remove(scriptFields.size() - 1);
		scriptsPanel.remove(scriptsPanel.getComponentCount() - 1);
		scriptsPanel.revalidate();
	}

	private void addScript()
	{
		RSyntaxTextArea scriptField = SyntaxEditorUtil.createDefaultJavaScriptSyntaxTextArea();
		String fieldName = "Page " + ( scriptFields.size() + 1 );
		scriptField.setName( fieldName );
		scriptFields.add(scriptField);
		scriptsPanel.add(new InputAreaWithHeader( fieldName, scriptField ));
		scriptsPanel.revalidate();
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

	protected OAuth2TokenExtractor getExtractor()
	{
		return new OAuth2TokenExtractor();
	}

	private void testScripts( OAuth2Profile profile )
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
		JPanel scriptsPanel = new JPanel( new GridLayout( 0, 1, 15, 15 ) );
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
			scriptsPanel.add( new InputAreaWithHeader( scriptName, scriptField ) );
		}
		return scriptsPanel;
	}

	/*
	Private helper classes
	 */

	private class InputAreaWithHeader extends JPanel
	{
		public InputAreaWithHeader( String scriptName, RSyntaxTextArea scriptField )
		{
			super( new BorderLayout() );
			add( new JLabel( scriptName ), BorderLayout.NORTH );
			add( new JScrollPane( scriptField ), BorderLayout.CENTER );
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
