package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.ComponentType;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
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
        
//        map.getLayers().get("Exits")
    }

    private boolean isOnNextLevelSquare(Vector3 player_pos) {
        return false;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent pos = tm.get(entity);
        BackgroundComponent bc = bm.get(entity);
        if (bc != null && bc.tiledmap != null) {

        } else {
            // we must be a player
            if (isOnNextLevelSquare(pos.c.pos)) {

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
    public Vector2 pos;
    public int next_map;
}