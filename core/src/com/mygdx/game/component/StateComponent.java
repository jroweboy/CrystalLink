package com.mygdx.game.component;

/**
 * Created by James on 12/13/2014.
 */
import com.badlogic.ashley.core.Component;
import com.mygdx.game.component.basecomponent.State;

public class StateComponent extends Component {
    public final State c = new State();
    public int get() {
        return c.get();
    }
    public void set(int state) {
        c.set(state);
    }
}