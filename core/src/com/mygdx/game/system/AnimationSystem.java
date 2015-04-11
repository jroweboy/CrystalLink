package com.mygdx.game.system;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mygdx.game.component.*;
import com.mygdx.game.component.basecomponent.State;


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
        State state = sm.get(entity).c;

//        int s = state.get();
        Animation animation;
//        if (s == StateComponent.STATE_WALK || s == StateComponent.STATE_RUN) {
//        if (state.direction == State.NO_DIRECTION) {
//
//        } else {
//        Gdx.app.log("Animation", "state: " + state.direction);
            animation = anim.animations.get(state.direction);
//        }
//        if (state != null) {
//            int s = state.get();
//            MovementComponent pos = entity.getComponent(MovementComponent.class);
//            animation = anim.animations.get(state.c.direction);
//        } else {
//            animation = anim.animations.get(state.get());
//        }

        if (animation != null) {
            if (state != null && state.get() == State.STATE_IDLE) {
                tex.region = animation.getKeyFrame(0);
                tex.normal = anim.animation_normals.get(state.direction).getKeyFrame(0);
            } else {
                tex.region = animation.getKeyFrame(state.time, true);
                tex.normal = anim.animation_normals.get(state.direction).getKeyFrame(state.time, true);
            }
        }

//        state.time += deltaTime;
    }
}