package com.mygdx.game;


import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.*;
import com.mygdx.game.system.RenderingSystem;

import javax.xml.soap.Text;
import java.util.Random;


public class World {
    public static final float WORLD_WIDTH = 32;
    public static final float WORLD_HEIGHT = 32;
    public static final int WORLD_STATE_RUNNING = 0;
    public static final int WORLD_STATE_NEXT_LEVEL = 1;
    public static final int WORLD_STATE_GAME_OVER = 2;
//    public static final Vector2 gravity = new Vector2(0, -12);

    public final Random rand;

//    public float heightSoFar;
//    public int score;
    public int state;

    private Engine engine;

    public World (Engine engine) {
        this.engine = engine;
        this.rand = new Random();
    }

    public void create() {
        generateLevel();
        Entity player = createPlayer();
        createCamera(player);
        createBackground();

//        this.heightSoFar = 0;
//        this.score = 0;
        this.state = WORLD_STATE_RUNNING;
    }

    private void generateLevel () {
        Assets.loadLevel("AdventurerPath.tmx");
    }

    private Entity createPlayer() {
        Entity entity = new Entity();

        AnimationComponent animation = new AnimationComponent();
        PlayerComponent bob = new PlayerComponent();
        BoundsComponent bounds = new BoundsComponent();
        MovementComponent movement = new MovementComponent();
        TransformComponent transform = new TransformComponent();
        StateComponent state = new StateComponent();
        TextureComponent texture = new TextureComponent();

        animation.animations.put(MovementComponent.DIRECTION_NORTH, Assets.playerWalkNorth);
        animation.animations.put(MovementComponent.DIRECTION_SOUTH, Assets.playerWalkSouth);
        animation.animations.put(MovementComponent.DIRECTION_EAST, Assets.playerWalkEast);
        animation.animations.put(MovementComponent.DIRECTION_WEST, Assets.playerWalkWest);

        bounds.bounds.width = transform.width;
        bounds.bounds.height = transform.height;
        texture.obj = new TextureMapObject(animation.animations.get(MovementComponent.DIRECTION_SOUTH).getKeyFrame(0));
        TiledMap map = Assets.currentMap;
        MapProperties spawn_point = map.getLayers().get("Spawn").getObjects().get(0).getProperties();
        float x = spawn_point.get("x", Float.class) * RenderingSystem.unitScale;
        float y = spawn_point.get("y", Float.class) * RenderingSystem.unitScale;
        transform.pos.set(x, y, 0);

        MapLayer objectLayer = map.getLayers().get("Sprites");
        objectLayer.getObjects().add(texture.obj);
        state.set(PlayerComponent.STATE_IDLE);

        entity.add(animation);
        entity.add(bob);
        entity.add(bounds);
        entity.add(movement);
        entity.add(transform);
        entity.add(state);
        entity.add(texture);

        engine.addEntity(entity);

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
//        TextureComponent texture = new TextureComponent();

//        texture.region = background.tiledmap.getTileSets().;

        entity.add(background);
        entity.add(position);
//        entity.add(texture);

        engine.addEntity(entity);
    }
}