package com.mygdx.game.actor;

enum Direction {
    UP,
    LEFT,
    DOWN,
    RIGHT,
    NONE
}

/**
 * Created by robert on 11/28/14.
 */


class XDirection {
    static final Direction LEFT = Direction.LEFT;
    static final Direction RIGHT = Direction.RIGHT;
    static final Direction NONE = Direction.NONE;
}

class YDirection {
    static final Direction UP = Direction.UP;
    static final Direction DOWN = Direction.DOWN;
    static final Direction NONE = Direction.NONE;
}

public class MovingActorState {

    MoveAction action;
    Direction xDirection;
    Direction yDirection;

    Direction facingDirection;
    boolean runningEngaged;

    public MovingActorState() {
        action = MoveAction.IDLE;
        xDirection = XDirection.NONE;
        yDirection = YDirection.DOWN;
        facingDirection = YDirection.DOWN;
        runningEngaged = false;
    }

    public MovingActorState(Direction xd, Direction yd) {
        action = MoveAction.IDLE;
        xDirection = xd;
        yDirection = yd;
        facingDirection = (yd != Direction.NONE) ? yd : xd;
        runningEngaged = false;

    }

    public Direction getXDirection() {
        return xDirection;
    }

    public Direction getYDirection() {
        return yDirection;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction d) {
        facingDirection = d;
    }

    public MoveAction getMoveAction() {
        return action;
    }

    public void beginRun() {
        runningEngaged = true;
        if (action == MoveAction.WALK) {
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
        // we still want to know which way the person is facing for checks even if they aren't moving
        xDirection = Direction.NONE;
        endMove();
    }

    public void endRight() {
        xDirection = Direction.NONE;
        endMove();
    }

    public void endUp() {
        yDirection = Direction.NONE;
        endMove();
    }

    public void endDown() {
        yDirection = Direction.NONE;
        endMove();
    }

    private void endMove() {
        if(xDirection == XDirection.NONE && yDirection == YDirection.NONE) {
            action = MoveAction.IDLE;
        }
    }
}
