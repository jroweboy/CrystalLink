package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.ComponentType;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.mygdx.game.Assets;
import com.mygdx.game.OrthogonalTiledMapRendererWithSprites;
import com.mygdx.game.component.BackgroundComponent;
import com.mygdx.game.component.TextureComponent;
import com.mygdx.game.component.TransformComponent;

import java.util.Comparator;


public class RenderingSystem extends IteratingSystem {
//    static final float FRUSTUM_WIDTH = 10;
//    static final float FRUSTUM_HEIGHT = 15;
//    static final float PIXELS_TO_METRES = 1.0f / 32.0f;

    private SpriteBatch batch;
    private Array<Entity> renderQueue;
    private Comparator<Entity> comparator;
    private OrthographicCamera cam;
    private OrthogonalTiledMapRendererWithSprites tiledMapRenderer;

    public static float unitScale = 1 / 16f;

    private ComponentMapper<TextureComponent> textureM;
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<BackgroundComponent> bm;

    public RenderingSystem(SpriteBatch batch) {
        super(Family.getFor(ComponentType.getBitsFor(TransformComponent.class),
                ComponentType.getBitsFor(BackgroundComponent.class, TextureComponent.class), new Bits()));

        textureM = ComponentMapper.getFor(TextureComponent.class);
        transformM = ComponentMapper.getFor(TransformComponent.class);
        bm = ComponentMapper.getFor(BackgroundComponent.class);

        renderQueue = new Array<Entity>();

        comparator = new Comparator<Entity>() {
            @Override
            public int compare(Entity entityA, Entity entityB) {
                return (int)Math.signum(transformM.get(entityB).pos.z -
                        transformM.get(entityA).pos.z);
            }
        };

        this.batch = batch;

        cam = new OrthographicCamera();
        cam.setToOrtho(false, 32, 32);


        tiledMapRenderer = new OrthogonalTiledMapRendererWithSprites(Assets.currentMap, unitScale);

//        cam = new OrthographicCamera(FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
//        cam.position.set(FRUSTUM_WIDTH / 2, FRUSTUM_HEIGHT / 2, 0);
    }

    @Override
    public void update(float deltaTime) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.update(deltaTime);
        renderQueue.sort(comparator);

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();

        for (Entity entity : renderQueue) {
            TextureComponent tex = textureM.get(entity);
//            if (tex == null || tex.region == null) {
                BackgroundComponent b = bm.get(entity);
                if (b != null && b.tiledmap != null) {
                    // do i need to make a new renderer
                    if (b.tiledmap != tiledMapRenderer.getMap()) {
                        tiledMapRenderer.setMap(b.tiledmap);
                    }
                    tiledMapRenderer.setView(cam);
                    tiledMapRenderer.render();
                }
//                continue;
//            }

//            TransformComponent t = transformM.get(entity);

//            float width = tex.region.getRegionWidth();
//            float height = tex.region.getRegionHeight();
//            float originX = width * 0.5f;
//            float originY = height * 0.5f;

//            batch.draw(tex.region,0,0);
//            batch.draw(tex.region,
//                    t.pos.x - originX, t.pos.y - originY,
////                    0,0,
//                    originX, originY,
//                    width, height,
//                    t.scale.x * unitScale, t.scale.y * unitScale,
//                    MathUtils.radiansToDegrees * t.rotation);
        }

        batch.end();
        renderQueue.clear();
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        renderQueue.add(entity);
    }

    public OrthographicCamera getCamera() {
        return cam;
    }
}