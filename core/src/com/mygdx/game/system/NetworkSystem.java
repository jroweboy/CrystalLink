package com.mygdx.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.game.net.NetworkTransformComponent;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

public class NetworkSystem { // extends IteratingSystem {
    private NetworkSystem() {
//        super(Family.getFor(NetworkTransformComponent.class));
    }

    public static NetworkSystem instance = new NetworkSystem();

    public static boolean isConnected = false;
    public static boolean isServer = false;
    public static boolean isClient = false;

    private static GatewayDevice activeGW;


    private static int DEFAULT_TCP_PORT = 59919;
    private static int DEFAULT_UDP_PORT = 59920;
    private static Socket socket;

    // map of object id to the entity
    private static Map<Integer, Entity> entityMap;


//    @Override
//    protected void processEntity(Entity entity, float deltaTime) {
//        for (Entity e : entityMap.values()) {
//            if (entity.getId() == e.getId())
//        }
//    }

    public boolean startServer() {

        Server server = new Server();
        server.start();
        try {
            server.bind(DEFAULT_TCP_PORT, DEFAULT_UDP_PORT);
        } catch (IOException e) {
            return false;
        }
        isServer = true;
        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof NetworkTransformComponent) {
                    NetworkTransformComponent pos = (NetworkTransformComponent) object;
//                    pos.entityId;
//                    System.out.println(request.text);
//                    SomeResponse response = new SomeResponse();
//                    response.text = "Thanks";
//                    connection.sendTCP(response);
                }
            }
        });
        return true;
    }

    private void upnpMapPort(int port) {
        if (port == 0) {
            port = DEFAULT_TCP_PORT;
        }

        addLogLine("Starting weupnp");

        GatewayDiscover gatewayDiscover = new GatewayDiscover();
        addLogLine("Looking for Gateway Devices...");

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
            addLogLine("No gateways found");
            addLogLine("Stopping weupnp");
            return;
        }
        addLogLine(gateways.size() + " gateway(s) found\n");

        int counter = 0;
        for (GatewayDevice gw : gateways.values()) {
            counter++;
            addLogLine("Listing gateway details of device #" + counter +
                    "\n\tFriendly name: " + gw.getFriendlyName() +
                    "\n\tPresentation URL: " + gw.getPresentationURL() +
                    "\n\tModel name: " + gw.getModelName() +
                    "\n\tModel number: " + gw.getModelNumber() +
                    "\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress() + "\n");
        }

        // choose the first active gateway for the tests
        activeGW = gatewayDiscover.getValidGateway();

        if (null != activeGW) {
            addLogLine("Using gateway: " + activeGW.getFriendlyName());
        } else {
            addLogLine("No active gateway device found");
            addLogLine("Stopping weupnp");
            return;
        }


        // testing PortMappingNumberOfEntries
//        Integer portMapCount = activeGW.getPortMappingNumberOfEntries();
//        addLogLine("GetPortMappingNumberOfEntries: " + (portMapCount!=null?portMapCount.toString():"(unsupported)"));
//
//        // testing getGenericPortMappingEntry
        PortMappingEntry portMapping = new PortMappingEntry();
//        if (LIST_ALL_MAPPINGS) {
//            int pmCount = 0;
//            do {
//                if (activeGW.getGenericPortMappingEntry(pmCount,portMapping))
//                    addLogLine("Portmapping #"+pmCount+" successfully retrieved ("+portMapping.getPortMappingDescription()+":"+portMapping.getExternalPort()+")");
//                else{
//                    addLogLine("Portmapping #"+pmCount+" retrieval failed");
//                    break;
//                }
//                pmCount++;
//            } while (portMapping!=null);
//        } else {
//            if (activeGW.getGenericPortMappingEntry(0,portMapping))
//                addLogLine("Portmapping #0 successfully retrieved ("+portMapping.getPortMappingDescription()+":"+portMapping.getExternalPort()+")");
//            else
//                addLogLine("Portmapping #0 retrival failed");
//        }
//
        InetAddress localAddress = activeGW.getLocalAddress();
//        addLogLine("Using local address: "+ localAddress.getHostAddress());
//        String externalIPAddress = activeGW.getExternalIPAddress();
//        addLogLine("External address: "+ externalIPAddress);

        addLogLine("Querying device to see if a port mapping already exists for port " + port);

        try {
            if (activeGW.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                addLogLine("Port " + port + " is already mapped. Aborting test.");
                return;
            } else {
                addLogLine("Mapping free. Sending port mapping request for port " + port);

                // test static lease duration mapping
                if (activeGW.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", "test")) {
                    addLogLine("Mapping SUCCESSFUL.");
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
            port = DEFAULT_TCP_PORT;
        }
        try {
            if (activeGW.deletePortMapping(port,"TCP")) {
                addLogLine("Port mapping removed, test SUCCESSFUL");
            } else {
                addLogLine("Port mapping removal FAILED");
            }
        } catch (IOException e) {

        } catch (SAXException e) {

        }
        addLogLine("Stopping weupnp");
    }

    private void addLogLine(String line) {
        Gdx.app.log("Weupnp", line);
    }
}
