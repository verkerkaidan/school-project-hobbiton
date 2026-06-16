package com.apigateway.apigateway.models.media;

import java.util.UUID;

public class MediaInstance {
    public UUID id;
    public UUID instanceId;
    // add whatever other fields the upstream service returns

    public MediaInstance(UUID instanceId) {
        this.instanceId = instanceId;
    }

    public MediaInstance() {
    }
}
