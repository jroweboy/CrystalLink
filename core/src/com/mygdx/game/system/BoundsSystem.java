package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.component.CollisionComponent;
import com.mygdx.game.component.TransformComponent;

public class BoundsSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<CollisionComponent> bm;

    public BoundsSystem() {
        super(Family.getFor(CollisionComponent.class, TransformComponent.class));

        tm = ComponentMapper.getFor(TransformComponent.class);
        bm = ComponentMapper.getFor(CollisionComponent.class);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
//        TransformComponent pos = tm.get(entity);
//        CollisionComponent bounds = bm.get(entity);
//
//        bounds.bounds.x = pos.pos.x - bounds.bounds.width * 0.5f;
//        bounds.bounds.y = pos.pos.y - bounds.bounds.height * 0.5f;
    }
}