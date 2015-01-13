package com.mygdx.game.component;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.component.basecomponent.Transform;

public class TransformComponent extends Component {
    public final Transform c = new Transform();

    public void set(Transform t) {
        this.c.set(t);
    }

    public void set(TransformComponent t){
        this.c.set(t);
    }
}