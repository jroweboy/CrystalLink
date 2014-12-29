package com.mygdx.game.system;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mygdx.game.component.AnimationComponent;
import com.mygdx.game.component.StateComponent;
import com.mygdx.game.component.TextureComponent;
import com.mygdx.game.component.TransformComponent;

public class AnimationSystem extends IteratingSystem {
    private ComponentMapper<TextureComponent> tm;
    private ComponentMapper<AnimationComponent> am;
    private ComponentMapper<StateComponent> sm;
    private ComponentMapper<TransformComponent> transm;

    public AnimationSystem() {
        super(Family.getFor(TextureComponent.class,
                AnimationComponent.class,
                StateComponent.class,
                TransformComponent.class));

        tm = ComponentMapper.getFor(TextureComponent.class);
        am = ComponentMapper.getFor(AnimationComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
        transm = ComponentMapper.getFor(TransformComponent.class);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        long id = entity.getId();
        TextureComponent tex = tm.get(entity);
        AnimationComponent anim = am.get(entity);
        StateComponent state = sm.get(entity);
        TransformComponent trans = transm.get(entity);

        Animation animation = anim.animations.get(state.get());

        if (animation != null) {
            tex.obj.setTextureRegion( animation.getKeyFrame(state.time) );
        }

        state.time += deltaTime;
        tex.obj.setX(trans.pos.x);
        tex.obj.setY(trans.pos.y);
    }
}