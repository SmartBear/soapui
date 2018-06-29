package com.smartbear.ready.recipe.teststeps;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import static com.smartbear.ready.recipe.NullChecker.checkNotNull;

/**
 * Used to deserialize JSON objects representing the authentication information for a REST request test step to Java.
 */
@ApiModel(value="Authentication", description="Authentication definition")
public class AuthenticationStruct {

    public AuthenticationStruct(@JsonProperty("type") String type, @JsonProperty("username") String username,
                                @JsonProperty("password") String password, @JsonProperty("domain") String domain) {
        checkNotNull(type, "type");

        this.type = type;
        this.username = username;
        this.password = password;
        this.domain = domain;
    }

    public String type;
    public String username;
    public String password;
    public String domain;

    //OAuth 2.0
    public String accessToken;
    public String accessTokenPosition;

    public String accessTokenUri;
    public String clientId;
    public String clientSecret;
    public String refreshToken;

}
