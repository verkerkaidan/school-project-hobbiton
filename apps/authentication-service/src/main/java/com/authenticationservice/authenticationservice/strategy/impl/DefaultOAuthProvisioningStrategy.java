package com.authenticationservice.authenticationservice.strategy.impl;

import com.authenticationservice.authenticationservice.config.JWTUtil;
import com.authenticationservice.authenticationservice.dao.RoleRepository;
import com.authenticationservice.authenticationservice.dao.UserRepository;
import com.authenticationservice.authenticationservice.models.CustomUser;
import com.authenticationservice.authenticationservice.models.Role;
import com.authenticationservice.authenticationservice.strategy.OAuthProvisioningStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * OAuthSuccessHandler delegates here for JWT issuance.
 * CustomOAuth2UserService delegates here for user provisioning.
 */
@Component
@RequiredArgsConstructor
public class DefaultOAuthProvisioningStrategy implements OAuthProvisioningStrategy {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public String provisionAndGenerateToken(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        List<Role> role = Collections.singletonList(roleRepository.findByName("ROLE_USER"));
        CustomUser user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(new CustomUser(email, role)));

        return jwtUtil.generateToken(String.valueOf(user.getId()));
    }
}
