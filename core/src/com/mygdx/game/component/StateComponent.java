package com.mygdx.game.component;

/**
 * Created by James on 12/13/2014.
 */
import com.badlogic.ashley.core.Component;

public class StateComponent extends Component {

    public static final int STATE_IDLE = 10;
    public static final int STATE_HIT = 11;
    public static final int STATE_WALK = 12;
    public static final int STATE_RUN = 13;

    private int state = STATE_IDLE;
    public float time = 0.0f;

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