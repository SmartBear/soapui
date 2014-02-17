package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.actions.oauth.JavaScriptValidator;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
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
	static final String[] SCRIPT_NAMES = { "Login screen script", "Consent screen script"};

	private List<RSyntaxTextArea> scriptFields = new ArrayList<RSyntaxTextArea>(  );
	private JavaScriptValidator javaScriptValidator = new JavaScriptValidator();

	public OAuth2ScriptsEditor( List<String> currentScripts )
	{
		super( new GridLayout( 2, 1 ) );
		addScriptFields( currentScripts);
	}

	public List<String> getJavaScripts()
	{
		List<String> scripts = new ArrayList<String>(  );
		for( RSyntaxTextArea scriptField : scriptFields )
		{
			scripts.add(scriptField.getText());
		}
		return scripts;
	}

	private void addScriptFields( List<String> currentScripts )
	{
		int index = 0;
		for( String scriptName : SCRIPT_NAMES )
		{
			RSyntaxTextArea scriptField = SyntaxEditorUtil.createDefaultJavaScriptSyntaxTextArea();
			scriptField.setName(scriptName);
			if (currentScripts.size() > index)
			{
				scriptField.setText(currentScripts.get(index));
			}
			scriptFields.add(scriptField);
			add( new InputAreaWithHeader( scriptName, scriptField ) );
			index++;
		}
	}

	private class InputAreaWithHeader extends JPanel
	{
		public InputAreaWithHeader( String scriptName, RSyntaxTextArea scriptField )
		{
			super(new BorderLayout(  ));
			add( new JLabel(scriptName), BorderLayout.NORTH);
			add( new JScrollPane(scriptField), BorderLayout.CENTER);
		}
	}

	public static class Dialog extends JDialog
	{

		static final String OK_BUTTON_NAME = "okButton";

		private List<String> scriptsToReturn;



		public Dialog( Frame owner, String title, List<String> scripts)
		{
			super( owner, title, true );
			Container contentPane = getContentPane();
			final OAuth2ScriptsEditor inputPanel = new OAuth2ScriptsEditor( scripts );
			contentPane.setLayout( new BorderLayout(  ) );
			contentPane.add(inputPanel, BorderLayout.CENTER);
			JPanel buttonsPanel = new JPanel(new FlowLayout( FlowLayout.RIGHT ));
			JButton okButton = new JButton( "OK" );
			okButton.setName( OK_BUTTON_NAME );
			okButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					if (inputPanel.hasInvalidJavaScripts() && !UISupport.confirm(
							"One or more of the entered scripts you've entered seems to be incorrect.\n\n" +
									"Do you still want to apply it?", "Incorrect JavaScript", Dialog.this ))
					{
						return;
					}
					scriptsToReturn = inputPanel.getJavaScripts();
					closeDialog();
				}
			} );
			JButton cancelButton = new JButton( "Cancel" );
			cancelButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( ActionEvent e )
				{
					scriptsToReturn = null;
					closeDialog();
				}
			} );
			buttonsPanel.add( okButton );
			buttonsPanel.add(cancelButton);
			contentPane.add(buttonsPanel, BorderLayout.SOUTH);
			setBounds(500, 500, 600, 500);
		}

		public List<String> getScripts()
		{
			return scriptsToReturn;
		}

		private void closeDialog()
		{
			setVisible( false );
			dispose();
		}

		public Dialog(List<String> scripts)
		{
			this(null, "OAuth2 flow JavaScripts", scripts);
		}
	}

	private boolean hasInvalidJavaScripts()
	{
		for( RSyntaxTextArea scriptField : scriptFields )
		{
			if( !javaScriptValidator.validate( scriptField.getText() ) )
			{
				return true;
			}
		}
		return false;
	}
}
