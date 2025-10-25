package com.smartbear.swagger.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropertyTransferDiscovery {

    public static class PropertyTransfer {
        public String sourceOperationId;
        public String targetOperationId;
        public String propertyName;

        public PropertyTransfer(String sourceOperationId, String targetOperationId, String propertyName) {
            this.sourceOperationId = sourceOperationId;
            this.targetOperationId = targetOperationId;
            this.propertyName = propertyName;
        }
    }

    public List<PropertyTransfer> discoverPropertyTransfers(OpenAPI openApi) {
        List<PropertyTransfer> transfers = new ArrayList<>();
        if (openApi == null || openApi.getPaths() == null) {
            return transfers;
        }

        openApi.getPaths().forEach((path, pathItem) -> {
            pathItem.readOperationsMap().forEach((httpMethod, sourceOperation) -> {
                findTransfers(openApi, sourceOperation, transfers);
            });
        });

        return transfers;
    }

    private void findTransfers(OpenAPI openApi, Operation sourceOperation, List<PropertyTransfer> transfers) {
        if (sourceOperation.getResponses() == null) {
            return;
        }

        sourceOperation.getResponses().forEach((responseCode, response) -> {
            if (response.getContent() == null) {
                return;
            }
            response.getContent().forEach((mediaType, mediaTypeObject) -> {
                if (mediaTypeObject.getSchema() != null) {
                    Schema schema = mediaTypeObject.getSchema();
                    if (schema.getProperties() != null) {
                        schema.getProperties().forEach((propertyName, propertySchema) -> {
                            findMatchingParameters(openApi, sourceOperation, propertyName.toString(), transfers);
                        });
                    }
                }
            });
        });
    }

    private void findMatchingParameters(OpenAPI openApi, Operation sourceOperation, String propertyName, List<PropertyTransfer> transfers) {
        openApi.getPaths().forEach((path, pathItem) -> {
            pathItem.readOperationsMap().forEach((httpMethod, targetOperation) -> {
                if (targetOperation.getParameters() != null) {
                    for (Parameter parameter : targetOperation.getParameters()) {
                        if (propertyName.equalsIgnoreCase(parameter.getName())) {
                            transfers.add(new PropertyTransfer(sourceOperation.getOperationId(), targetOperation.getOperationId(), propertyName));
                        }
                    }
                }
            });
        });
    }
}
