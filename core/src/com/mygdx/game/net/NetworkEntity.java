package com.mygdx.game.net;

import com.mygdx.game.component.basecomponent.State;
import com.mygdx.game.component.basecomponent.Transform;

import java.util.UUID;

public class NetworkEntity {
    public long id;
    public Object[] components;

    private NetworkEntity() {
        id = UUID.randomUUID().getLeastSignificantBits();
    }

    private NetworkEntity(long u, int size) {
        id = u;
        components = new Object[size];
        components[0] = new Transform();
        components[1] = new State();
    }

    public static NetworkEntity createPlayer(long uid) {
        return new NetworkEntity(uid, 2);
    }
}
