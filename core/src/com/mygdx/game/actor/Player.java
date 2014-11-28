package com.mygdx.game.actor;

import com.mygdx.game.actor.GameActor;

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