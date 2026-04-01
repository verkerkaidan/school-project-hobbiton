package com.interactionservice.events;

import java.util.UUID;

public record CommentDeletedEvent(UUID commentId) {
}
