package com.mygdx.game.component;

/**
 * Created by James on 12/13/2014.
 */
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class MovementComponent extends Component {
    public final Vector2 velocity = new Vector2();
    public int direction;

    private MovementComponent(){}

    public MovementComponent(int direction) {
        this.direction = direction;
    }

    public static final int NORTH = 1000;
    public static final int SOUTH = 1001;
    public static final int EAST = 1002;
    public static final int WEST = 1003;
    public static final int NORTHEAST = 1004;
    public static final int SOUTHEAST = 1005;
    public static final int NORTHWEST = 1006;
    public static final int SOUTHWEST = 1007;
}