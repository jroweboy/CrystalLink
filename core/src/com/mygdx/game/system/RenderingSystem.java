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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.mygdx.game.Assets;
import com.mygdx.game.OrthogonalTiledMapRendererWithSprites;
import com.mygdx.game.World;
import com.mygdx.game.actor.Player;
import com.mygdx.game.component.BackgroundComponent;
import com.mygdx.game.component.BoundsComponent;
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
//    private ComponentMapper<BoundsComponent> boundsM;

    public RenderingSystem(SpriteBatch batch) {
        super(Family.getFor(ComponentType.getBitsFor(TransformComponent.class),
                ComponentType.getBitsFor(BackgroundComponent.class, TextureComponent.class, BoundsComponent.class), new Bits()));
//        super(Family.getFor(TransformComponent.class, TextureComponent.class));
        textureM = ComponentMapper.getFor(TextureComponent.class);
        transformM = ComponentMapper.getFor(TransformComponent.class);
//        boundsM = ComponentMapper.getFor(BoundsComponent.class);

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
        cam.setToOrtho(false, World.WIDTH, World.HEIGHT);

        tiledMapRenderer = new OrthogonalTiledMapRendererWithSprites(Assets.currentMap, unitScale, batch);
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

        tiledMapRenderer.setView(cam);
        tiledMapRenderer.renderBack();

//        Array<Entity> wallsToRender = new Array<Entity>();

        for (Entity entity : renderQueue) {
            TextureComponent tex = textureM.get(entity);
            TransformComponent t = transformM.get(entity);
            if (tex != null) {
                float width = tex.region.getRegionWidth();
                float height = tex.region.getRegionHeight();
                float originX = width * 0.5f;
                float originY = height * 0.5f;
                batch.draw(tex.region,
                        t.pos.x - originX, t.pos.y - originY,
                        originX, originY,
                        width, height,
                        t.scale.x * unitScale, t.scale.y * unitScale,
                        MathUtils.radiansToDegrees * t.rotation);
            }
//            } else {
//                wallsToRender.add(entity);
//            }
        }
        tiledMapRenderer.renderFront();
        batch.end();
//        debugDrawWalls(wallsToRender);
        renderQueue.clear();
    }

//    private void debugDrawWalls(Array<Entity> walls) {
//        // Stub this out to disable debug wall drawing
//        // or just remove all that junk up above
//        // but i will need lots of things drawn completely after so i think i'll keep this here
//        ShapeRenderer shapeRenderer = new ShapeRenderer();
//        shapeRenderer.setProjectionMatrix(cam.combined);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
////        shapeRenderer.setProjectionMatrix(cam.combined);
//        shapeRenderer.setColor(.33f, .33f, .33f, .33f);
//        Gdx.app.log("pos", "" + walls.get(0).getComponent(BoundsComponent.class).bounds.x*unitScale);
//        for (Entity wall : walls) {
//            BoundsComponent b = wall.getComponent(BoundsComponent.class);
//            shapeRenderer.rect(b.bounds.x * unitScale, b.bounds.y * unitScale, b.bounds.width, b.bounds.height);
//        }
//        shapeRenderer.end();
//    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        BackgroundComponent bc = entity.getComponent(BackgroundComponent.class);
        if (bc != null && bc.tiledmap != null) {
            if (bc.tiledmap != tiledMapRenderer.getMap()) {
                tiledMapRenderer.setMap(bc.tiledmap);
            }
        } else {
            renderQueue.add(entity);
        }
    }

    public OrthographicCamera getCamera() {
        return cam;
    }
}