/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.support;

import com.eviware.soapui.impl.support.http.MediaType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MediaTypeComboBoxTest {
    MediaType model = mock(MediaType.class);

    @Test
    public void shouldSetSelectedItem() {
        String stuffMediaType = "application/stuff";
        when(model.getMediaType()).thenReturn(stuffMediaType);

        MediaTypeComboBox mediaTypeComboBox = new MediaTypeComboBox(model);

        assertThat((String) mediaTypeComboBox.getSelectedItem(), is(stuffMediaType));
    }

    @Test
    public void shouldBeEditable() {
        assertThat(new MediaTypeComboBox(model).isEditable(), is(true));
    }

    @Test
    public void shouldListenToChanges() {
        String fluffMediaType = "application/fluff";
        new MediaTypeComboBox(model).setSelectedItem(fluffMediaType);
        verify(model, atLeastOnce()).setMediaType(fluffMediaType);
    }
}
