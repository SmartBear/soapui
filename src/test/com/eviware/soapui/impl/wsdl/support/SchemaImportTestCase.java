package com.eviware.soapui.impl.wsdl.support;

import java.io.File;

import org.apache.xmlbeans.SchemaTypeLoader;

import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;

import junit.framework.TestCase;

public class SchemaImportTestCase extends TestCase
{
	public void testImport() throws Exception
	{
		String url = new File( "C:\\Documents and Settings\\ole\\My Documents\\eviware\\support\\soapUI Schemas\\wsdl\\PIXManager.wsdl" ).toURI().toURL().toString();
		SchemaTypeLoader schemaTypes = SchemaUtils.loadSchemaTypes( url, SoapVersion.Soap11, new UrlWsdlLoader( url ) );
	}
}
