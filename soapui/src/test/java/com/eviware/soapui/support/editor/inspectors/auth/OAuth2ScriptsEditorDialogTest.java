package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.utils.ContainerWalker;
import com.eviware.soapui.utils.StubbedDialogsTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests of the dialog used to edit OAuth 2 automation JavaScripts.
 */
public class OAuth2ScriptsEditorDialogTest extends StubbedDialogsTestBase
{

	private boolean isDialogVisible;
	private OAuth2ScriptsEditor.Dialog editorDialog;
	private ContainerWalker walker;

	@Before
	public void setUp() throws Exception
	{
		editorDialog = new OAuth2ScriptsEditor.Dialog( Collections.<String>emptyList() ) {
			@Override
			public void setVisible( boolean b )
			{
				isDialogVisible = b;
			}
		};

		walker = new ContainerWalker( editorDialog );
	}

	@Test
	public void warnsUserIfAScriptIsInvalid() throws Exception
	{
		// this will simulate a user cancelling the confirm dialog that should pop up
		stubbedDialogs.mockConfirmWithReturnValue( false );

		editorDialog.setVisible( true );
		enterScript( 0, "this is an invalid script" );
		clickOnOkButton();
		assertThat( isDialogVisible, is(true));
	}

	@Test
	public void acceptInvalidScriptsIfUserConfirms() throws Exception
	{
		// this will simulate a user cancelling the confirm dialog that should pop up
		stubbedDialogs.mockConfirmWithReturnValue( true );

		editorDialog.setVisible(true);
		int index = 0;
		String script = "this is an invalid script";
		enterScript( index, script );
		clickOnOkButton();
		assertThat( isDialogVisible, is(false));
	}

	@Test
	public void acceptsInputIfScriptsAreValid() throws Exception
	{
		editorDialog.setVisible( true );
		enterScript( 0, "document.getElementById('usr').value = 'kalle'" );
		enterScript( 1, "document.getElementById('btn').click()" );
		clickOnOkButton();
		assertThat( isDialogVisible, is( false ) );
	}

	private void enterScript( int index, String script )
	{
		walker.findTextComponent( OAuth2ScriptsEditor.SCRIPT_NAMES[index]).setText( script );
	}

	private void clickOnOkButton()
	{
		walker.findButtonWithName( OAuth2ScriptsEditor.Dialog.OK_BUTTON_NAME ).doClick();
	}
}
