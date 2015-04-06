package com.mygdx.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.pfa.HierarchicalPathFinder;
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.ai.pfa.PathSmootherRequest;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.PathFindingComponent;
import com.mygdx.game.system.ai.TiledGraph;
import com.mygdx.game.system.ai.TiledManhattanDistance;
import com.mygdx.game.system.ai.TiledRaycastCollisionDetector;
import com.mygdx.game.system.ai.TiledSmoothableGraphPath;
import com.mygdx.game.system.ai.diag.DiagTiledGraph;
import com.mygdx.game.system.ai.diag.DiagTiledNode;
import com.mygdx.game.system.ai.flat.FlatTiledGraph;
import com.mygdx.game.system.ai.flat.FlatTiledNode;
import com.mygdx.game.system.ai.hrchy.HierarchicalTiledGraph;
import com.mygdx.game.system.ai.hrchy.HierarchicalTiledNode;

import java.util.Observable;
import java.util.Observer;

public class AISystem extends IteratingSystem implements Observer {

    TiledMap map;
    DiagTiledGraph worldMap;
    TiledSmoothableGraphPath<DiagTiledNode> path;
    TiledManhattanDistance<DiagTiledNode> heuristic;
    IndexedAStarPathFinder<DiagTiledNode> pathFinder;
    PathSmoother<DiagTiledNode, Vector2> pathSmoother;

    public AISystem() {
        super(Family.getFor(PathFindingComponent.class));
        int sizeX = map.getProperties().get("width", Integer.class);
        int sizeY = map.getProperties().get("height", Integer.class);
        worldMap = new DiagTiledGraph(sizeX, sizeY);
        worldMap.init(map);
        path = new TiledSmoothableGraphPath<DiagTiledNode>();
        heuristic = new TiledManhattanDistance<DiagTiledNode>();
        pathFinder = new IndexedAStarPathFinder<DiagTiledNode>(worldMap, true);
        pathSmoother = new PathSmoother<DiagTiledNode, Vector2>(new TiledRaycastCollisionDetector<DiagTiledNode>(worldMap));
        
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }

    private void onMapLoad(TiledMap map) {
        this.map = map;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof TiledMap) {
            onMapLoad((TiledMap) arg);
        }
    }
}
