package com.profileconnectionservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.UUID;

@Entity
@Table(name = "block", uniqueConstraints = @UniqueConstraint(columnNames = { "blocker_id", "blocked_id" }))
@Data
@EqualsAndHashCode(callSuper = true)
@AttributeOverrides({
        @AttributeOverride(name = "initiator", column = @Column(name = "blocker_id")),
        @AttributeOverride(name = "target", column = @Column(name = "blocked_id"))
})
public class Block extends ProfileConnection {

    public Block() {
    }

    public Block(UUID blocker, UUID blocked) {
        super(blocker, blocked);
    }

    public UUID getBlocker() {
        return getInitiator();
    }

    public void setBlocker(UUID blocker) {
        setInitiator(blocker);
    }

    public UUID getBlocked() {
        return getTarget();
    }

    public void setBlocked(UUID blocked) {
        setTarget(blocked);
    }
}
