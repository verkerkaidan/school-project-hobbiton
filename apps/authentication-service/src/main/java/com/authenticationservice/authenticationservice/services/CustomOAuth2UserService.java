package com.authenticationservice.authenticationservice.services;

import com.authenticationservice.authenticationservice.dao.RoleRepository;
import com.authenticationservice.authenticationservice.dao.UserRepository;
import com.authenticationservice.authenticationservice.models.CustomUser;
import com.authenticationservice.authenticationservice.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String email = oAuth2User.getAttribute("email");

        List<Role> role = Collections.singletonList(roleRepository.findByName("ROLE_USER"));
        userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(new CustomUser(email, role))
        );

        return oAuth2User;
    }
}
