package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.MovementComponent;
import com.mygdx.game.component.TransformComponent;

public class MovementSystem extends IteratingSystem {
//    private Vector2 tmp = new Vector2();

    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;

    public MovementSystem() {
        super(Family.getFor(TransformComponent.class, MovementComponent.class));

        tm = ComponentMapper.getFor(TransformComponent.class);
        mm = ComponentMapper.getFor(MovementComponent.class);
    }

    @Override
    public void update(float dt){
        super.update(dt);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TransformComponent pos = tm.get(entity);
        MovementComponent mov = mm.get(entity);;
        pos.pos.add(mov.velocity.x, mov.velocity.y, 0.0f);

//        tmp.set(mov.accel).scl(deltaTime);
//        mov.velocity.add(tmp);

//        tmp.set(mov.velocity).scl(deltaTime);
//        pos.pos.add(tmp.x, tmp.y, 0.0f);
    }
}