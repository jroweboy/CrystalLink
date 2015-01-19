package com.mygdx.game.component.basecomponent;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

public class NetworkComponent extends Component {
    private NetworkComponent() {}
    public NetworkComponent(long id) {
        this.id = id;
    }
    // TODO: I remember there being some good reason for this not being an id field...
    public long id;
    // TODO think about filling in this field and use it to send just the needed base components
//    public final Array<Class> componentsToSerialize = new Array<Class>();
}
