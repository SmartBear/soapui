package com.eviware.soapui.impl.support;

import java.util.Map;

import org.apache.xmlbeans.XmlObject;

public interface DefinitionContext
{
	boolean hasSchemaTypes();
	
	public Map<String, XmlObject> getDefinitionParts() throws Exception;
}
