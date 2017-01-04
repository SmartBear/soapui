package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "SOAPMockResponseTestStep", description = "SOAP mock response test step definition")
public class WsdlMockResponseStepStruct extends TestStepStruct {
    public String wsdl;
    public String binding;
    public String operation;
    public String path;
    public int port;
    public Boolean createResponse;

    @JsonCreator
    public WsdlMockResponseStepStruct(@JsonProperty("type") String type,
                                      @JsonProperty("name") String name,
                                      @JsonProperty("wsdl") String wsdl,
                                      @JsonProperty("binding") String binding,
                                      @JsonProperty("operation") String operation,
                                      @JsonProperty("path") String path,
                                      @JsonProperty("port") Integer port,
                                      @JsonProperty("createResponse") Boolean createResponse) {
        super(type, name);
        this.wsdl = wsdl;
        this.binding = binding;
        this.operation = operation;
        this.path = path;
        this.port = port == null ? 8080 : port;
        this.createResponse = createResponse;
    }
}