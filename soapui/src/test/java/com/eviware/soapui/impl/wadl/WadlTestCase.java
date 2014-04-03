/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.impl.wadl;

import static org.junit.Assert.assertNotNull;

import junit.framework.JUnit4TestAdapter;
import net.java.dev.wadl.x2009.x02.ApplicationDocument;
import net.java.dev.wadl.x2009.x02.ApplicationDocument.Application;

import org.junit.Test;

public class WadlTestCase {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(WadlTestCase.class);
    }

    @Test
    public void testWadl() throws Exception {
        ApplicationDocument applicationDocument = ApplicationDocument.Factory.newInstance();
        Application application = applicationDocument.addNewApplication();
        assertNotNull(application);
    }
}
