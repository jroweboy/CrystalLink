package com.mygdx.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class PathFindingComponent extends Component {
    public Entity target;
    public float velocity = 0.05f;
}
