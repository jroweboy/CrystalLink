package com.mygdx.game.component;

import com.badlogic.ashley.core.Component;

public class PlayerComponent extends Component {

    public static final int STATE_IDLE = 2;
    public static final int STATE_HIT = 3;
    public static final float WALK_SPEED = 0.1f;
    public static final float RUN_SPEED_MULT = 2.0f;
}