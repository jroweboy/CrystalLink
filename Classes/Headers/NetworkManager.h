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
#include <atomic>

class User;

typedef struct NetworkMger {
    RakNet::RakPeerInterface *rakPeer;
    // Hold references to users on which tem
    RakNet::TeamManager *teamManager;
    // Replicates classes to be sent over the network
    RakNet::ReplicaManager3 *replicaManager3;
    RakNet::NetworkIDManager *networkIDManager;
    RakNet::TCPInterface *tcp;
    RakNet::ReadyEvent *readyEvent;
    RakNet::NatPunchthroughClient *natPunchthroughClient;
    RakNet::NatTypeDetectionClient *natTypeDetectionClient;
    RakNet::RPC4 *rpc4;
    // Holds a fully connected P2P network topology
    RakNet::FullyConnectedMesh2 *fullyConnectedMesh2;
    // Used to query the RakNet master server to join rooms
    RakNet::HTTPConnection2 *httpConnection2;
    // This computer's user object
    User *user;
    // change to atomic_flag if this is too slow
    std::atomic_bool isAlive;
} NetworkMger;



// the global state for the network manager :p
// TOTALLY NOT EVIL I SWEAR
extern RakNet::SocketDescriptor socketDescriptors[2];
extern NetworkMger *nm;

namespace NetworkManager {
    void init();

    void startNetwork();

    void searchForGames();

    bool connect();

    bool isConnected();

    void endNetwork();

    void destroy();

    void lock();
    void unlock();
    void isLocked();
}

#endif  //__NETWORK_MANAGER_H__