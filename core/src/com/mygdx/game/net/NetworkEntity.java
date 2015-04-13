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

    private NetworkEntity(int size) {
        id = UUID.randomUUID().getLeastSignificantBits();
        components = new Object[size];
    }

    private NetworkEntity(long u, int size) {
        id = u;
        components = new Object[size];
    }

    public static NetworkEntity createPlayer() {
        NetworkEntity e = new NetworkEntity(2);
        e.components[0] = new Transform();
        e.components[1] = new State();
        return e;
    }

    public static NetworkEntity createDragonfly() {
        NetworkEntity e = new NetworkEntity(2);
        e.components[0] = new Transform();
        e.components[1] = new State();
        return e;
    }
}
