package com.profileconnectionservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.UUID;

@Entity
@Table(name = "follow", uniqueConstraints = @UniqueConstraint(columnNames = { "follower_id", "following_id" }))
@Data
@EqualsAndHashCode(callSuper = true)
@AttributeOverrides({
        @AttributeOverride(name = "initiator", column = @Column(name = "follower_id")),
        @AttributeOverride(name = "target", column = @Column(name = "following_id"))
})
public class Follow extends ProfileConnection {

    public Follow() {
    }

    public Follow(UUID follower, UUID following) {
        super(follower, following);
    }

    public UUID getFollower() {
        return getInitiator();
    }

    public void setFollower(UUID follower) {
        setInitiator(follower);
    }

    public UUID getFollowing() {
        return getTarget();
    }

    public void setFollowing(UUID following) {
        setTarget(following);
    }
}
