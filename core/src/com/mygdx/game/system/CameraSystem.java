package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.mygdx.game.Assets;
import com.mygdx.game.component.CameraComponent;
import com.mygdx.game.component.TransformComponent;

import java.util.Observable;
import java.util.Observer;

public class CameraSystem extends IteratingSystem implements Observer {

    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<CameraComponent> cm;

    private float leftBound;
    private float rightBound;
    private float topBound;
    private float bottomBound;
    private int width;
    private int height;
    private int map_width;
    private int map_height;
    private TiledMap currentMap;

    public CameraSystem() {
        super(Family.getFor(CameraComponent.class));

        tm = ComponentMapper.getFor(TransformComponent.class);
        cm = ComponentMapper.getFor(CameraComponent.class);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        CameraComponent cam = cm.get(entity);

        if (cam.target == null) {
            return;
        }

        TransformComponent target = tm.get(cam.target);

        if (target == null) {
            return;
        }

        // sometimes resize gets called and the map isn't loaded yet so we need to update this when the map is loaded
        if (topBound == 0) {
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // if the screen witdh is bigger than the map width we center it on the screen
        if (map_width * 2 < width * RenderingSystem.unitScale) {
            cam.camera.position.x = map_width;
        } else {
            cam.camera.position.x = Math.min(Math.max(target.c.pos.x, leftBound), rightBound);
        }
        if (map_height * 2 < height * RenderingSystem.unitScale) {
            cam.camera.position.y = map_height;
        } else {
            cam.camera.position.y = Math.min(Math.max(target.c.pos.y, bottomBound), topBound);
        }
        // used to debug the camera
//        cam.camera.position.x = target.c.pos.x;
//        cam.camera.position.y = target.c.pos.y;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        if (currentMap != null) {
            leftBound = width / 2.0f * RenderingSystem.unitScale;
            bottomBound = height / 2.0f * RenderingSystem.unitScale;
            map_width = currentMap.getProperties().get("width", Integer.class);
            map_height = currentMap.getProperties().get("height", Integer.class);
//            Gdx.app.log("CameraSystem", "w " + map_width + " h " + map_height);
            topBound = map_height * 2 - bottomBound;
            rightBound = map_width * 2 - leftBound;
        }
//        Gdx.app.log("CameraSystem", "l " + leftBound + " r " + rightBound + " u " + topBound + " d " + bottomBound);
    }
    private void onMapLoad(TiledMap map) {
        currentMap = map;
        resize(width, height);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof TiledMap) {
            onMapLoad((TiledMap) arg);
        }
    }
}