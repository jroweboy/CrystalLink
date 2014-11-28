package com.mygdx.game.actor;

import java.awt.*;

/**
 * Created by robert on 11/28/14.
 */
public class MovingActorState {

    MoveAction action;
    XDirection xDirection;
    YDirection yDirection;
    boolean runningEngaged;

    public MovingActorState() {
        action = MoveAction.IDLE;
        xDirection = XDirection.NONE;
        yDirection = YDirection.DOWN;
        runningEngaged = false;
    }

    public XDirection getXDirection() {
        return xDirection;
    }

    public YDirection getYDirection() {
        return yDirection;
    }

    public MoveAction getMoveAction() {
        return action;
    }

    public void beginRun() {
        runningEngaged = true;
        if(action == MoveAction.WALK) {
            action = MoveAction.RUN;
        }
    }

    public void beginLeft() {
        xDirection = XDirection.LEFT;
        beginMove();
    }

    public void beginRight() {
        xDirection = XDirection.RIGHT;
        beginMove();
    }

    public void beginUp() {
        yDirection = YDirection.UP;
        beginMove();
    }

    public void beginDown() {
        yDirection = YDirection.DOWN;
        beginMove();
    }

    private void beginMove() {
        if (action == MoveAction.IDLE) {
            action = MoveAction.WALK;
        }
        if (action == MoveAction.WALK && runningEngaged) {
            action = MoveAction.RUN;
        }
    }

    public void endRun() {
        runningEngaged = false;
        if (action == MoveAction.RUN){
            action = MoveAction.WALK;
        }
    }

    public void endLeft() {
        xDirection = xDirection.NONE;
        endMove();
    }

    public void endRight() {
        xDirection = xDirection.NONE;
        endMove();
    }

    public void endUp() {
        yDirection = yDirection.NONE;
        endMove();
    }

    public void endDown() {
        yDirection = yDirection.NONE;
        endMove();
    }

    private void endMove() {
        if(xDirection == XDirection.NONE && yDirection == YDirection.NONE) {
            action = MoveAction.IDLE;
        }
    }
}
