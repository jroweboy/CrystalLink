package com.mygdx.game.net;

import com.mygdx.game.component.basecomponent.Transform;

public class NetworkNewPlayer {
    public long id;
    public final Transform transform = new Transform();
    private NetworkNewPlayer() { }

    public NetworkNewPlayer(long id) {
        this.id = id;
    }

    public NetworkNewPlayer(long id, Transform t) {
        this.id = id;
        transform.set(t);
    }
}
