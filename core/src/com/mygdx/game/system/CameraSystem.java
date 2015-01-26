package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.mygdx.game.Assets;
import com.mygdx.game.component.CameraComponent;
import com.mygdx.game.component.TransformComponent;

public class CameraSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<CameraComponent> cm;

    private float leftBound;
    private float rightBound;
    private float topBound;
    private float bottomBound;

    public CameraSystem() {
        super(Family.getFor(CameraComponent.class));

        tm = ComponentMapper.getFor(TransformComponent.class);
        cm = ComponentMapper.getFor(CameraComponent.class);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        leftBound = width / 2.0f * RenderingSystem.unitScale;
        bottomBound = height / 2.0f * RenderingSystem.unitScale;
        if (Assets.currentMap != null) {
            int map_width = Assets.currentMap.getProperties().get("width", Integer.class);
            int map_height = Assets.currentMap.getProperties().get("height", Integer.class);
//            Gdx.app.log("CameraSystem", "w " + map_width + " h " + map_height);
            topBound = map_height * 2 - bottomBound;
            rightBound = map_width * 2 - leftBound;
        }
//        Gdx.app.log("CameraSystem", "l " + leftBound + " r " + rightBound + " u " + topBound + " d " + bottomBound);
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

        if (topBound == 0) {
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        // swap the lines out to toggle camera lock to edges or follow player camera
        cam.camera.position.x = Math.min(Math.max(target.c.pos.x, leftBound), rightBound);
        cam.camera.position.y = Math.min(Math.max(target.c.pos.y, bottomBound), topBound);
//        cam.camera.position.x = target.c.pos.x;
//        cam.camera.position.y = target.c.pos.y;
    }
}