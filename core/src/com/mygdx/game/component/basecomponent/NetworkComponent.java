package com.mygdx.game.component.basecomponent;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

public class NetworkComponent extends Component {
    // TODO fill in this field and use it to send just the needed base components
    public final Array<Class> componentsToSerialize = new Array<Class>();
}
