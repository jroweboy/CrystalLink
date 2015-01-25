package com.mygdx.game;


import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.mygdx.game.component.*;
import com.mygdx.game.component.basecomponent.State;
import com.mygdx.game.system.PhysicsSystem;
import com.mygdx.game.system.RenderingSystem;

import java.util.Random;


public class World {
    public static final float WIDTH = 40;
    public static final float HEIGHT = 30;
    public static final float PIXEL_WIDTH = WIDTH * 32;
    public static final float PIXEL_HEIGHT = HEIGHT * 32;
    public static final int WORLD_STATE_RUNNING = 0;
    public static final int WORLD_STATE_NEXT_LEVEL = 1;
    public static final int WORLD_STATE_GAME_OVER = 2;

    public final Random rand;
    public int state;
    private Engine engine;
    private PhysicsSystem physicsSystem;

    public World (Engine engine, PhysicsSystem physicsSystem) {
        this.engine = engine;
        this.rand = new Random();
        this.physicsSystem = physicsSystem;
    }

    public void create() {
        TiledMap map = generateLevel();
        Entity player = createPlayer();
        createCamera(player);
        createBackground();
        createWalls(map);
        this.state = WORLD_STATE_RUNNING;
    }

    private TiledMap generateLevel () {
        return Assets.loadLevel("AdventurerPath.tmx");
    }

    public Entity createPlayer() {
        Entity entity = new Entity();

        AnimationComponent animation = new AnimationComponent();
        PlayerComponent player = new PlayerComponent();
        MovementComponent movement = new MovementComponent();
        TransformComponent transform = new TransformComponent();
        StateComponent state = new StateComponent();

        animation.animations.put(State.NORTH, Assets.playerWalkNorth);
        animation.animations.put(State.SOUTH, Assets.playerWalkSouth);
        animation.animations.put(State.EAST, Assets.playerWalkEast);
        animation.animations.put(State.WEST, Assets.playerWalkWest);

        TextureComponent texture = new TextureComponent(
                animation.animations.get(State.SOUTH).getKeyFrame(0));
        TiledMap map = Assets.currentMap;
        MapProperties spawn_point = map.getLayers().get("Spawn").getObjects().get(0).getProperties();
        float x = spawn_point.get("x", Float.class) * RenderingSystem.unitScale;
        float y = spawn_point.get("y", Float.class) * RenderingSystem.unitScale;
        // TODO change the bounding box offset to be dynamic and not hardcoded?
        CollisionComponent bounds = new CollisionComponent(getRectangle(new RectangleMapObject(-15, -24, 30, 24)));
        transform.c.pos.set(x, y, 0);

        state.set(State.STATE_IDLE);

        entity.add(animation);
        entity.add(player);
        entity.add(bounds);
        entity.add(movement);
        entity.add(transform);
        entity.add(state);
        entity.add(texture);

        engine.addEntity(entity);
        physicsSystem.createMovableBody(entity);

        return entity;
    }

    private void createCamera(Entity target) {
        Entity entity = new Entity();

        CameraComponent camera = new CameraComponent();
        camera.camera = engine.getSystem(RenderingSystem.class).getCamera();
        camera.target = target;

        entity.add(camera);

        engine.addEntity(entity);
    }

    private void createBackground() {
        Entity entity = new Entity();
        BackgroundComponent background = new BackgroundComponent();
        TransformComponent position = new TransformComponent();
        background.tiledmap = Assets.currentMap;

        entity.add(background);
        entity.add(position);

        engine.addEntity(entity);
    }

    private void createWalls(TiledMap map) {
        MapObjects objs = map.getLayers().get("Collisions").getObjects();
        for (MapObject obj : objs) {
            Shape shape;
            if (obj instanceof RectangleMapObject) {
                shape = getRectangle((RectangleMapObject)obj);
            } else if (obj instanceof PolygonMapObject) {
                shape = getPolygon((PolygonMapObject)obj);
            } else if (obj instanceof PolylineMapObject) {
                shape = getPolyline((PolylineMapObject)obj);
            } else if (obj instanceof CircleMapObject) {
                shape = getCircle((CircleMapObject)obj);
            } else {
                Gdx.app.log("World Collisions", "Warning cannot use " + obj.getClass() + " as a collision right now");
                continue;
            }

            Entity e = new Entity();
            WallComponent w = new WallComponent();
            CollisionComponent b = new CollisionComponent(shape);
            e.add(w);
            e.add(b);
            physicsSystem.createStationaryBody(e);
            engine.addEntity(e);
        }
    }

    private static PolygonShape getRectangle(RectangleMapObject rectangleObject) {
        Rectangle rectangle = rectangleObject.getRectangle();
        PolygonShape polygon = new PolygonShape();
        Vector2 size = new Vector2((rectangle.x + rectangle.width * 0.5f) * RenderingSystem.unitScale,
                (rectangle.y + rectangle.height * 0.5f ) * RenderingSystem.unitScale);
        polygon.setAsBox(rectangle.width * 0.5f * RenderingSystem.unitScale,
                rectangle.height * 0.5f * RenderingSystem.unitScale,
                size,
                0.0f);
        return polygon;
    }

    private static CircleShape getCircle(CircleMapObject circleObject) {
        Circle circle = circleObject.getCircle();
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(circle.radius * RenderingSystem.unitScale);
        circleShape.setPosition(new Vector2(circle.x * RenderingSystem.unitScale, circle.y * RenderingSystem.unitScale));
        return circleShape;
    }

    private static PolygonShape getPolygon(PolygonMapObject polygonObject) {
        PolygonShape polygon = new PolygonShape();
        float[] vertices = polygonObject.getPolygon().getTransformedVertices();

        float[] worldVertices = new float[vertices.length];

        for (int i = 0; i < vertices.length; ++i) {
            worldVertices[i] = vertices[i] * RenderingSystem.unitScale;
        }

        polygon.set(worldVertices);
        return polygon;
    }

    private static ChainShape getPolyline(PolylineMapObject polylineObject) {
        float[] vertices = polylineObject.getPolyline().getTransformedVertices();
        Vector2[] worldVertices = new Vector2[vertices.length / 2];

        for (int i = 0; i < vertices.length / 2; ++i) {
            worldVertices[i] = new Vector2();
            worldVertices[i].x = vertices[i * 2] * RenderingSystem.unitScale;
            worldVertices[i].y = vertices[i * 2 + 1] * RenderingSystem.unitScale;
        }

        ChainShape chain = new ChainShape();
        chain.createChain(worldVertices);
        return chain;
    }
}