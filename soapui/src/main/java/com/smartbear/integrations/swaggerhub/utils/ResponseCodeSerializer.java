package com.smartbear.swagger.utils;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import org.apache.commons.lang.math.NumberUtils;

import java.io.IOException;


/*Custom serializer for response codes. According to OpenAPI 3.0 specification:
* Response code MUST be enclosed in quotation marks (for example, "200") for compatibility between JSON and YAML.
*/
public class ResponseCodeSerializer extends StdKeySerializers.StringKeySerializer {
    @Override
    public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (value instanceof String) {
            String code = (String) value;
            if (NumberUtils.isNumber(code)) {
                ((OpenApiYamlGenerator) g).writeResponseCode((String) value);
            } else {
                g.writeFieldName(code);
            }
        }
    }
}
