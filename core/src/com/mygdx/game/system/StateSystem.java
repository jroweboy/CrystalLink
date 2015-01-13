package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.component.StateComponent;

public class StateSystem extends IteratingSystem {
    private ComponentMapper<StateComponent> sm;

    public StateSystem() {
        super(Family.getFor(StateComponent.class));

        sm = ComponentMapper.getFor(StateComponent.class);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        sm.get(entity).c.time += deltaTime;
    }
}