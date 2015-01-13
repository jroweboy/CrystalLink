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
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.actor.Player;
import com.mygdx.game.net.GameClient;
import com.mygdx.game.net.GameServer;

public class CrystalLink extends Game {
	public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager manager;
    public Player player;
    public GameServer server;
    public GameClient client;

	@Override
	public void create() {
//        Log.set(Log.LEVEL_DEBUG);
        batch = new SpriteBatch();
        manager = new AssetManager();
        Assets.loadMain(manager);
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        server = new GameServer();
        client = new GameClient();

        this.setScreen(new LoadingScreen(this));
	}

	@Override
	public void render() {
        super.render();
    }


    public void dispose() {
        batch.dispose();
        font.dispose();
        manager.dispose();
        server.dispose();
    }

}
