package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.net.GameClient;
import com.mygdx.game.net.GameServer;

public class CrystalLink extends Game {
	public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager manager;
    public GameServer server;
    public GameClient client;
    public Assets assets = Assets.get();

	@Override
	public void create() {
//        Log.set(Log.LEVEL_DEBUG);
        batch = new SpriteBatch();
        manager = new AssetManager();
        assets.loadBasicRequirements(manager);
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
