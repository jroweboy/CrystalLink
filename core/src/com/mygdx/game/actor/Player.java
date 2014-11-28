package com.mygdx.game.actor;

import com.mygdx.game.actor.GameActor;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by robert on 11/27/14.
 */
public class Player extends GameActor implements MovingActor {

    private MovingActorState movementState;
    private Point2D position;

    public Player() {
        position = new Point2D.Double(0, 0);
        movementState = new MovingActorState();
    }

    public void update() {
        processMovement();
    }

    // The following 3 methods are for knowing which sprite to draw
    public XDirection getXFacing() {
        return movementState.getXDirection();
    }

    public YDirection getYFacing() {
        return movementState.getYDirection();
    }

    public MoveAction getMoveState() {
        return movementState.getMoveAction();
    }

    private void processMovement() {
        // TODO handle collisions
        MoveAction moveAction = movementState.getMoveAction();
        XDirection xDirection = movementState.getXDirection();
        YDirection yDirection = movementState.getYDirection();

        if(moveAction == MoveAction.IDLE) {
            return;
        }

        double dx = 0;
        double dy = 0;

        if(xDirection == XDirection.LEFT) {
            dx = -0.5;
        } else if (xDirection == XDirection.RIGHT) {
            dx = 0.5;
        }

        if(yDirection == YDirection.UP) {
            dy = -0.5;
        } else if (yDirection == YDirection.DOWN) {
            dy = 0.5;
        }

        if(moveAction == MoveAction.RUN) {
            dx *= 2;
            dy *= 2;
        }

        position.setLocation(position.getX() + dx, position.getY() + dy);
    }

    @Override
    public void beginRun() {
        movementState.beginRun();
    }

    @Override
    public void beginUp() {
        movementState.beginUp();
    }

    @Override
    public void beginDown() {
        movementState.beginDown();
    }

    @Override
    public void beginLeft() {
        movementState.beginLeft();
    }

    @Override
    public void beginRight() {
        movementState.beginRight();
    }

    @Override
    public void endRun() {
        movementState.endRun();
    }

    @Override
    public void endUp() {
        movementState.endUp();
    }

    @Override
    public void endDown() {
        movementState.endDown();
    }

    @Override
    public void endLeft() {
        movementState.endLeft();
    }

    @Override
    public void endRight() {
        movementState.endRight();
    }

}