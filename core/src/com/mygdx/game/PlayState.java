package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.actor.Player;
import com.mygdx.game.input.PlayerInputProcessor;

/**
 * Created by jrowe7 on 11/27/14.
 */
public class PlayState extends ScreenAdapter {
    private CrystalLink game;
    private OrthographicCamera camera;
    //    private float cameraFollowLerp = 0.1f;
    private OrthogonalTiledMapRenderer renderer;
    private TiledMap map;
    private Stage stage;

    public PlayState(CrystalLink game){
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 32, 32);
        float unitScale = 1 / 16f;
//        map = Assets.manager.get("AdventurerPath.tmx");
        map = Assets.loadLevel("AdventurerPath.tmx");
        MapProperties spawn_point = map.getLayers().get("Spawn").getObjects().get(0).getProperties();
        float x = spawn_point.get("x", Float.class) * unitScale;
        float y = spawn_point.get("y", Float.class) * unitScale;
        camera.position.x = x;
        camera.position.y = y;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);
        stage = new Stage();

        game.player = new Player(game, new Vector2(x, y));
        PlayerInputProcessor inputProcessor = new PlayerInputProcessor(game.player);
        Gdx.input.setInputProcessor(inputProcessor);
        stage.addActor(game.player);
//        stage.setViewport(WIDTH, HEIGHT, false);
    }

    @Override
    public void dispose(){
        renderer.dispose();
        map.dispose();
        stage.dispose();
    }

    @Override
    public void render(float dt){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the camera to update its matrices.
        Vector3 position = camera.position;
        position.x = game.player.position.x;
        position.y = game.player.position.y;
//        position.x = (game.player.position.x - position.x) * cameraFollowLerp;
//        position.y = (game.player.position.y - position.y) * cameraFollowLerp;
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);
        renderer.setView(camera);
        renderer.render();
        stage.act(dt);
        stage.draw();

    }
}
