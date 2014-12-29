package com.mygdx.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.mygdx.game.World;
import com.mygdx.game.component.*;

import java.util.Random;

public class CollisionSystem extends EntitySystem {
    private ComponentMapper<BoundsComponent> bm;
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<StateComponent> sm;
    private ComponentMapper<TransformComponent> tm;

    public static interface CollisionListener {
        public void jump ();
        public void highJump ();
        public void hit ();
        public void coin ();
    }

    private Engine engine;
    private World world;
    private CollisionListener listener;
    private Random rand = new Random();
    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> coins;
    private ImmutableArray<Entity> squirrels;
    private ImmutableArray<Entity> springs;
    private ImmutableArray<Entity> castles;
    private ImmutableArray<Entity> platforms;

    public CollisionSystem(World world, CollisionListener listener) {
        this.world = world;
        this.listener = listener;

        bm = ComponentMapper.getFor(BoundsComponent.class);
        mm = ComponentMapper.getFor(MovementComponent.class);
        sm = ComponentMapper.getFor(StateComponent.class);
        tm = ComponentMapper.getFor(TransformComponent.class);
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = engine;

        players = engine.getEntitiesFor(Family.getFor(PlayerComponent.class, BoundsComponent.class, TransformComponent.class, StateComponent.class));
//        coins = engine.getEntitiesFor(Family.getFor(CoinComponent.class, BoundsComponent.class));
//        squirrels = engine.getEntitiesFor(Family.getFor(SquirrelComponent.class, BoundsComponent.class));
//        springs = engine.getEntitiesFor(Family.getFor(SpringComponent.class, BoundsComponent.class, TransformComponent.class));
//        castles = engine.getEntitiesFor(Family.getFor(CastleComponent.class, BoundsComponent.class));
//        platforms = engine.getEntitiesFor(Family.getFor(PlatformComponent.class, BoundsComponent.class, TransformComponent.class));
    }

    @Override
    public void update(float deltaTime) {
        PlayerSystem playerSystem = engine.getSystem(PlayerSystem.class);
//        PlatformSystem platformSystem = engine.getSystem(PlatformSystem.class);

        for (int i = 0; i < players.size(); ++i) {
            Entity bob = players.get(i);

//            StateComponent bobState = sm.get(bob);
//
//            if (bobState.get() == PlayerComponent.STATE_HIT) {
//                continue;
//            }
//
//            MovementComponent playerMov = mm.get(bob);
//            BoundsComponent playerBounds = bm.get(bob);
//
//            if (playerMov.velocity.y < 0.0f) {
//                TransformComponent bobPos = tm.get(bob);
//
//                for (int j = 0; j < platforms.size(); ++j) {
//                    Entity platform = platforms.get(j);
//
//                    TransformComponent platPos = tm.get(platform);
//
//                    if (bobPos.pos.y > platPos.pos.y) {
//                        BoundsComponent platBounds = bm.get(platform);
//
//                        if (playerBounds.bounds.overlaps(platBounds.bounds)) {
//                            playerSystem.hitPlatform(bob);
//                            listener.jump();
//                            if (rand.nextFloat() > 0.5f) {
//                                platformSystem.pulverize(platform);
//                            }
//
//                            break;
//                        }
//                    }
//                }
//
//                for (int j = 0; j < springs.size(); ++j) {
//                    Entity spring = springs.get(j);
//
//                    TransformComponent springPos = tm.get(spring);
//                    BoundsComponent springBounds = bm.get(spring);
//
//                    if (bobPos.pos.y > springPos.pos.y) {
//                        if (playerBounds.bounds.overlaps(springBounds.bounds)) {
//                            bobSystem.hitSpring(bob);
//                            listener.highJump();
//                        }
//                    }
//                }
//            };
//
//            for (int j = 0; j < squirrels.size(); ++j) {
//                Entity squirrel = squirrels.get(j);
//
//                BoundsComponent squirrelBounds = bm.get(squirrel);
//
//                if (squirrelBounds.bounds.overlaps(playerBounds.bounds)) {
//                    bobSystem.hitSquirrel(bob);
//                    listener.hit();
//                }
//            }
//
//            for (int j = 0; j < coins.size(); ++j) {
//                Entity coin = coins.get(j);
//
//                BoundsComponent coinBounds = bm.get(coin);
//
//                if (coinBounds.bounds.overlaps(playerBounds.bounds)) {
//                    engine.removeEntity(coin);
//                    listener.coin();
//                    world.score += CoinComponent.SCORE;
//                }
//            }
//
//            for (int j = 0; j < castles.size(); ++j) {
//                Entity castle = castles.get(j);
//
//                BoundsComponent castleBounds = bm.get(castle);
//
//                if (castleBounds.bounds.overlaps(playerBounds.bounds)) {
//                    world.state = World.WORLD_STATE_NEXT_LEVEL;
//                }
//            }
        }
    }
}