package com.mygdx.game.component.basecomponent;

public class State {
    public static final int STATE_IDLE = 10;
    public static final int STATE_HIT = 11;
    public static final int STATE_WALK = 12;
    public static final int STATE_RUN = 13;

    protected int state = STATE_IDLE;
    public float time = 0.0f;


    // direction states
    public static final int NO_DIRECTION = 999;
    public static final int NORTH = 1000;
    public static final int SOUTH = 1001;
    public static final int EAST = 1002;
    public static final int WEST = 1003;
    public static final int NORTHEAST = 1004;
    public static final int SOUTHEAST = 1005;
    public static final int NORTHWEST = 1006;
    public static final int SOUTHWEST = 1007;

    public int direction = NO_DIRECTION;

    public int get() {
        return state;
    }

    public void set(int newState) {
        if (state != newState) {
            state = newState;
            time = 0.0f;
        }
    }
}
