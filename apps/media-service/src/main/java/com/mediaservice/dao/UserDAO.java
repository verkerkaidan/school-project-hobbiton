package com.mediaservice.dao;

import com.mediaservice.model.User;
import com.mediaservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserDAO {
    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves User object from database, and deletes it
     *
     * @param userId, unique identifier of user to be deleted
     */
    public void deleteUser(UUID userId) {
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}
