package com.mygdx.game.component;

/**
 * Created by James on 12/13/2014.
 */
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class MovementComponent extends Component {
    public final Vector2 velocity = new Vector2();
    public final Vector2 accel = new Vector2();


    public static final int STATE_WALK = 0;
    public static final int STATE_RUN = 1;

    public static final int DIRECTION_NORTH = 0;
    public static final int DIRECTION_SOUTH = 1;
    public static final int DIRECTION_EAST = 2;
    public static final int DIRECTION_WEST = 3;
}