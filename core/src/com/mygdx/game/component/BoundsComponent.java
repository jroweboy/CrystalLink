package com.mygdx.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;

public class BoundsComponent extends Component {
    public Rectangle bounds = new Rectangle();
    private BoundsComponent() {}
    public BoundsComponent(float width, float height) {
        bounds.width = width;
        bounds.height = height;
        bounds.x = 0;
        bounds.y = 0;
    }

    public BoundsComponent(float x, float y, float width, float height) {
        bounds.width = width;
        bounds.height = height;
        bounds.x = x;
        bounds.y = y;
    }

    public BoundsComponent(Rectangle r) {
        bounds.width = r.width;
        bounds.height = r.height;
        bounds.x = r.x;
        bounds.y = r.y;
    }
}
