package com.mygdx.game.net;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygdx.game.GameScreen;
import com.mygdx.game.actor.Player;
import com.mygdx.game.component.PlayerComponent;
import com.mygdx.game.component.StateComponent;
import com.mygdx.game.component.TransformComponent;
import com.mygdx.game.component.basecomponent.NetworkComponent;
import com.mygdx.game.component.basecomponent.Transform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameClient {

    private static Thread thread;
    public Client client;
    public NetworkEntity entity;
    private Map<Long, Entity> connectedEntities = new HashMap<Long, Entity>();

    public GameClient() {}

    public void joinGame(final String IpAddr, final Engine engine) {
        client = new Client();
        NetworkCommon.register(client);
        //starts the client listen thread
        new Thread(client).start();

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                Entity player = engine.getEntitiesFor(Family.getFor(PlayerComponent.class)).first();
                // -1 is refering to myself
                entity = NetworkEntity.createPlayer(-1);
                NetworkComponent n = new NetworkComponent();
                player.add(n);
            }
            @Override
            public void disconnected(Connection connection) {
                Gdx.app.log("Client", "Disconnecting...");
                for (Entity e : connectedEntities.values()) {
                    engine.removeEntity(e);
                }
            }
            @Override
            public void received(Connection connection, Object object){
                if (object instanceof NetworkNewPlayer) {
                    // setup the new player and add them to the engine
                    NetworkNewPlayer n = (NetworkNewPlayer) object;
                    Entity e = new Entity();
                    NetworkCommon.setupNetworkPlayer(e);
                    e.getComponent(TransformComponent.class).set(n.transform);
                    engine.addEntity(e);
                    Gdx.app.log("Client", "Added a new player id: " + n.id + " entity: " + e);
                    connectedEntities.put(n.id, e);
                } else if (object instanceof NetworkEntity) {
                    NetworkEntity entity = (NetworkEntity) object;
                    for (Object o: entity.components) {
                        if (o instanceof Transform) {
                            Transform pos = (Transform) o;
//                            Gdx.app.log("Client", "Position update: " + entity.id + " " + pos.pos.x + " " + pos.pos.y);
                            TransformComponent p = connectedEntities.get(entity.id).getComponent(TransformComponent.class);
                            p.set(pos);
                        }  else if (o instanceof com.mygdx.game.component.basecomponent.State) {
                            com.mygdx.game.component.basecomponent.State st = (com.mygdx.game.component.basecomponent.State) o;
//                            Gdx.app.log("Client", "State update: " + entity.id + " " + st.direction);
                            StateComponent state = connectedEntities.get(entity.id).getComponent(StateComponent.class);
                            state.c.set(st.get());
                            state.c.direction = st.direction;
                        }
                    }
                } else {
                    Gdx.app.log("Client", "Case not covered. Wat is it? " + object);
                }
            }
        });

        // tries to connect in a background thread
        new Thread() {
            public void run(){
                try {
                    client.connect(5000, IpAddr, NetworkCommon.DEFAULT_TCP_PORT, NetworkCommon.DEFAULT_UDP_PORT);
//                    Gdx.graphics.setTitle("CrystalLink - Client");
                } catch (IOException e) {
                    Gdx.app.log("Client", "Failed to connect");
                }
            }
        }.start();
//        thread.start();
    }
}
