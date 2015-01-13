package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.MovementComponent;
import com.mygdx.game.component.StateComponent;
import com.mygdx.game.component.TransformComponent;
import com.mygdx.game.component.basecomponent.State;

public class MovementSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<StateComponent> sm;

    public MovementSystem() {
        super(Family.getFor(TransformComponent.class, StateComponent.class, MovementComponent.class));

        tm = ComponentMapper.getFor(TransformComponent.class);
        mm = ComponentMapper.getFor(MovementComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
    }

    @Override
    public void update(float dt){
        super.update(dt);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TransformComponent pos = tm.get(entity);
        MovementComponent mov = mm.get(entity);
        StateComponent state = sm.get(entity);
        pos.c.pos.add(mov.velocity.x, mov.velocity.y, 0.0f);
        if (mov.velocity.y > 0) {
            state.c.direction = State.NORTH;
        } else if (mov.velocity.y < 0) {
            state.c.direction = State.SOUTH;
        } else if (mov.velocity.x < 0) {
            state.c.direction = State.EAST;
        } else if (mov.velocity.x > 0) {
            state.c.direction = State.WEST;
        }

//        tmp.set(mov.accel).scl(deltaTime);
//        mov.velocity.add(tmp);

//        tmp.set(mov.velocity).scl(deltaTime);
//        pos.pos.add(tmp.x, tmp.y, 0.0f);
    }
}