package com.authenticationservice.authenticationservice.services;

import com.authenticationservice.authenticationservice.dao.RoleRepository;
import com.authenticationservice.authenticationservice.dao.UserRepository;
import com.authenticationservice.authenticationservice.models.CustomUser;
import com.authenticationservice.authenticationservice.models.Privilege;
import com.authenticationservice.authenticationservice.models.Role;
import jakarta.transaction.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository,  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
        public UserDetails loadUserByUsername(String userId)
                throws UsernameNotFoundException {

            Optional<CustomUser> optionalUser = userRepository.findById(UUID.fromString(userId));

            if (optionalUser.isEmpty()) {
                return new org.springframework.security.core.userdetails.User(
                        " ", " ", true, true, true, true,
                        getAuthorities(Collections.singletonList(
                                roleRepository.findByName("ROLE_USER"))
                        )
                );
            }

            CustomUser user = optionalUser.get();

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(), user.isEnabled(), true, true,
                    true, getAuthorities(user.getRoles()));
        }

        private Collection<? extends GrantedAuthority> getAuthorities(
                Collection<Role> roles) {
            return getGrantedAuthorities(getPrivileges(roles));
        }

        private List<String> getPrivileges(Collection<Role> roles) {
            List<String> privileges = new ArrayList<>();
            List<Privilege> collection = new ArrayList<>();
            for (Role role : roles) {
                privileges.add(role.getName());
                collection.addAll(role.getPrivileges());
            }
            for (Privilege item : collection) {
                privileges.add(item.getName());
            }
            return privileges;
        }

        private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (String privilege : privileges) {
                authorities.add(new SimpleGrantedAuthority(privilege));
            }
            return authorities;
        }
    }

