package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class Assets {
//    public static Texture background;
//    public static TextureRegion backgroundRegion;

    public static AssetManager manager;

    public static TiledMap currentMap;
//    public static TextureAtlas spriteSheet;
    public static Animation playerWalkNorth;
    public static Animation playerWalkWest;
    public static Animation playerWalkEast;
    public static Animation playerWalkSouth;


    public static Texture loadTexture (String file) {
        return new Texture(Gdx.files.internal(file));
    }

    public static TiledMap loadLevel(String file) {
        Assets.currentMap = manager.get(file);
        return Assets.currentMap;
    }

    public static void setupAfterLoad() {
        float frameLength = 1.0f / 16;
        TextureAtlas player_walk = manager.get("HighFantasyInUse/raen_walk.txt");
        playerWalkSouth = new Animation(frameLength, player_walk.createSprites("Raen_W_D"));
        playerWalkNorth = new Animation(frameLength, player_walk.createSprites("Raen_W_U"));
        playerWalkEast = new Animation(frameLength, player_walk.createSprites("Raen_W_L"));
        playerWalkWest = new Animation(frameLength, player_walk.createSprites("Raen_W_R"));
//        playerWalkSouth = new Animation(frameLength, spriteSheet.createSprites("soldier_d"));
//        playerWalkNorth = new Animation(frameLength, spriteSheet.createSprites("soldier_u"));
//        playerWalkWest = new Animation(frameLength, spriteSheet.createSprites("soldier_r"));
//        playerWalkEast = new Animation(frameLength, spriteSheet.createSprites("soldier_l"));
    }

    public static void loadMain(AssetManager manager) {
        // load every thing needed from
        Assets.manager = manager;
        manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
//        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
//        params.textureMinFilter = Texture.TextureFilter.MipMapNearestLinear;
//        params.textureMagFilter = Texture.TextureFilter.MipMapNearestLinear;
//        manager.load("AdventurerPath.tmx", TiledMap.class, new TmxMapLoader.Parameters());
        manager.load("NewTiles.tmx", TiledMap.class, new TmxMapLoader.Parameters());
//        manager.load("soldier.png", Texture.class);
//        manager.load("soldier.txt", TextureAtlas.class);
        manager.load("HighFantasyInUse/raen_walk.txt", TextureAtlas.class);
        manager.load("HighFantasyInUse/raen_walk.png", Texture.class);
    }

    public static void playSound (Sound sound) {
        if (Settings.soundEnabled) sound.play(1);
    }
}