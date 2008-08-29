package com.eviware.soapui.impl.wsdl.support;

import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import junit.framework.TestCase;
import org.apache.xmlbeans.SchemaTypeLoader;

import java.io.File;

public class SchemaImportTestCase extends TestCase
{
	public void testImport() throws Exception
	{
		String url = new File( "C:\\Documents and Settings\\ole\\My Documents\\eviware\\support\\soapUI Schemas\\wsdl\\PIXManager.wsdl" ).toURI().toURL().toString();
		SchemaTypeLoader schemaTypes = SchemaUtils.loadSchemaTypes( url,  new UrlWsdlLoader( url ) );
	}
}
