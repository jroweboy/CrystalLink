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
import com.mygdx.game.component.TransformComponent;

import java.io.IOException;

public class GameClient {

    private static Thread thread;
    public Client client;
    public GameClient() {}

    public void joinGame(final String IpAddr, final Engine engine) {
        client = new Client();
        NetworkCommon.register(client);
        //starts the client listen thread
        new Thread(client).start();

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                // swap the player networked components with their counterparts
                Entity player = engine.getEntitiesFor(Family.getFor(PlayerComponent.class)).first();
//                        NetworkCommon.GameConnection player = (NetworkCommon.GameConnection) connection;
                TransformComponent t = player.getComponent(TransformComponent.class);
//                        player.remove(TransformComponent.class);
                NetworkTransformComponent n = new NetworkTransformComponent();
                n.set(t);
                player.add(n);
            }
            @Override
            public void disconnected(Connection connection) {

            }
            @Override
            public void received(Connection connection, Object object){

            }
        });

        // tries to connect in a background thread
        new Thread() {
            public void run(){
                try {
                    client.connect(5000, IpAddr, NetworkCommon.DEFAULT_TCP_PORT, NetworkCommon.DEFAULT_UDP_PORT);
                } catch (IOException e) {
                    Gdx.app.log("Client", "Failed to connect");
                }
            }
        }.start();
//        thread.start();
    }
}
