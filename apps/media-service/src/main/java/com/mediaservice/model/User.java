package com.mediaservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(
        name = "users"
)
public class User {
    @Id
    @Column(
            name = "user_id"
    )
    private UUID userId;

    public User(UUID user_id) {
        this.userId = user_id;
    }

    public User(){}
}
