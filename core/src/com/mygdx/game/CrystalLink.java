package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.mygdx.game.actor.Player;

public class CrystalLink extends Game {
	public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager assetManager;
    public Player player;

	@Override
	public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        assetManager.load("AdventurerPath.tmx", TiledMap.class);
        assetManager.load("soldier.png", Texture.class);
        assetManager.load("soldier.txt", TextureAtlas.class);
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
