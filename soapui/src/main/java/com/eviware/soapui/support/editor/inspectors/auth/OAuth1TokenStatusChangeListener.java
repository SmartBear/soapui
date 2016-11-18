package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.AccessTokenStatusConfig;
import com.eviware.soapui.impl.rest.OAuth1Profile;

import javax.annotation.Nonnull;

public interface OAuth1TokenStatusChangeListener {

    void onAccessTokenStatusChanged(@Nonnull AccessTokenStatusConfig.Enum status);

    void onTokenSecretStatusChanged(@Nonnull AccessTokenStatusConfig.Enum status);

    @Nonnull
    OAuth1Profile getProfile();
}
