package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class CrystalLink extends Game {
	public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager assetManager;

	@Override
	public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        assetManager.load("AdventurerPath.tmx", TiledMap.class);
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        this.setScreen(new LoadingScreen(this));
	}

	@Override
	public void render() {
        super.render();
    }


    public void dispose() {
        batch.dispose();
        font.dispose();
        assetManager.dispose();
    }

}
