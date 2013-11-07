package com.eviware.soapui.support.scripting.groovy;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.utils.ModelItemFactory;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Test verifying that the Groovy support in SoapUI is working
 */
public class GroovyScriptEngineFactoryTest
{


	@Test
	public void supportsGroovy2() throws Exception
	{
		DefaultSoapUICore soapUICore = new DefaultSoapUICore();
		DefaultSoapUICore.log = mock(Logger.class);
		SoapUI.setSoapUICore( soapUICore, true );
		GroovyScriptEngineFactory scriptEngineFactory = new GroovyScriptEngineFactory(  );
		SoapUIScriptEngine scriptEngine = scriptEngineFactory.createScriptEngine( ModelItemFactory.makeWsdlProject() );
		// binary literals are new to Groovy 2
		scriptEngine.setScript("assert 0b10 == 2");
		scriptEngine.run();

	}
}
