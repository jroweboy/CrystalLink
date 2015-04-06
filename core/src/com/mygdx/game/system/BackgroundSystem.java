package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.game.component.BackgroundComponent;
import com.mygdx.game.component.TransformComponent;
// TODO: Is this even used?
public class BackgroundSystem extends IteratingSystem {
    private OrthographicCamera camera;
//    private OrthogonalTiledMapRenderer tiledMapRenderer;

//    private ComponentMapper<BackgroundComponent> bm;
    private ComponentMapper<TransformComponent> tm;

    public BackgroundSystem() {
        super(Family.getFor(BackgroundComponent.class));
//        bm = ComponentMapper.getFor(BackgroundComponent.class);
        tm = ComponentMapper.getFor(TransformComponent.class);
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

//    public void setMap(TiledMap map) {
////        bm.getthis.tiledMapRenderer = new OrthogonalTiledMapRenderer(map);
//    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        TransformComponent t = tm.get(entity);
//        BackgroundComponent b = bm.get(entity);

//        b.tiledMapRenderer.setView(camera);
//        tiledMapRenderer.render();
        t.c.pos.set(camera.position.x, camera.position.y, 10.0f);
    }
}