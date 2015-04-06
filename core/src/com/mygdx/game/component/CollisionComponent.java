package com.mygdx.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Disposable;

public class CollisionComponent extends Component implements Disposable {
    public Body body;
    public Shape bounds;

    private CollisionComponent() {}

    public CollisionComponent(Shape p) {
        bounds = p;
    }


    @Override
    public void dispose() {
        body.getWorld().destroyBody(body);
        if (bounds != null) {
            bounds.dispose();
        }
    }
}
