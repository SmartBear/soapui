package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestAttachmentStruct {

    public String contentType;
    public String name;
    public String contentId;
    public String content;

    @JsonCreator
    public RequestAttachmentStruct(
            @JsonProperty("contentType") String contentType,
            @JsonProperty("name") String name,
            @JsonProperty("contentId") String contentId,
            @JsonProperty("content") String content) {
        this.contentType = contentType;
        this.name = name;
        this.contentId = contentId;
        this.content = content;
    }
}
