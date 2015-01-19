package com.mygdx.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Disposable;

public class CollisionComponent extends Component implements Disposable {
    public Body body;
    public Rectangle bounds = new Rectangle();
    private CollisionComponent() {}
    public CollisionComponent(float width, float height) {
        bounds.width = width;
        bounds.height = height;
        bounds.x = 0;
        bounds.y = 0;
    }

    public CollisionComponent(float x, float y, float width, float height) {
        bounds.width = width;
        bounds.height = height;
        bounds.x = x;
        bounds.y = y;
    }

    public CollisionComponent(Rectangle r) {
        bounds.width = r.width;
        bounds.height = r.height;
        bounds.x = r.x;
        bounds.y = r.y;
    }

    @Override
    public void dispose() {
        body.getWorld().destroyBody(body);
    }
}
