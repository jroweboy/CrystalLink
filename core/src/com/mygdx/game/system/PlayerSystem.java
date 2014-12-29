package com.mygdx.game.system;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.World;
import com.mygdx.game.actor.Player;
import com.mygdx.game.component.MovementComponent;
import com.mygdx.game.component.PlayerComponent;
import com.mygdx.game.component.StateComponent;
import com.mygdx.game.component.TransformComponent;

public class PlayerSystem extends IteratingSystem {
    private static final Family family = Family.getFor(PlayerComponent.class,
            StateComponent.class,
            TransformComponent.class,
            MovementComponent.class);

    private float accelX = 0.0f;
    private World world;

    private ComponentMapper<PlayerComponent> bm;
    private ComponentMapper<StateComponent> sm;
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;

    public PlayerSystem(World world) {
        super(family);

        this.world = world;

        bm = ComponentMapper.getFor(PlayerComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
        tm = ComponentMapper.getFor(TransformComponent.class);
        mm = ComponentMapper.getFor(MovementComponent.class);
    }

    public void setAccelX(float accelX) {
        this.accelX = accelX;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

//        accelX = 0.0f;
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TransformComponent t = tm.get(entity);
        StateComponent state = sm.get(entity);
        MovementComponent mov = mm.get(entity);
        PlayerComponent bob = bm.get(entity);
//
//        if (state.get() != PlayerComponent.STATE_HIT && t.pos.y <= 0.5f) {
//            hitPlatform(entity);
//        }
//
//        if (state.get() != PlayerComponent.STATE_HIT) {
//            mov.velocity.x = -accelX / 10.0f * PlayerComponent.MOVE_VELOCITY;
//        }
//
//        if (mov.velocity.y > 0 && state.get() != PlayerComponent.STATE_HIT) {
//            if (state.get() != PlayerComponent.STATE_JUMP) {
//                state.set(PlayerComponent.STATE_JUMP);
//            }
//        }
//
//        if (mov.velocity.y < 0 && state.get() != PlayerComponent.STATE_HIT) {
//            if (state.get() != PlayerComponent.STATE_FALL) {
//                state.set(PlayerComponent.STATE_FALL);
//            }
//        }
//
//        if (t.pos.x < 0) {
//            t.pos.x = World.WORLD_WIDTH;
//        }
//
//        if (t.pos.x > World.WORLD_WIDTH) {
//            t.pos.x = 0;
//        }
//
//        t.scale.x = mov.velocity.x < 0.0f ? Math.abs(t.scale.x) * -1.0f : Math.abs(t.scale.x);
//
//        bob.heightSoFar = Math.max(t.pos.y, bob.heightSoFar);
//
//        if (bob.heightSoFar - 7.5f > t.pos.y) {
//            world.state = World.WORLD_STATE_GAME_OVER;
//        }
    }

//    public void hitSquirrel (Entity entity) {
//        if (!family.matches(entity)) return;
//
//        StateComponent state = sm.get(entity);
//        MovementComponent mov = mm.get(entity);
//
//        mov.velocity.set(0, 0);
//        state.set(PlayerComponent.STATE_HIT);
//    }
//
//    public void hitPlatform (Entity entity) {
//        if (!family.matches(entity)) return;
//
//        StateComponent state = sm.get(entity);
//        MovementComponent mov = mm.get(entity);
//
//        mov.velocity.y = PlayerComponent.JUMP_VELOCITY;
//        state.set(PlayerComponent.STATE_JUMP);
//    }
//
//    public void hitSpring (Entity entity) {
//        if (!family.matches(entity)) return;
//
//        StateComponent state = sm.get(entity);
//        MovementComponent mov = mm.get(entity);
//
//        mov.velocity.y = PlayerComponent.JUMP_VELOCITY * 1.5f;
//        state.set(PlayerComponent.STATE_JUMP);
//    }
}