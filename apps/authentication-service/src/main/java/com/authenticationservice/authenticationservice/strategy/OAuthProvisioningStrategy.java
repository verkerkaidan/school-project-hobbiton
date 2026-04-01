package com.authenticationservice.authenticationservice.strategy;

import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Strategy for provisioning a CustomUser from an OAuth2 principal
 * and issuing a JWT. Kept as a separate interface because OAuth2 input
 * (OAuth2User) is structurally different from credential-based input
 * (AuthenticationRequest), so forcing them into the same authenticate()
 * signature would pollute both.
 */
public interface OAuthProvisioningStrategy {
    String provisionAndGenerateToken(OAuth2User oAuth2User);
}
