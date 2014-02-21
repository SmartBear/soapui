package com.eviware.soapui.support;

import com.eviware.soapui.impl.support.http.MediaType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class MediaTypeComboBoxTest
{
	MediaType model = mock( MediaType.class );

	@Test
	public void shouldBeCreated()
	{

		new MediaTypeComboBox( model );
	}

	@Test
	public void shouldSetSelectedItem()
	{
		String stuffMediaType = "application/stuff";
		when( model.getMediaType() ).thenReturn( stuffMediaType );

		MediaTypeComboBox mediaTypeComboBox = new MediaTypeComboBox( model );

		assertThat( ( String )mediaTypeComboBox.getSelectedItem(), is( stuffMediaType ) );
	}

	@Test
	public void shouldBeEnabled()
	{
		assertThat( new MediaTypeComboBox( model ).isEnabled(), is( true ));
	}

	@Test
	public void shouldListenToChanges()
	{
		String fluffMediaType = "application/fluff";
		new MediaTypeComboBox( model ).setSelectedItem( fluffMediaType );
		verify( model, atLeastOnce() ).setMediaType( fluffMediaType );
	}
}
