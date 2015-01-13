package com.mygdx.game.net;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.game.component.PlayerComponent;
import com.mygdx.game.component.StateComponent;
import com.mygdx.game.component.TransformComponent;
import com.mygdx.game.component.basecomponent.NetworkComponent;
import com.mygdx.game.component.basecomponent.Transform;
import com.mygdx.game.net.NetworkCommon.GameConnection;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class GameServer {
    private static GatewayDevice activeGW;

    private static Thread thread;

    public GameServer() {}

    public void dispose() {
        upnpRemoveMap(NetworkCommon.DEFAULT_TCP_PORT, "TCP");
        upnpRemoveMap(NetworkCommon.DEFAULT_TCP_PORT, "UDP");
    }

    public Server server;

    public long server_id = UUID.randomUUID().getLeastSignificantBits();
    public NetworkEntity entity = NetworkEntity.createPlayer(server_id);

    public boolean isRunning = false;

    public void startServer(final Engine engine) {
        thread = new Thread() {
            public void run() {
                server = new Server() {
                    protected Connection newConnection () {
                        // Used to store the Player's
                        return new GameConnection();
                    }
                };
                // try to upnp port forward to this machine
                upnpMapPort(NetworkCommon.DEFAULT_TCP_PORT, "TCP");
                upnpMapPort(NetworkCommon.DEFAULT_UDP_PORT, "UDP");
                NetworkCommon.register(server);
                server.start();
                try {
                    server.bind(NetworkCommon.DEFAULT_TCP_PORT, NetworkCommon.DEFAULT_UDP_PORT);
                    Entity me = engine.getEntitiesFor(Family.getFor(PlayerComponent.class)).first();
                    me.add(new NetworkComponent());
//                    Gdx.graphics.setTitle("CrystalLink - Server");
                } catch (IOException e) {
                    return;
                }
                server.addListener(new Listener() {
                    public void connected(Connection connection) {
                        Gdx.app.log("Server", "Got a new connection!");
                        GameConnection g = (GameConnection) connection;
                        //create a packet containing the new players id
                        Entity newPlayer = new Entity();
                        NetworkCommon.setupNetworkPlayer(newPlayer);

                        // send myself to the new client
                        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.getFor(PlayerComponent.class));
                        Transform t = players.first().getComponent(TransformComponent.class).c;
                        connection.sendTCP(new NetworkNewPlayer(server_id, t));
                        engine.addEntity(newPlayer);
                        g.player = newPlayer;
                    }

                    public void received (Connection connection, Object object) {
                        Entity player = ((GameConnection)connection).player;
//                        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.getFor(PlayerComponent.class));
//                        players.first().;

                        if (object instanceof TransformComponent) {
                            TransformComponent pos = (TransformComponent) object;
                            TransformComponent p = player.getComponent(TransformComponent.class);
//                            Gdx.app.log("Server", "Position update: " + pos.pos.x + " " + pos.pos.y);
                            p.set(pos);
                        } else if (object instanceof NetworkEntity) {
                            NetworkEntity entity = (NetworkEntity) object;
                            for (Object o: entity.components) {
                                if (o instanceof Transform) {
                                    Transform pos = (Transform) o;
//                                    Gdx.app.log("Server", "Position update: " + entity.id + " " + pos.pos.x + " " + pos.pos.y);
                                    TransformComponent p = player.getComponent(TransformComponent.class);
                                    p.set(pos);
                                } else if (o instanceof com.mygdx.game.component.basecomponent.State) {
                                    com.mygdx.game.component.basecomponent.State st = (com.mygdx.game.component.basecomponent.State) o;
//                                    Gdx.app.log("Server", "State update: " + entity.id + " " + st.direction);
                                    StateComponent state = player.getComponent(StateComponent.class);
                                    state.c.set(st.get());
                                    state.c.direction = st.direction;
                                }
                            }
                        }
                    }

                    @Override
                    public void disconnected(Connection connection) {
                        Gdx.app.log("Server", "Client disconnected");
                        Entity player = ((GameConnection)connection).player;
                        engine.removeEntity(player);
                    }

                });
                isRunning = true;
            }
        };
        thread.start();
    }

    public void stopServer() {
        server.stop();
        isRunning = false;
    }

    private void upnpMapPort(int port, String type) {
        if (port == 0) {
            port = NetworkCommon.DEFAULT_TCP_PORT;
        }

        Gdx.app.log("GameServer", "Starting weupnp");

        GatewayDiscover gatewayDiscover = new GatewayDiscover();
//        Gdx.app.log("GameServer", "Looking for Gateway Devices...");

        Map<InetAddress, GatewayDevice> gateways = null;
        try {
            gateways = gatewayDiscover.discover();
        } catch (IOException e) {

            return;
        } catch (SAXException e) {

            return;
        } catch (ParserConfigurationException e) {

            return;
        }

        if (gateways.isEmpty()) {
            Gdx.app.log("GameServer", "No gateways found");
            Gdx.app.log("GameServer", "Stopping weupnp");
            return;
        }
//        Gdx.app.log("GameServer", gateways.size() + " gateway(s) found\n");

//        int counter = 0;
//        for (GatewayDevice gw : gateways.values()) {
//            counter++;
//            Gdx.app.log("GameServer", "Listing gateway details of device #" + counter +
//                    "\n\tFriendly name: " + gw.getFriendlyName() +
//                    "\n\tPresentation URL: " + gw.getPresentationURL() +
//                    "\n\tModel name: " + gw.getModelName() +
//                    "\n\tModel number: " + gw.getModelNumber() +
//                    "\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress() + "\n");
//        }

        // choose the first active gateway for the tests
        activeGW = gatewayDiscover.getValidGateway();

        if (null != activeGW) {
            Gdx.app.log("GameServer", "Using gateway: " + activeGW.getFriendlyName());
        } else {
            Gdx.app.log("GameServer", "No active gateway device found");
            Gdx.app.log("GameServer", "Stopping weupnp");
            return;
        }

        PortMappingEntry portMapping = new PortMappingEntry();
        InetAddress localAddress = activeGW.getLocalAddress();

        Gdx.app.log("GameServer", "Querying device to see if a port mapping already exists for " + type + " for port " + port);

        try {
            if (activeGW.getSpecificPortMappingEntry(port, type, portMapping)) {
                Gdx.app.log("GameServer", "Port " + port + " is already mapped. Aborting test.");
                return;
            } else {
                Gdx.app.log("GameServer", "Mapping free. Sending port mapping request for " + type + " for port " + port);

                // test static lease duration mapping
                if (activeGW.addPortMapping(port, port, localAddress.getHostAddress(), type, "test")) {
                    Gdx.app.log("GameServer", "Mapping SUCCESSFUL for " + type + ".");
                }
            }
        } catch (IOException e) {
            return;
        } catch (SAXException e) {
            return;
        }
    }

    public void upnpRemoveMap(int port, String type) {
        if (port == 0) {
            port = NetworkCommon.DEFAULT_TCP_PORT;
        }
        if (activeGW == null) {
            Gdx.app.log("GameServer", "Port mapping for " + type + " does not exist");
            return;
        }
        try {
            if (activeGW.deletePortMapping(port,"TCP")) {
                Gdx.app.log("GameServer", "Port mapping for " + type + " removed, test SUCCESSFUL");
            } else {
                Gdx.app.log("GameServer", "Port mapping for " + type + " removal FAILED");
            }
        } catch (IOException e) {

        } catch (SAXException e) {

        }
        Gdx.app.log("GameServer", "Stopping weupnp");
    }

}
