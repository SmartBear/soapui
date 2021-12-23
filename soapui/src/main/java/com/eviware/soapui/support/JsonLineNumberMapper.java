package com.eviware.soapui.support;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonLineNumberMapper {
    private Map<JsonPointer, NodeRange> lines;
    private JsonPointer jsonPointer = JsonPointer.compile("");
    private boolean rootProcessed = false;
    private int yamlLineNumberCorrection = 0;

    public Map<JsonPointer, NodeRange> mapLineNumber(JsonParser parser, boolean isYaml) {
        lines = new HashMap<>();
        if (isYaml) {
            yamlLineNumberCorrection = 1;
        }
        try {
            JsonToken token = parser.nextToken();
            while (token != null) {
                JsonStreamContext context = parser.getParsingContext();
                JsonLocation location = parser.getCurrentLocation();
                processLineEntry(token, location, context);
                token = parser.nextToken();
            }
            lines.get(jsonPointer).endLine = parser.getCurrentLocation().getLineNr();
        } catch (IOException e) {
            LogManager.getLogger(JsonLineNumberMapper.class).error(e.getMessage(), e);
        }

        return lines;
    }

    private void processLineEntry(final JsonToken token, final JsonLocation location, final JsonStreamContext context) {
        if (!rootProcessed) {
            NodeRange nodeRange = new NodeRange(location.getLineNr());
            lines.put(jsonPointer, nodeRange);
            rootProcessed = true;
            return;
        }

        if (context.inRoot()) {
            return;
        }

        if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
            lines.get(jsonPointer).endLine = location.getLineNr();
            jsonPointer = JsonPointer.forPath(context.getParent(), false);
            return;
        }

        if (token == JsonToken.FIELD_NAME) {
            return;
        }

        JsonStreamContext parentContext = context.getParent();
        int lineNumber = location.getLineNr();

        if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
            jsonPointer = JsonPointer.forPath(parentContext, false);
            lineNumber = lineNumber <= 1 ? lineNumber : lineNumber - yamlLineNumberCorrection;
            lines.put(jsonPointer, new NodeRange(lineNumber));
            return;
        }

        final JsonPointer entryPointer = JsonPointer.forPath(context, false);
        lines.put(entryPointer, new NodeRange(lineNumber, lineNumber));
    }

    public static String processNodeName(String name) {
        return name.replace("~", "~0").replace("/", "~1");
    }
}

