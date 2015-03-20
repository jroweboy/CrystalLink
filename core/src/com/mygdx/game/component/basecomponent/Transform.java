package com.mygdx.game.component.basecomponent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.component.TransformComponent;

public class Transform {
    public final Vector3 pos = new Vector3();
    public final Vector2 scale = new Vector2(1.0f, 1.0f);
    public float rotation = 0.0f;
    public long level_id = 0;

    public void set(Transform t) {
        this.pos.set(t.pos);
        this.scale.set(t.scale);
        this.rotation = t.rotation;
    }

    public void set(TransformComponent t) {
        this.pos.set(t.c.pos);
        this.scale.set(t.c.scale);
        this.rotation = t.c.rotation;
    }
}
