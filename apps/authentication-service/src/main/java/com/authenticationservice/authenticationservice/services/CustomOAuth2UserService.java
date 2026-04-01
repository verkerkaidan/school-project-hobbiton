package com.authenticationservice.authenticationservice.services;

import com.authenticationservice.authenticationservice.strategy.OAuthProvisioningStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * This service is still needed by Spring's OAuth2 filter chain to load the
 * OAuth2User during the authorization code flow, but user provisioning
 * is no longer its concern.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthProvisioningStrategy oAuthProvisioningStrategy;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        // Ensure the user exists in our DB before Spring completes authentication.
        // JWT issuance happens later in OAuthSuccessHandler.
        oAuthProvisioningStrategy.provisionAndGenerateToken(oAuth2User);

        return oAuth2User;
    }
}
