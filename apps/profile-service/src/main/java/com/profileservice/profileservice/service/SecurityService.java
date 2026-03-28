package com.profileservice.profileservice.service;

import org.springframework.stereotype.Service;

@Service
public class SecurityService {


    public String getRole(String userId){
        return "USER_ROLE";
    };
}
