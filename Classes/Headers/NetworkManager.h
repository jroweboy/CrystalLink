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
#include "Kbhit.h"
#include "HTTPConnection2.h"

typedef struct NetworkManager {
    RakNet::RakPeerInterface *rakPeer;
    RakNet::TeamManager *teamManager;
} NetworkManager;

extern NetworkManager nm;

#endif  //__NETWORK_MANAGER_H__