package com.mygdx.game.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IntervalIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.LongMap;
import com.mygdx.game.CrystalLink;
import com.mygdx.game.component.PlayerComponent;
import com.mygdx.game.component.StateComponent;
import com.mygdx.game.component.TransformComponent;
import com.mygdx.game.component.basecomponent.NetworkComponent;
import com.mygdx.game.component.basecomponent.State;
import com.mygdx.game.component.basecomponent.Transform;
import com.mygdx.game.net.NetworkEntity;

public class NetworkSystem extends IntervalIteratingSystem {

    private static LongMap<NetworkEntity> networkEntities = new LongMap<NetworkEntity>();
    private ComponentMapper<NetworkComponent> nm;
    private CrystalLink game;
    private Engine engine;

    public NetworkSystem(CrystalLink game, Engine engine) {
        // network updates 10 times a second instead of whatever framerate it was before
        super(Family.getFor(NetworkComponent.class), 1.0f / 60.0f);
        nm = ComponentMapper.getFor(NetworkComponent.class);
        this.game = game;
        this.engine = engine;
    }

    @Override
    protected void processEntity(Entity entity) {
        NetworkEntity toSend;
        if (entity.getComponent(PlayerComponent.class) != null) {
            long id = nm.get(entity).id;
            if (networkEntities.containsKey(id)) {
                toSend = networkEntities.get(id);
            } else {
                Gdx.app.log("NetworkSystem", "NetworkEntity " + id + " not found so making a new NetworkEntity");
                toSend = NetworkEntity.createPlayer();
            }
            ((Transform)toSend.components[0]).set(entity.getComponent(TransformComponent.class));
            ((State)toSend.components[1]).set(entity.getComponent(StateComponent.class));
        } else {
            // unknown type to make a network entity out of
            Gdx.app.log("Network", "Unknown entity type " + entity.getComponents().toArray());
            return;
        }

        if (game.server.server != null) {
            game.server.server.sendToAllUDP(toSend);
        } else if (game.client.client != null) {
            game.client.client.sendUDP(toSend);
        }
    }

    public static void addEntity(NetworkEntity e) {
        networkEntities.put(e.id, e);
    }
}
