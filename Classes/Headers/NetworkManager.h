#ifndef __NETWORK_MANAGER_H__
#define __NETWORK_MANAGER_H__

#include "GetTime.h"
#include "Rand.h"
#include "RakPeerInterface.h"
#include "MessageIdentifiers.h"
#include "FullyConnectedMesh2.h"
#include "TeamManager.h"
#include "Kbhit.h"
#include "RakSleep.h"
#include "RakNetTypes.h"
#include "BitStream.h"
#include "SocketLayer.h"
#include "ReplicaManager3.h"
#include "NetworkIDManager.h"
#include "Gets.h"
#include "Itoa.h"
#include "NatPunchthroughClient.h"
#include "NatTypeDetectionClient.h"
#include "miniupnpc.h"
#include "upnpcommands.h"
#include "upnperrors.h"
#include "TCPInterface.h"
#include "ReadyEvent.h"	
#include "PacketLogger.h"
#include "RPC4Plugin.h"
#include "HTTPConnection2.h"

typedef struct NetworkMger {
    RakNet::RakPeerInterface *rakPeer;
    RakNet::TeamManager *teamManager;
    RakNet::ReplicaManager3 *replicaManager3;
    RakNet::NetworkIDManager *networkIDManager;
    RakNet::TCPInterface *tcp;
    RakNet::ReadyEvent *readyEvent;
    RakNet::NatPunchthroughClient *natPunchthroughClient;
    RakNet::NatTypeDetectionClient *natTypeDetectionClient;
    RakNet::RPC4 *rpc4;
    RakNet::FullyConnectedMesh2 *fullyConnectedMesh2;
    RakNet::HTTPConnection2 *httpConnection2;
    RakNet::SocketDescriptor sd;
} NetworkMger;

class Game;
class Team;
class User;

// the global state for the network manager :p
// TOTALLY NOT EVIL I SWEAR
extern NetworkMger *nm;
extern Game *game;

namespace NetworkManager {
    void init();

    void startSever();

    void searchForGames();

    void connect();

    void isConnected();

    void endConnection();

    void destroy();

    void lock();
    void unlock();
    void isLocked();
}

#endif  //__NETWORK_MANAGER_H__