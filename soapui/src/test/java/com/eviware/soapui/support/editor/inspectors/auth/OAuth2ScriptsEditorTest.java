package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.utils.ContainerWalker;
import com.eviware.soapui.utils.StubbedDialogsTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.eviware.soapui.utils.CommonMatchers.aCollectionWithSize;
import static com.eviware.soapui.utils.CommonMatchers.anEmptyCollection;
import static com.eviware.soapui.utils.ModelItemFactory.makeOAuth2Profile;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 *
 */
public class OAuth2ScriptsEditorTest extends StubbedDialogsTestBase
{

	private OAuth2ScriptsEditor editorWithoutScripts;
	private ContainerWalker containerWalker;

	@Before
	public void setUp() throws Exception
	{
		editorWithoutScripts = new OAuth2ScriptsEditor(createProfileWith( Collections.<String>emptyList() ) );
		containerWalker = new ContainerWalker( editorWithoutScripts );
	}

	@Test
	public void canBeInitializedWithoutExistingScripts() throws Exception
	{
		assertThat( editorWithoutScripts.getJavaScripts(), is(Arrays.asList( "", "")));
	}

	@Test
	public void getsJavaScriptsEnteredByUser() throws Exception
	{
		final String firstScript = "alert('first')";
		final String secondScript = "alert('second')";

		containerWalker.findTextComponent( OAuth2ScriptsEditor.SCRIPT_NAMES[0] ).setText( firstScript );
		containerWalker.findTextComponent( OAuth2ScriptsEditor.SCRIPT_NAMES[1] ).setText( secondScript );

		assertThat(editorWithoutScripts.getJavaScripts(), is(Arrays.asList(firstScript, secondScript)));
	}

	@Test
	public void showsErrorMessageWhenInvalidScriptIsTested() throws Exception
	{
		final String invalidScript = "this is clearly invalid";

		containerWalker.findTextComponent( OAuth2ScriptsEditor.SCRIPT_NAMES[0] ).setText( invalidScript );
		containerWalker.findButtonWithName( OAuth2ScriptsEditor.TEST_SCRIPTS_BUTTON_NAME ).doClick();

		List<String> errorMessages = stubbedDialogs.getErrorMessages();
		assertThat( errorMessages, is(aCollectionWithSize(1)));
		assertThat(errorMessages.get(0), containsString(invalidScript));
	}

	@Test
	public void showsNoErrorMessageWhenValidScriptIsTested() throws Exception
	{
		final String validScript = "alert('hej')";

		containerWalker.findTextComponent( OAuth2ScriptsEditor.SCRIPT_NAMES[0] ).setText( validScript );
		containerWalker.findButtonWithName( OAuth2ScriptsEditor.TEST_SCRIPTS_BUTTON_NAME ).doClick();

		List<String> errorMessages = stubbedDialogs.getErrorMessages();
		assertThat( errorMessages, is( anEmptyCollection() ));
	}



	@Test
	public void getsJavaScriptsFromInitialization() throws Exception
	{
		List<String> scripts = Arrays.asList("alert('hello')", "window.status='hello'");
		OAuth2ScriptsEditor editorWithExistingScripts = new OAuth2ScriptsEditor( createProfileWith( scripts ));
		assertThat( editorWithExistingScripts.getJavaScripts(), is(scripts));
	}

	private OAuth2Profile createProfileWith( List<String> scripts ) throws SoapUIException
	{
		OAuth2Profile profile = makeOAuth2Profile();
		profile.setJavaScripts( scripts );
		return profile;
	}
}
