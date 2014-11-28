package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/**
 * Created by jrowe7 on 11/27/14.
 */
public class PlayState extends ScreenAdapter {
    private CrystalLink game;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer renderer;
    private TiledMap map;

    public PlayState(CrystalLink game){
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 32, 32);

        float unitScale = 1 / 32f;
        map = game.assetManager.get("AdventurerPath.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);
    }

    @Override
    public void dispose(){
        renderer.dispose();
        map.dispose();
    }

    @Override
    public void render(float dt){

        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);
        renderer.setView(camera);
        renderer.render();
    }
}
