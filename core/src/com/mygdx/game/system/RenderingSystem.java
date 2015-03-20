package com.mygdx.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.Assets;
import com.mygdx.game.OrthogonalTiledMapRendererWithSprites;
import com.mygdx.game.World;
import com.mygdx.game.component.BackgroundComponent;
import com.mygdx.game.component.CollisionComponent;
import com.mygdx.game.component.TextureComponent;
import com.mygdx.game.component.TransformComponent;

import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;


public class RenderingSystem extends IteratingSystem implements Observer {
    private SpriteBatch batch;
    private Array<Entity> renderQueue;
    private Comparator<Entity> comparator;
    private OrthographicCamera cam;
    private ScreenViewport viewport;
    private OrthogonalTiledMapRendererWithSprites tiledMapRenderer;
    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private com.badlogic.gdx.physics.box2d.World world;
    public static float unitScale = 1 / 8f;

    private FPSLogger fpsLogger = new FPSLogger();

    private ComponentMapper<TextureComponent> textureM;
    private ComponentMapper<TransformComponent> transformM;

    public RenderingSystem(SpriteBatch batch, com.badlogic.gdx.physics.box2d.World world) {
        super(Family.getFor(ComponentType.getBitsFor(TransformComponent.class),
                ComponentType.getBitsFor(BackgroundComponent.class, TextureComponent.class, CollisionComponent.class), new Bits()));
//        super(Family.getFor(TransformComponent.class, TextureComponent.class));
        textureM = ComponentMapper.getFor(TextureComponent.class);
        transformM = ComponentMapper.getFor(TransformComponent.class);
//        boundsM = ComponentMapper.getFor(CollisionComponent.class);

        renderQueue = new Array<Entity>();

        comparator = new Comparator<Entity>() {
            @Override
            public int compare(Entity entityA, Entity entityB) {
                return (int)Math.signum(transformM.get(entityB).c.pos.y -
                        transformM.get(entityA).c.pos.y);
            }
        };

        this.batch = batch;
        this.world = world;

        onMapLoad(Assets.get().currentMap);
    }

    private void onMapLoad(TiledMap map) {
        cam = new OrthographicCamera();
        viewport = new ScreenViewport(cam);
        viewport.setUnitsPerPixel(unitScale);
        cam.setToOrtho(false, World.WIDTH, World.HEIGHT);
        tiledMapRenderer = new OrthogonalTiledMapRendererWithSprites(map, unitScale, batch);
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
        for (Entity entity : renderQueue) {
            TextureComponent tex = textureM.get(entity);
            TransformComponent t = transformM.get(entity);
            if (tex != null) {
                float width = tex.region.getRegionWidth();
                float height = tex.region.getRegionHeight();
                float originX = width * 0.5f;
                float originY = height * 0.5f;
                batch.draw(tex.region,
                        t.c.pos.x - originX, t.c.pos.y - originY,
                        originX, originY,
                        width, height,
                        t.c.scale.x * unitScale, t.c.scale.y * unitScale,
                        MathUtils.radiansToDegrees * t.c.rotation);
            }
        }
        tiledMapRenderer.renderFront();
        batch.end();
//        debugRenderer.render(world, cam.combined);
        renderQueue.clear();
//        fpsLogger.log();
    }

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

    public void resize(int w, int h) {
        viewport.update(w, h , true);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof TiledMap) {
            onMapLoad((TiledMap) arg);
        }
    }
}