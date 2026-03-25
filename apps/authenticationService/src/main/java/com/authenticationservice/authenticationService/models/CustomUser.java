package com.authenticationservice.authenticationService.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.lang.NonNull;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
public class CustomUser {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true)
    @NonNull
    private String email;

    @NonNull
    private String password;
    private boolean enabled;
    private boolean tokenExpired;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;

    private OffsetDateTime lastEdited;

    @CreationTimestamp
    @Column(name = "created_at")
    public OffsetDateTime created_at;

    public CustomUser(@NonNull String email, @NonNull String encodedPassword, List<Role> roleUser) {
        this.email = email;
        this.password = encodedPassword;
        this.roles = roleUser;
        this.enabled = true;
        this.lastEdited = OffsetDateTime.now();
    }


    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }
}
