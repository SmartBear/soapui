/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * WSDL related settings constants
 * 
 * @author Emil.Breding
 */

public interface WsdlSettings
{
	@Setting( name = "Cache WSDLs", description = "caches and associated WSDLs locally for offline access and improved performance", type = SettingType.BOOLEAN )
	public final static String CACHE_WSDLS = WsdlSettings.class.getSimpleName() + "@" + "cache-wsdls";

	@Setting( name = "Sample Values", description = "generate example values in new requests", type = SettingType.BOOLEAN )
	public final static String XML_GENERATION_TYPE_EXAMPLE_VALUE = WsdlSettings.class.getSimpleName() + "@"
			+ "xml-generation-type-example-value";

	@Setting( name = "Type Comment", description = "generate comments with type information in new requests", type = SettingType.BOOLEAN )
	public final static String XML_GENERATION_TYPE_COMMENT_TYPE = WsdlSettings.class.getSimpleName() + "@"
			+ "xml-generation-type-comment-type";

	@Setting( name = "Include Optional", description = "always include optional schema elements when creating requests", type = SettingType.BOOLEAN )
	public final static String XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS = WsdlSettings.class.getSimpleName()
			+ "@" + "xml-generation-always-include-optional-elements";

	@Setting( name = "Pretty Print", description = "pretty print response messages", type = SettingType.BOOLEAN )
	public final static String PRETTY_PRINT_RESPONSE_MESSAGES = WsdlSettings.class.getSimpleName() + "@"
			+ "pretty-print-response-xml";

	@Setting( name = "Attachment Parts", description = "generate rpc message parts for attachments", type = SettingType.BOOLEAN )
	public final static String ATTACHMENT_PARTS = WsdlSettings.class.getSimpleName() + "@" + "attachment-parts";

	@Setting( name = "No Content-Type Validation", description = "allow incorrect content-types in mime-attachments", type = SettingType.BOOLEAN )
	public final static String ALLOW_INCORRECT_CONTENTTYPE = WsdlSettings.class.getSimpleName() + "@"
			+ "allow-incorrect-contenttype";

	public final static String ENABLE_MTOM = WsdlSettings.class.getSimpleName() + "@" + "enable-mtom";

	@Setting( name = "Schema Directory", description = "local directory containing schemas that should be added to loading/validation", type = SettingType.FOLDER )
	public static final String SCHEMA_DIRECTORY = WsdlSettings.class.getSimpleName() + "@" + "schema-directory";

	@Setting( name = "Name with Binding", description = "uses the WSDL binding name (instead of portType) for imported Interfaces", type = SettingType.BOOLEAN )
	public final static String NAME_WITH_BINDING = WsdlSettings.class.getSimpleName() + "@" + "name-with-binding";

	@Setting( name = "Excluded types", description = "types to exclude in request generation", type = SettingType.STRINGLIST )
	public final static String EXCLUDED_TYPES = WsdlSettings.class.getSimpleName() + "@" + "excluded-types";

	@Setting( name = "Strict schema types", description = "fails schema imports if types/particles are redefined", type = SettingType.BOOLEAN )
	public final static String STRICT_SCHEMA_TYPES = WsdlSettings.class.getSimpleName() + "@" + "strict-schema-types";

	@Setting( name = "Compression Limit", description = "minimum message size to compress", type = SettingType.INT )
	public final static String COMPRESSION_LIMIT = WsdlSettings.class.getSimpleName() + "@" + "compression-limit";

	@Setting( name = "Pretty Print Project Files", description = "pretty prints project files", type = SettingType.BOOLEAN )
	public final static String PRETTY_PRINT_PROJECT_FILES = WsdlSettings.class.getSimpleName() + "@"
			+ "pretty-print-project-files";

	public static final String XML_GENERATION_SKIP_COMMENTS = WsdlSettings.class.getSimpleName() + "@"
			+ "xml-generation-skip-comments";

	// @Setting( name="Always Attachments",
	// description="always allow attachments despite of definition",
	// type=SettingType.BOOLEAN )
	// public final static String ALWAYS_ALLOW_ATTACHMENTS =
	// WsdlSettings.class.getSimpleName() + "@" + "always-allow-attachments";

	// public enum XmlGenerationType {
	// EMPTY,
	// TYPE_AS_COMMENT,
	// EXAMPLE_VALUES
	// }
}
