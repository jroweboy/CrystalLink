package com.mygdx.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

public class TransformComponent extends Component {
    @Tag(0) public final Vector3 pos = new Vector3();
    @Tag(0) public final Vector2 scale = new Vector2(1.0f, 1.0f);
    @Tag(0) public float rotation = 0.0f;

    public void set(TransformComponent t){
        pos.set(t.pos);
        scale.set(t.scale);
        rotation = t.rotation;
    }

}