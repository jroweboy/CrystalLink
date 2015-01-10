package com.mygdx.game.net;

import com.badlogic.ashley.core.Entity;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

import java.util.UUID;

import static com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.*;

public class NetworkEntity extends Entity {
    @Tag(0) public Long id;
    public NetworkEntity() {
        id = UUID.randomUUID().getLeastSignificantBits();
    }
    public NetworkEntity(long u) {
        id = u;
    }
}
