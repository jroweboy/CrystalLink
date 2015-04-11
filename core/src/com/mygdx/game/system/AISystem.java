package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentType;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Bits;
import com.mygdx.game.component.*;
import com.mygdx.game.component.basecomponent.State;
import com.mygdx.game.system.ai.TiledManhattanDistance;
import com.mygdx.game.system.ai.TiledRaycastCollisionDetector;
import com.mygdx.game.system.ai.TiledSmoothableGraphPath;
import com.mygdx.game.system.ai.diag.DiagTiledGraph;
import com.mygdx.game.system.ai.diag.DiagTiledNode;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class AISystem extends IteratingSystem implements Observer {

    TiledMap map;
    DiagTiledGraph worldMap;
    TiledSmoothableGraphPath<DiagTiledNode> path;
    TiledManhattanDistance<DiagTiledNode> heuristic;
    IndexedAStarPathFinder<DiagTiledNode> pathFinder;
    PathSmoother<DiagTiledNode, Vector2> pathSmoother;
    ArrayList<Entity> targets;
    ArrayList<Entity> actors;
    private int sizeX, sizeY;

    public AISystem() {
//        super(Family.getFor(PathFindingComponent.class));
        super(Family.getFor(
//                new Bits(),
                ComponentType.getBitsFor(TransformComponent.class),
                ComponentType.getBitsFor(PathFindingComponent.class, PlayerComponent.class),
                new Bits()));
        targets = new ArrayList<>();
        actors = new ArrayList<>();
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        for (Entity entity : actors) {
            // find the nearest player and path towards them
            // convert entity x,y to the nearest node position
            TransformComponent tr = entity.getComponent(TransformComponent.class);
            int x = Math.round(tr.c.pos.x * RenderingSystem.unitScale);
            int y = Math.round(tr.c.pos.y * RenderingSystem.unitScale);
            DiagTiledNode my_loc = worldMap.getNode(x, y);

            // search for the path we should take
            PathFindingComponent pc = entity.getComponent(PathFindingComponent.class);
            if (pc.target == null) {
                pc.target = targets.get(0);
            }
            TransformComponent target_tr = pc.target.getComponent(TransformComponent.class);
            int tar_x = Math.max(Math.min(Math.round(target_tr.c.pos.x * RenderingSystem.unitScale), sizeX), 0);
            int tar_y = Math.max(Math.min(Math.round(target_tr.c.pos.y * RenderingSystem.unitScale), sizeY), 0);
            DiagTiledNode tar_loc = worldMap.getNode(tar_x, tar_y);
            path.clear();
            pathFinder.searchNodePath(my_loc, tar_loc, heuristic, path);
            // smooth the path
            pathSmoother.smoothPath(path);
            // find the next x,y direction
            // stop allocating! start using final class vars instead :p
            Vector2 current = new Vector2(x, y);
            Vector2 direction;
            if (path.nodes.size > 1) {
                direction = new Vector2(path.nodes.get(1).x, path.nodes.get(1).y);
            } else if (path.nodes.size == 1){
                direction = new Vector2(path.nodes.get(0).x, path.nodes.get(0).y);
            } else {
                direction = new Vector2(0,0);
            }
            // do some magic to get an x y velocity
            float velocity_base = pc.velocity;
            direction.sub(current);
            direction.nor();
            direction.scl(velocity_base);
            // set the velocity in that direction
            MovementComponent mv = entity.getComponent(MovementComponent.class);
            StateComponent state = entity.getComponent(StateComponent.class);
            if (direction.isZero()) {
                state.set(State.STATE_IDLE);
            } else {
                state.set(State.STATE_WALK);
            }
//            Gdx.app.log("AISystem", "Velocity " + direction);
            mv.velocity.set(direction);
        }
        actors.clear();
        targets.clear();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (entity.getComponent(PlayerComponent.class) != null) {
//            Gdx.app.log("AISystem", "Adding target");
            targets.add(entity);
        } else {
//            Gdx.app.log("AISystem", "Adding actor");
            actors.add(entity);
        }
    }

    private void onMapLoad(TiledMap map) {

        this.map = map;

        sizeX = map.getProperties().get("width", Integer.class);
        sizeY = map.getProperties().get("height", Integer.class);
//        Gdx.app.log("AISystem", "x: " + sizeX + " y: " + sizeY);
        worldMap = new DiagTiledGraph(sizeX, sizeY);
        worldMap.init(map);
        path = new TiledSmoothableGraphPath<DiagTiledNode>();
        heuristic = new TiledManhattanDistance<DiagTiledNode>();
        pathFinder = new IndexedAStarPathFinder<DiagTiledNode>(worldMap, true);
        pathSmoother = new PathSmoother<DiagTiledNode, Vector2>(new TiledRaycastCollisionDetector<DiagTiledNode>(worldMap));
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof TiledMap) {
            onMapLoad((TiledMap) arg);
        }
    }
}
