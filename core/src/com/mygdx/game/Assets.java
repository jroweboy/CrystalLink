package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import java.util.HashMap;
import java.util.Observable;

// Classes can listen for changes to be loaded and for changes in the map
// they can be registered to be notified when the map changes
public final class Assets extends Observable {
//    public static Texture background;
//    public static TextureRegion backgroundRegion;

    private static class AssetSingleton {
        private static final Assets INSTANCE = new Assets();
    }

    private Assets() {
        if (AssetSingleton.INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static Assets get() {
        return AssetSingleton.INSTANCE;
    }


    public AssetManager manager;

    public TiledMap currentMap;
//    public static TextureAtlas spriteSheet;
    public HashMap<String, Animation> animations = new HashMap<String, Animation>();

    public Texture loadTexture (String file) {
        return new Texture(Gdx.files.internal(file));
    }

    private void loadConnectedMaps() {
//        for (MapObject obj : this.currentMap.getLayers().get("Exits").getObjects()) {
//            // TODO: clear out older unused maps
//            manager.load(obj.getName(), TiledMap.class, new TmxMapLoader.Parameters());
//        }
    }

    public TiledMap loadLevel(String file) {
        if (!manager.isLoaded(file)){
            manager.load(file, TiledMap.class, new TmxMapLoader.Parameters());
            manager.finishLoading();
        }
        this.currentMap = manager.get(file);
        loadConnectedMaps();
        setChanged();
        notifyObservers(currentMap);
        return this.currentMap;
    }

    public void setupAfterLoad() {
        float frameLength = 1.0f / 16;
        TextureAtlas player_walk = manager.get("chars/raen_walk.txt");
        animations.put("playerWalkSouth", new Animation(frameLength, player_walk.createSprites("Raen_W_D")));
        animations.put("playerWalkNorth", new Animation(frameLength, player_walk.createSprites("Raen_W_U")));
        animations.put("playerWalkEast", new Animation(frameLength, player_walk.createSprites("Raen_W_L")));
        animations.put("playerWalkWest", new Animation(frameLength, player_walk.createSprites("Raen_W_R")));

//        playerWalkSouth = new Animation(frameLength, player_walk.createSprites("Raen_W_D"));
//        playerWalkNorth = new Animation(frameLength, player_walk.createSprites("Raen_W_U"));
//        playerWalkEast = new Animation(frameLength, player_walk.createSprites("Raen_W_L"));
//        playerWalkWest = new Animation(frameLength, player_walk.createSprites("Raen_W_R"));
//        playerWalkSouth = new Animation(frameLength, spriteSheet.createSprites("soldier_d"));
//        playerWalkNorth = new Animation(frameLength, spriteSheet.createSprites("soldier_u"));
//        playerWalkWest = new Animation(frameLength, spriteSheet.createSprites("soldier_r"));
//        playerWalkEast = new Animation(frameLength, spriteSheet.createSprites("soldier_l"));
        TextureAtlas dragonfly = manager.get("chars/dragonfly.txt");
        animations.put("dragonfly_south", new Animation(frameLength, dragonfly.createSprites("d")));
        animations.put("dragonfly_north", new Animation(frameLength, dragonfly.createSprites("u")));
        animations.put("dragonfly_east", new Animation(frameLength, dragonfly.createSprites("l")));
        animations.put("dragonfly_west", new Animation(frameLength, dragonfly.createSprites("r")));
    }

    public void loadBasicRequirements(AssetManager m) {
        this.manager = m;
        // load every thing needed for basic play
        manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
//        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
//        params.textureMinFilter = Texture.TextureFilter.MipMapNearestLinear;
//        params.textureMagFilter = Texture.TextureFilter.MipMapNearestLinear;
//        manager.load("AdventurerPath.tmx", TiledMap.class, new TmxMapLoader.Parameters());
        manager.load("maps/NewTiles.tmx", TiledMap.class, new TmxMapLoader.Parameters());
//        manager.load("soldier.png", Texture.class);
//        manager.load("soldier.txt", TextureAtlas.class);
        manager.load("chars/raen_walk.txt", TextureAtlas.class);
        manager.load("chars/raen_walk.png", Texture.class);
        manager.load("chars/dragonfly.txt", TextureAtlas.class);
        manager.load("chars/dragonfly.png", Texture.class);
        manager.load("chars/dragonfly_n.png", Texture.class);
    }

    public void playSound (Sound sound) {
        if (Settings.soundEnabled) sound.play(1);
    }
}