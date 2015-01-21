package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.ComponentType;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.mygdx.game.component.*;

import java.util.Set;


/**
 * Physics System is responsible for preventing two objects from overlapping such as preventing players from
 * walking through walls and etc. This is different than CollisionSystem, who's job it is to report collisions and
 * handle things like taking damage and talking with npcs and so on
 */
public class PhysicsSystem extends IteratingSystem {
//    private LongMap<Entity> bodies;
    private Array<Entity> bodies;
    public World world;
    private ComponentMapper<CollisionComponent> cm;
    private ComponentMapper<WallComponent> wm;
    private ComponentMapper<PlayerComponent> pm;

    public static final float BOX2D_TIME_STEP = 1.0f / 16;
    public static final int BOX2D_VELOCITY_ITER = 5;
    public static final int BOX2D_POSITION_ITER = 5;

    public PhysicsSystem() {
        super(Family.getFor(
                ComponentType.getBitsFor(CollisionComponent.class),
                ComponentType.getBitsFor(WallComponent.class, PlayerComponent.class),
                new Bits()));
        cm = ComponentMapper.getFor(CollisionComponent.class);
        wm = ComponentMapper.getFor(WallComponent.class);
        pm = ComponentMapper.getFor(PlayerComponent.class);
        bodies = new Array<Entity>();
        // Zero gravity, and true means bodies can sleep... whatever that means :p
        world = new World(Vector2.Zero, true);
    }
    @Override
    public void processEntity(Entity entity, float deltaTime) {
        if (pm.get(entity) != null) {
            // only the movable bodys need up dating
            bodies.add(entity);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        for (Entity entity : bodies) {
            CollisionComponent col = cm.get(entity);
            MovementComponent mv = entity.getComponent(MovementComponent.class);
            col.body.setLinearVelocity(mv.velocity.scl(50.0f));
        }

        world.step(BOX2D_TIME_STEP, BOX2D_VELOCITY_ITER, BOX2D_POSITION_ITER);

        for (Entity entity : bodies) {
            TransformComponent tr = entity.getComponent(TransformComponent.class);
            CollisionComponent col = cm.get(entity);

            tr.c.pos.x = (col.body.getPosition().x) + col.bounds.x;
            tr.c.pos.y = (col.body.getPosition().y) + col.bounds.y;
        }
        bodies.clear();
    }

    private Body createBody(float x, float y, float w, float h) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w / 2, h / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setTransform(x, y, 0);

        shape.dispose();

        return body;
    }

    public void createStationaryBody(Entity e) {
        CollisionComponent col = cm.get(e);
//        Gdx.app.log("Physics", "Wall coords: " + col.bounds.x + " " + col.bounds.y);
        float width = col.bounds.width * RenderingSystem.unitScale;
        float height = col.bounds.height * RenderingSystem.unitScale;
        float x = col.bounds.x * RenderingSystem.unitScale + width / 2;
        float y = col.bounds.y * RenderingSystem.unitScale + height / 2;
        Body body = createBody(x, y, width, height);
        body.setType(BodyDef.BodyType.StaticBody);
        col.body = body;
    }

    public void createMovableBody(Entity e) {
        CollisionComponent col = cm.get(e);
        TransformComponent tr = e.getComponent(TransformComponent.class);
//        Gdx.app.log("Physics", "Player coords: " + tr.c.pos.x + " " + tr.c.pos.y);
        Body body = createBody(
                tr.c.pos.x + col.bounds.x, tr.c.pos.y + col.bounds.y,
                col.bounds.width * RenderingSystem.unitScale, col.bounds.height * RenderingSystem.unitScale);
        body.setType(BodyDef.BodyType.DynamicBody);
        body.setFixedRotation(true);
        col.body = body;
    }
}
