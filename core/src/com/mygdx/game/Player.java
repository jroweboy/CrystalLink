package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Created by robert on 11/27/14.
 */
public class Player {

    private Action action;
    private Direction direction;

    public Player() {
        action = Action.IDLE;
        direction = Direction.SOUTH;
    }

    public void handleActionInput() {

    }

    public void handleMovementInput() {

    }

    private enum Action{
        IDLE,
        WALKING,
        RUNNING
    }

    private enum Direction{
        NORTH,
        SOUTH,
        EAST,
        WEST
    }
}