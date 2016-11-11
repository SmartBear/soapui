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

package com.eviware.soapui.settings;

import com.eviware.soapui.settings.Setting.SettingType;

/**
 * WSDL related settings constants
 *
 * @author Emil.Breding
 */

public interface WsdlSettings {
    @Setting(name = "Cache WSDLs", description = "caches and associates WSDLs locally for offline access and improved performance", type = SettingType.BOOLEAN)
    public final static String CACHE_WSDLS = WsdlSettings.class.getSimpleName() + "@" + "cache-wsdls";

    @Setting(name = "Sample Values", description = "generate example values in new requests", type = SettingType.BOOLEAN)
    public final static String XML_GENERATION_TYPE_EXAMPLE_VALUE = WsdlSettings.class.getSimpleName() + "@"
            + "xml-generation-type-example-value";

    @Setting(name = "Type Comment", description = "generate comments with type information in new requests", type = SettingType.BOOLEAN)
    public final static String XML_GENERATION_TYPE_COMMENT_TYPE = WsdlSettings.class.getSimpleName() + "@"
            + "xml-generation-type-comment-type";

    @Setting(name = "Include Optional", description = "always include optional schema elements when creating requests", type = SettingType.BOOLEAN)
    public final static String XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS = WsdlSettings.class.getSimpleName()
            + "@" + "xml-generation-always-include-optional-elements";

    @Setting(name = "Pretty Print", description = "pretty print response messages", type = SettingType.BOOLEAN)
    public final static String PRETTY_PRINT_RESPONSE_MESSAGES = WsdlSettings.class.getSimpleName() + "@"
            + "pretty-print-response-xml";

    @Setting(name = "Attachment Parts", description = "generate rpc message parts for attachments", type = SettingType.BOOLEAN)
    public final static String ATTACHMENT_PARTS = WsdlSettings.class.getSimpleName() + "@" + "attachment-parts";

    @Setting(name = "No Content-Type Validation", description = "allow incorrect content-types in mime-attachments", type = SettingType.BOOLEAN)
    public final static String ALLOW_INCORRECT_CONTENTTYPE = WsdlSettings.class.getSimpleName() + "@"
            + "allow-incorrect-contenttype";

    public final static String ENABLE_MTOM = WsdlSettings.class.getSimpleName() + "@" + "enable-mtom";

    @Setting(name = "Schema Directory", description = "local directory containing schemas that should be added to loading/validation", type = SettingType.FOLDER)
    public static final String SCHEMA_DIRECTORY = WsdlSettings.class.getSimpleName() + "@" + "schema-directory";

    @Setting(name = "Name with Binding", description = "uses the WSDL binding name (instead of portType) for imported Interfaces", type = SettingType.BOOLEAN)
    public final static String NAME_WITH_BINDING = WsdlSettings.class.getSimpleName() + "@" + "name-with-binding";

    @Setting(name = "Excluded types", description = "types to exclude in request generation", type = SettingType.STRINGLIST)
    public final static String EXCLUDED_TYPES = WsdlSettings.class.getSimpleName() + "@" + "excluded-types";

    @Setting(name = "Strict schema types", description = "fails schema imports if types/particles are redefined", type = SettingType.BOOLEAN)
    public final static String STRICT_SCHEMA_TYPES = WsdlSettings.class.getSimpleName() + "@" + "strict-schema-types";

    @Setting(name = "Compression Limit", description = "minimum message size to compress", type = SettingType.INT)
    public final static String COMPRESSION_LIMIT = WsdlSettings.class.getSimpleName() + "@" + "compression-limit";

    @Setting(name = "Pretty Print Project Files", description = "pretty prints project files", type = SettingType.BOOLEAN)
    public final static String PRETTY_PRINT_PROJECT_FILES = WsdlSettings.class.getSimpleName() + "@"
            + "pretty-print-project-files";

    public static final String XML_GENERATION_SKIP_COMMENTS = WsdlSettings.class.getSimpleName() + "@"
            + "xml-generation-skip-comments";

    @Setting(name = "Trim WSDL", description = "Trims leading and trailing whitespaces from WSDL file (might not work on non 8 bit encoding)", type = SettingType.BOOLEAN)
    public final static String TRIM_WSDL = WsdlSettings.class.getSimpleName() + "@"
            + "trim-wsdl";

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
