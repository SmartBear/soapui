package com.smartbear.swagger;

import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.support.MessageSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.google.common.collect.Lists;

import java.util.List;

public class ValidationSupport {
    private static final MessageSupport messages = MessageSupport.getMessages(ValidationSupport.class);

    static void validateMessage(JsonSchema jsonSchema, JsonNode contentObject) throws AssertionException {
        ProcessingReport report = null;
        try {
            report = jsonSchema.validate(contentObject, true);
        } catch (ProcessingException e) {
            throw new AssertionException(new AssertionError(e.getProcessingMessage().getMessage()));
        }

        if (!report.isSuccess()) {
            List<AssertionError> errors = Lists.newArrayList();
            final String POINTER_FIELD = "pointer";

            for (ProcessingMessage message : report) {
                if (message.getLogLevel() == LogLevel.ERROR || message.getLogLevel() == LogLevel.FATAL) {
                    try {
                        JsonNode node = message.asJson();
                        String checkedPath = "";
                        String schemaPath = "";
                        //get checked Node path
                        JsonNode checkedInstanceNode = node.get("instance");
                        if (checkedInstanceNode != null) {
                            JsonNode checkedPathNode = checkedInstanceNode.get(POINTER_FIELD);
                            if (checkedPathNode != null) {
                                checkedPath = checkedPathNode.asText();
                            }
                        }
                        //get corresponding schema path
                        JsonNode schema = node.get("schema");
                        if (schema != null) {
                            JsonNode schemaNode = schema.get(POINTER_FIELD);
                            if (schemaNode != null) {
                                schemaPath = schemaNode.asText();
                            }
                        }

                        errors.add(new AssertionError(message.getMessage() + " " +
                                messages.get("ValidationSupport.CheckedNode.LogMessage", checkedPath) + " "
                                + messages.get("ValidationSupport.CorrespondingSchemaItem.LogMessage", schemaPath)));
                    } catch (Exception e) {
                        errors.add(new AssertionError(message.getMessage()));
                    }
                }
            }

            if (!errors.isEmpty()) {
                throw new AssertionException(errors.toArray(new AssertionError[errors.size()]));
            }
        }
    }
}
