package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

/**
 * Created by jrowe7 on 11/28/14.
 */
public class LoadingScreen extends ScreenAdapter {
    private CrystalLink game;
    public LoadingScreen(CrystalLink game) {
        this.game = game;
    }
    public void render(float dt) {
        if (game.assetManager.update()) {
            // we are done loading, let's move to another screen!
            game.setScreen(new PlayState(game));

        } else {
            // display loading information
            float progress = game.assetManager.getProgress();
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            game.batch.begin();
            game.font.draw(game.batch, String.format("Loading: %2.0f%%", progress), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.batch.end();
        }
    }
}
