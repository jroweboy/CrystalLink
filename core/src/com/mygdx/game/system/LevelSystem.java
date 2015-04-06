package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.ComponentType;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.mygdx.game.Assets;
import com.mygdx.game.component.BackgroundComponent;
import com.mygdx.game.component.PlayerComponent;
import com.mygdx.game.component.TransformComponent;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class LevelSystem extends IteratingSystem implements Observer{
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<BackgroundComponent> bm;

    public LevelSystem() {
        super(Family.getFor(ComponentType.getBitsFor(TransformComponent.class),
                ComponentType.getBitsFor(BackgroundComponent.class, PlayerComponent.class), new Bits()));

        tm = ComponentMapper.getFor(TransformComponent.class);
        bm = ComponentMapper.getFor(BackgroundComponent.class);
    }

    private ArrayList<LevelExit> exits = new ArrayList<LevelExit>();
    private void buildLevelExits(TiledMap map){
        exits.clear();

        MapObjects rects = map.getLayers().get("Exits").getObjects();
        for (MapObject r : rects) {
            // Only supporting rectangle exits for now others should be trivial to add.
            if (!(r instanceof RectangleMapObject)) {
                continue;
            }
            RectangleMapObject rect = (RectangleMapObject) r;
            LevelExit l = new LevelExit(rect);
            Gdx.app.log("LevelSystem", l.bounds.toString());
            exits.add(l);
        }
    }

    // Return the name of the next map or null if not on an exit
    // maybe to do? make this return an optional :p
    private String isOnNextLevelSquare(Vector3 player_pos) {
        for (LevelExit exit : exits) {
//            Gdx.app.log("LevelSystem", "x: " + player_pos.x + " y: " + player_pos.y);
            if (exit.bounds.contains(player_pos.x, player_pos.y)) {
                return exit.next_map;
            }
        }
        return null;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent pos = tm.get(entity);
        BackgroundComponent bc = bm.get(entity);
        if (bc != null && bc.tiledmap != null) {

        } else {
            // we must be a player
            String next_map = isOnNextLevelSquare(pos.c.pos);
            if (next_map != null) {
//                Gdx.app.log("LevelSystem", "YO! Time to change the level!");
                Assets.get().loadLevel(next_map);
            }
        }
    }

    private void onMapLoad(TiledMap map) {
        buildLevelExits(map);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof TiledMap) {
            onMapLoad((TiledMap) arg);
        }
    }
}

class LevelExit {
    public Rectangle bounds;
    public String next_map;

    private LevelExit(){}

    public LevelExit(RectangleMapObject r) {
        next_map = r.getName();
        bounds = r.getRectangle();
        bounds.x *= RenderingSystem.unitScale;
        bounds.y *= RenderingSystem.unitScale;
        bounds.width *= RenderingSystem.unitScale;
        bounds.height *= RenderingSystem.unitScale;
    }

}