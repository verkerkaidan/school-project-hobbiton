package com.authenticationservice.authenticationservice.config;

import com.authenticationservice.authenticationservice.strategy.OAuthProvisioningStrategy;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    // Handler is now slim: it handles the HTTP concern (cookie) only.
    // User provisioning + JWT issuance are the strategy's responsibility.
    private final OAuthProvisioningStrategy oAuthProvisioningStrategy;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String jwt = oAuthProvisioningStrategy.provisionAndGenerateToken(oAuth2User);

        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
    }
}
