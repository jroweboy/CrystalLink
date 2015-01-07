package com.mygdx.game.system;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.component.*;

import javax.swing.plaf.nimbus.State;

public class AnimationSystem extends IteratingSystem {
    private ComponentMapper<TextureComponent> tm;
    private ComponentMapper<AnimationComponent> am;
    private ComponentMapper<StateComponent> sm;
    private ComponentMapper<TransformComponent> transm;

    public AnimationSystem() {
        super(Family.getFor(TextureComponent.class,
                AnimationComponent.class,
                StateComponent.class
        ));

        tm = ComponentMapper.getFor(TextureComponent.class);
        am = ComponentMapper.getFor(AnimationComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TextureComponent tex = tm.get(entity);
        AnimationComponent anim = am.get(entity);
        StateComponent state = sm.get(entity);

//        int s = state.get();
        Animation animation;
//        if (s == StateComponent.STATE_WALK || s == StateComponent.STATE_RUN) {
        if (state != null) {
            int s = state.get();
            MovementComponent pos = entity.getComponent(MovementComponent.class);
            animation = anim.animations.get(pos.direction);
        } else {
            animation = anim.animations.get(state.get());
        }

        if (animation != null) {
            if (state != null && state.get() == StateComponent.STATE_IDLE) {
                tex.region = animation.getKeyFrame(0);
            } else {
                tex.region = animation.getKeyFrame(state.time, true);
            }
        }

//        state.time += deltaTime;
    }
}