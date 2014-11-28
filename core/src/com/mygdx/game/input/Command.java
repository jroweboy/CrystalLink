package com.mygdx.game.input;

import com.mygdx.game.actor.GameActor;

/**
 * Created by robert on 11/27/14.
 */
public abstract class Command {

    public abstract void execute(GameActor actor);

}
