package com.mygdx.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.IntMap;


/**
 * Created by James on 12/13/2014.
 */

public class AnimationComponent extends Component {
    public IntMap<Animation> animations = new IntMap<Animation>();
}