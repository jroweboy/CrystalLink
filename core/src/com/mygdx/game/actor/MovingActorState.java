package com.mygdx.game.actor;

/**
 * Created by robert on 11/28/14.
 */
public class MovingActorState {

    Action action;
    XDirection xDirection;
    YDirection yDirection;
    boolean runningEngaged;

    public MovingActorState() {
        action = Action.IDLE;
        xDirection = XDirection.NONE;
        yDirection = YDirection.DOWN;
        runningEngaged = false;
    }

    public void beginRun() {
        runningEngaged = true;
        if(action == Action.WALK) {
            action = Action.RUN;
        }
    }

    public void beginLeft() {
        xDirection = XDirection.LEFT;
        if (action == Action.IDLE) {
            action = Action.WALK;
        }
        if (action == Action.WALK && runningEngaged) {
            action = Action.RUN;
        }
    }

    public void beginRight() {

    }

    public void beginUp() {

    }

    public void beginDown() {

    }

    public void endRun() {

    }

    public void endLeft() {

    }

    public void endRight() {

    }

    public void endUp() {

    }

    public void endDown() {

    }

    private enum Action{
        RUN,
        WALK,
        IDLE
    }

    private enum XDirection{
        LEFT,
        RIGHT,
        NONE
    }

    private enum YDirection{
        UP,
        DOWN,
        NONE
    }
}
