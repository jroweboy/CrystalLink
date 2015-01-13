package com.mygdx.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.MovementComponent;
import com.mygdx.game.component.PlayerComponent;
import com.mygdx.game.component.StateComponent;
import com.mygdx.game.component.TransformComponent;
import com.mygdx.game.component.basecomponent.State;

public class InputSystem extends IteratingSystem {
    public InputSystem() {
        // the Transform component is to differentiate it from the network players
        super(Family.getFor(PlayerComponent.class, MovementComponent.class, TransformComponent.class));
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
//        PlayerComponent player = entity.getComponent(PlayerComponent.class);
        StateComponent state = entity.getComponent(StateComponent.class);
        MovementComponent pos = entity.getComponent(MovementComponent.class);
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
            dy = 1;
//            pos.direction = MovementComponent.NORTH;
            if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
                dx = 1;
//                pos.direction = MovementComponent.NORTHEAST;
            } else if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
                dx = -1;
//                pos.direction = MovementComponent.NORTHWEST;
            } else {
//                pos.direction = MovementComponent.NORTH;
            }
        } else if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
            dy = -1;
//            pos.direction = MovementComponent.SOUTH;
            if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
                dx = 1;
//                pos.direction = MovementComponent.SOUTHEAST;
            } else if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
                dx = -1;
//                pos.direction = MovementComponent.SOUTHWEST;
            } else {
//                pos.direction = MovementComponent.SOUTH;
            }
        } else if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
            dx = -1;
//            pos.direction = MovementComponent.EAST;
        } else if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
            dx = 1;
//            pos.direction = MovementComponent.WEST;
        }

        // if you are going diagonally, limit the speed
        if (dx != 0 && dy != 0) {
            dx /= 1.41421356237f;
            dy /= 1.41421356237f;
        }


        if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
            state.set(State.STATE_RUN);
            pos.velocity.set(PlayerComponent.RUN_SPEED_MULT * PlayerComponent.WALK_SPEED * dx,
                    PlayerComponent.RUN_SPEED_MULT * PlayerComponent.WALK_SPEED * dy);
        } else {
            state.set(State.STATE_WALK);
            pos.velocity.set(PlayerComponent.WALK_SPEED * dx, PlayerComponent.WALK_SPEED * dy);
        }

        if (dx == 0 && dy == 0) {
            state.set(State.STATE_IDLE);
        }
    }
}
