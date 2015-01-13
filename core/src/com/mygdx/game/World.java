package com.mygdx.game;


import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.actor.Player;
import com.mygdx.game.component.*;
import com.mygdx.game.component.basecomponent.State;
import com.mygdx.game.net.NetworkEntity;
import com.mygdx.game.system.RenderingSystem;

import javax.xml.soap.Text;
import java.security.DigestException;
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

    public World (Engine engine) {
        this.engine = engine;
        this.rand = new Random();
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

        BoundsComponent bounds = new BoundsComponent(30, 60);
//        bounds.bounds.width = transform.width;
//        bounds.bounds.height = transform.height;
        TextureComponent texture = new TextureComponent(
                animation.animations.get(State.SOUTH).getKeyFrame(0));
        TiledMap map = Assets.currentMap;
        MapProperties spawn_point = map.getLayers().get("Spawn").getObjects().get(0).getProperties();
        float x = spawn_point.get("x", Float.class) * RenderingSystem.unitScale;
        float y = spawn_point.get("y", Float.class) * RenderingSystem.unitScale;
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
            if (obj instanceof RectangleMapObject) {
                Entity e = new Entity();
                WallComponent w = new WallComponent();
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                BoundsComponent b = new BoundsComponent(r);

                // comment out to stop drawing walls
                TransformComponent t = new TransformComponent(); e.add(t);
                e.add(w);
                e.add(b);
                engine.addEntity(e);
            } else {
                Gdx.app.log("Error", "Cannot use non rectangle walls yet");
            }
        }
    }
}