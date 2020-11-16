/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.support.scripting.groovy;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Test verifying that the Groovy support in SoapUI is working
 */
public class GroovyScriptEngineFactoryTest {


    @Test
    public void supportsGroovy2() throws Exception {
        DefaultSoapUICore soapUICore = new DefaultSoapUICore();
        DefaultSoapUICore.log = mock(Logger.class);
        SoapUI.setSoapUICore(soapUICore, true);
        GroovyScriptEngineFactory scriptEngineFactory = new GroovyScriptEngineFactory();
        SoapUIScriptEngine scriptEngine = scriptEngineFactory.createScriptEngine(ModelItemFactory.makeWsdlProject());
        // binary literals are new to Groovy 2
        scriptEngine.setScript("assert 0b10 == 2");
        scriptEngine.run();

    }
}
