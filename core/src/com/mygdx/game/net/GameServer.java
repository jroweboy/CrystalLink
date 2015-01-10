package com.mygdx.game.net;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.game.component.PlayerComponent;
import com.mygdx.game.component.TransformComponent;
import com.mygdx.game.net.NetworkCommon.GameConnection;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

public class GameServer {
    private static GatewayDevice activeGW;

    private static Thread thread;

    public GameServer() {}

    public void dispose() {
        upnpRemoveMap(NetworkCommon.DEFAULT_TCP_PORT);
    }

    private Server server;
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
                upnpMapPort(NetworkCommon.DEFAULT_TCP_PORT);
                // TODO make that work for UDP as well
//                upnpMapPort(DEFAULT_UDP_PORT);
                NetworkCommon.register(server);
                server.start();
                try {
                    server.bind(NetworkCommon.DEFAULT_TCP_PORT, NetworkCommon.DEFAULT_UDP_PORT);
                } catch (IOException e) {
                    return;
                }
                server.addListener(new Listener() {
                    public void connected(Connection connection) {
                        Gdx.app.log("Server", "Got a new connection!");
                        GameConnection g = (GameConnection) connection;
                        //create a packet containing the new players id
                        NetworkEntity newPlayer = new NetworkEntity();
                        g.player = newPlayer;
                        connection.sendTCP(newPlayer);
                    }

                    public void received (Connection connection, Object object) {
                        NetworkEntity player = ((GameConnection)connection).player;
//                        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.getFor(PlayerComponent.class));
//                        players.first().;
                        if (object instanceof NetworkTransformComponent) {
                            TransformComponent pos = (TransformComponent) object;
                            TransformComponent p = player.getComponent(TransformComponent.class);
                            Gdx.app.log("Server", "Position update: " + pos);
                            p.set(pos);
                        }
                    }

                    @Override
                    public void disconnected(Connection c) {
                        Gdx.app.log("Server", "Client disconnected");
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

    private void upnpMapPort(int port) {
        if (port == 0) {
            port = NetworkCommon.DEFAULT_TCP_PORT;
        }

        Gdx.app.log("GameServer", "Starting weupnp");

        GatewayDiscover gatewayDiscover = new GatewayDiscover();
        Gdx.app.log("GameServer", "Looking for Gateway Devices...");

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
        Gdx.app.log("GameServer", gateways.size() + " gateway(s) found\n");

        int counter = 0;
        for (GatewayDevice gw : gateways.values()) {
            counter++;
            Gdx.app.log("GameServer", "Listing gateway details of device #" + counter +
                    "\n\tFriendly name: " + gw.getFriendlyName() +
                    "\n\tPresentation URL: " + gw.getPresentationURL() +
                    "\n\tModel name: " + gw.getModelName() +
                    "\n\tModel number: " + gw.getModelNumber() +
                    "\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress() + "\n");
        }

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

        Gdx.app.log("GameServer", "Querying device to see if a port mapping already exists for port " + port);

        try {
            if (activeGW.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                Gdx.app.log("GameServer", "Port " + port + " is already mapped. Aborting test.");
                return;
            } else {
                Gdx.app.log("GameServer", "Mapping free. Sending port mapping request for port " + port);

                // test static lease duration mapping
                if (activeGW.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", "test")) {
                    Gdx.app.log("GameServer", "Mapping SUCCESSFUL.");
                }
            }
        } catch (IOException e) {
            return;
        } catch (SAXException e) {
            return;
        }
    }

    public void upnpRemoveMap(int port) {
        if (port == 0) {
            port = NetworkCommon.DEFAULT_TCP_PORT;
        }
        if (activeGW == null) {
            Gdx.app.log("GameServer", "Port mapping does not exist");
            return;
        }
        try {
            if (activeGW.deletePortMapping(port,"TCP")) {
                Gdx.app.log("GameServer", "Port mapping removed, test SUCCESSFUL");
            } else {
                Gdx.app.log("GameServer", "Port mapping removal FAILED");
            }
        } catch (IOException e) {

        } catch (SAXException e) {

        }
        Gdx.app.log("GameServer", "Stopping weupnp");
    }

}
