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

package com.eviware.soapui.impl.rest.support;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests OverlayRestParamsPropertyHolder.
 *
 * @author : Shadid Chowdhury
 */
public class OverlayRestParamsPropertyHolderTest {
    OverlayRestParamsPropertyHolder propertyHolder;

    @Before
    public void setUP() {
        RestRequestParamsPropertyHolder.InternalRestParamProperty mockedParamProperty =
                mock(RestRequestParamsPropertyHolder.InternalRestParamProperty.class);

        RestParamsPropertyHolder overlay = mock(RestParamsPropertyHolder.class);
        when(overlay.containsKey("paramOverLay")).thenReturn(true).thenReturn(false);
        when(overlay.removeProperty("paramOverLay")).thenReturn(mockedParamProperty).thenReturn(null);

        RestParamsPropertyHolder parent = mock(RestParamsPropertyHolder.class);
        when(parent.containsKey("paramOverLay")).thenReturn(true).thenReturn(false);
        when(parent.removeProperty("paramParent")).thenReturn(mockedParamProperty).thenReturn(null);

        propertyHolder = new OverlayRestParamsPropertyHolder(parent, overlay);
    }

    @Test
    public void testRemoveProperty() throws Exception {
        assertNotNull(propertyHolder.removeProperty("paramOverLay"));
        assertNull(propertyHolder.removeProperty("paramOverLay"));
        assertNotNull(propertyHolder.removeProperty("paramParent"));
        assertNull(propertyHolder.removeProperty("paramParent"));
    }

    @After
    public void tearDown() {
        propertyHolder = null;
    }

}
