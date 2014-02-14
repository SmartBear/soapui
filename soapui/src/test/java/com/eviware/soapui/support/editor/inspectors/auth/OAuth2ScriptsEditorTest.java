package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.utils.ContainerWalker;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class OAuth2ScriptsEditorTest
{
	@Test
	public void getsJavaScriptsFromInitialization() throws Exception
	{
		List<String> scripts = Arrays.asList("alert('hello')", "window.status='hello'");
		OAuth2ScriptsEditor editorWithExistingScripts = new OAuth2ScriptsEditor( scripts );
		assertThat( editorWithExistingScripts.getJavaScripts(), is(scripts));
	}

	@Test
	public void canBeInitializedWithoutExistingScripts() throws Exception
	{
		OAuth2ScriptsEditor editorWithoutScripts = new OAuth2ScriptsEditor( Collections.<String>emptyList() );
		assertThat( editorWithoutScripts.getJavaScripts(), is(Arrays.asList( "", "")));
	}

	@Test
	public void getsJavaScriptsEnteredByUser() throws Exception
	{
		final String firstScript = "alert('first')";
		final String secondScript = "alert('second')";
		OAuth2ScriptsEditor editorWithoutScripts = new OAuth2ScriptsEditor( Collections.<String>emptyList() );
		ContainerWalker walker = new ContainerWalker( editorWithoutScripts );

		walker.findTextComponent( OAuth2ScriptsEditor.SCRIPT_NAMES[0]).setText( firstScript );
		walker.findTextComponent( OAuth2ScriptsEditor.SCRIPT_NAMES[1]).setText( secondScript );

		assertThat(editorWithoutScripts.getJavaScripts(), is(Arrays.asList(firstScript, secondScript)));
	}
}
