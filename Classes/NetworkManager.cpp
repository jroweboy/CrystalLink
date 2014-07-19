#include "NetworkManager.h"
#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include <iostream>
#include <thread>
#include "cocos2d.h"

using namespace RakNet;

class Game;
class Team;
class User;

SocketDescriptor socketDescriptors[2];
NetworkMger *nm;
Game *game;


#define FAST_RETURN if (!nm->isAlive) { return NULL; }
std::thread read_thread;
std::thread open_upnp_thread;


#define DEFAULT_SERVER_PORT "61111"
// Public test server
#define DEFAULT_SERVER_ADDRESS "natpunch.jenkinssoftware.com"
#define NAT_TYPE_DETECTION_SERVER 0
#define MASTER_SERVER_ADDRESS "masterserver2.raknet.com"
//#define MASTER_SERVER_ADDRESS "localhost"
#define MASTER_SERVER_PORT 80

void PostRoomToMaster();

class Game : public Replica3 {
public:
    enum Phase {
        CONNECTING_TO_SERVER,
        DETERMINE_NAT_TYPE,
        SEARCH_FOR_GAMES,
        NAT_PUNCH_TO_GAME_HOST,
        CONNECTING_TO_GAME_HOST,
        VERIFIED_JOIN,
        IN_LOBBY_WAITING_FOR_HOST,
        IN_LOBBY_WITH_HOST,
        IN_GAME,
        EXIT_SAMPLE,
    };

	// ---------------------------------------------------------------------------------
	// Serialized variables
	// ---------------------------------------------------------------------------------
	// Shows up in game listings
	RakString gameName;
	// If host locked the game, join queries are rejected
	bool lockGame;
	// Game is either in the lobby or in gameplay
	bool gameInLobby;
	// Which row of the master server our game was uploaded to
	// Returned from the POST request
	// This is serialized so if the host migrates, the new host takes over that row. Otherwise, a new row would be written for the same game.
	int masterServerRow;

	// ---------------------------------------------------------------------------------
	// Not serialized variables
	// ---------------------------------------------------------------------------------
	// Store what type of router I am behind
	NATTypeDetectionResult myNatType;
	Phase phase;
	// NAT punchthrough server runs RakNet project NatCompleteServer with NAT_TYPE_DETECTION_SERVER, and NAT_PUNCHTHROUGH_SERVER
	// TODO: This is required for having a dedicated server. We should have that as an option later
	RakNetGUID natPunchServerGuid;
	SystemAddress natPunchServerAddress;
	// char serverIPAddr[256];
	std::string serverIPAddr;
	// Just tracks what other objects have been created
	DataStructures::List<User*> users;
	DataStructures::List<Team*> teams;
	// TODO: Add password protection 
	// RaKString passwordHash;
	// Master server has to be refreshed periodically so it knows we didn't crash
	RakNet::Time whenToNextUpdateMasterServer;

	// The GET request returns a string. I use http://www.digip.org/jansson/ to parse the string, and store the results.
    rapidjson::Document masterServerQueryResult;
	// json_t *jsonArray;

	Game() {
        myNatType = NAT_TYPE_UNKNOWN; 
        masterServerRow = -1; 
        Reset(); 
        whenToNextUpdateMasterServer = 0;
        // masterServerQueryResult = 0;
    }
	virtual ~Game() {
        // if (masterServerQueryResult) {
        //     json_decref(masterServerQueryResult);
        // }
    }
	virtual void WriteAllocationID(Connection_RM3 *destinationConnection, BitStream *allocationIdBitstream) const {}
	virtual RM3ConstructionState QueryConstruction(Connection_RM3 *destinationConnection, ReplicaManager3 *replicaManager3) {
		if (nm->fullyConnectedMesh2->IsConnectedHost()) {
			return QueryConstruction_PeerToPeer(destinationConnection, R3P2PM_STATIC_OBJECT_CURRENTLY_AUTHORITATIVE);
        } else {
			return QueryConstruction_PeerToPeer(destinationConnection, R3P2PM_STATIC_OBJECT_NOT_CURRENTLY_AUTHORITATIVE);
        }
	}
	virtual bool QueryRemoteConstruction(Connection_RM3 *sourceConnection) {
        return true;
    }
	virtual void SerializeConstruction(BitStream *constructionBitstream, Connection_RM3 *destinationConnection) {}
	virtual bool DeserializeConstruction(BitStream *constructionBitstream, Connection_RM3 *sourceConnection) {
        return true;
    }
	virtual void SerializeConstructionExisting(BitStream *constructionBitstream, Connection_RM3 *destinationConnection)	{
		constructionBitstream->Write(gameName);
		constructionBitstream->Write(lockGame);
		constructionBitstream->Write(gameInLobby);
		constructionBitstream->Write(masterServerRow);
	}
	virtual void DeserializeConstructionExisting(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *sourceConnection) {
		constructionBitstream->Read(gameName);
		constructionBitstream->Read(lockGame);
		constructionBitstream->Read(gameInLobby);
		constructionBitstream->Read(masterServerRow);
		printf("Downloaded game. locked=%i. inLobby=%i\n", lockGame, gameInLobby);
	}
	virtual void SerializeDestruction(RakNet::BitStream *destructionBitstream, RakNet::Connection_RM3 *destinationConnection) {}
	virtual bool DeserializeDestruction(RakNet::BitStream *destructionBitstream, RakNet::Connection_RM3 *sourceConnection) {
        return true;
    }
	virtual RM3ActionOnPopConnection QueryActionOnPopConnection(Connection_RM3 *droppedConnection) const {
        return RM3AOPC_DO_NOTHING;
    }
	virtual void DeallocReplica(Connection_RM3 *sourceConnection) {
        delete this;
    }
	virtual RM3QuerySerializationResult QuerySerialization(Connection_RM3 *destinationConnection) {
		if (nm->fullyConnectedMesh2->IsConnectedHost()) {
			return QuerySerialization_PeerToPeer(destinationConnection, R3P2PM_STATIC_OBJECT_CURRENTLY_AUTHORITATIVE);
        } else {
			return QuerySerialization_PeerToPeer(destinationConnection, R3P2PM_STATIC_OBJECT_NOT_CURRENTLY_AUTHORITATIVE);
        }
	}
	virtual RM3SerializationResult Serialize(SerializeParameters *serializeParameters) {
		serializeParameters->outputBitstream[0].Write(lockGame);
		serializeParameters->outputBitstream[0].Write(gameInLobby);
		serializeParameters->outputBitstream[0].Write(masterServerRow);
		return RM3SR_BROADCAST_IDENTICALLY;
	}
	virtual void Deserialize(DeserializeParameters *deserializeParameters) {
		if (deserializeParameters->bitstreamWrittenTo[0]) {
			bool b;
			deserializeParameters->serializationBitstream[0].Read(b);
			if (b!=lockGame) {
				lockGame=b;
				if (lockGame) {
					printf("Game is no longer locked\n");
                } else {
					printf("Game is now locked\n");
                }
			}
			deserializeParameters->serializationBitstream[0].Read(b);
			if (b!=gameInLobby) {
				gameInLobby=b;
				if (gameInLobby) {
					nm->readyEvent->DeleteEvent(0);
					game->EnterPhase(Phase::IN_LOBBY_WITH_HOST);
					printf("Game is now in the lobby\n");
				} else {
					nm->readyEvent->ForceCompletion(0);
					game->EnterPhase(Phase::IN_GAME);
				}
			}
			deserializeParameters->serializationBitstream[0].Read(masterServerRow);
		}
	}
	void EnterPhase(Phase newPhase) {
		phase = newPhase;
		switch (newPhase)
		{
		case CONNECTING_TO_SERVER:
			{
				std::string port;
				printf("Enter address of server running the NATCompleteServer project.\nEnter for default: ");
				// Gets(game->serverIPAddr, 256);
				// if (game->serverIPAddr[0]==0) {
				game->serverIPAddr = DEFAULT_SERVER_ADDRESS;
                // }
				printf("Enter server port, or enter for default: ");
				// Gets(port, 256);
				// if (port[0] == 0) {
				port = DEFAULT_SERVER_PORT;
                // }
                ConnectionAttemptResult car = nm->rakPeer->Connect(serverIPAddr.c_str(), atoi(port.c_str()), 0, 0);
				if (car != RakNet::CONNECTION_ATTEMPT_STARTED) {
					printf("Failed connect call to %s. Code=%i\n", serverIPAddr.c_str(), car);
					phase = EXIT_SAMPLE;
				}
			}
			break;
		case DETERMINE_NAT_TYPE:
				printf("Determining NAT type...\n");
				nm->natTypeDetectionClient->DetectNATType(natPunchServerAddress);
			break;
		case SEARCH_FOR_GAMES:
				SearchForGames();
			break;
		case NAT_PUNCH_TO_GAME_HOST:
				printf("Attempting NAT punch to host of game session...\n");
			break;
		case IN_LOBBY_WITH_HOST:
				printf("(1) to join team 1.\n");
				printf("(2) to join team 2.\n");
				printf("(R)eady to start\n");
				printf("(U)nready to start\n");
			break;
		case IN_GAME:
				printf("Game started.\n(C)hat in-game\n");
			break;
		}
	}

	void Reset(void) {
        lockGame = false;
        gameInLobby = true;
    }

	void SearchForGames(void) {
		//printf("Downloading rooms...\n");

		RakString rsRequest = RakString::FormatForGET(
			MASTER_SERVER_ADDRESS "/testServer?__gameId=comprehensivePCGame");
		nm->httpConnection2->TransmitRequest(rsRequest, MASTER_SERVER_ADDRESS, MASTER_SERVER_PORT);
	}

	// Helper function to store and read the JSON from the GET request
	 //void SetMasterServerQueryResult(rapidjson::Document *root) {
	 //    if (masterServerQueryResult) {
	 //	       json_decref(masterServerQueryResult);
	 //    }
	 //    masterServerQueryResult = root;
	 //}

    //Document* GetMaster
	rapidjson::Document* GetMasterServerQueryResult(void) {

    }
	// {
	// 	if (masterServerQueryResult == 0) {
	// 		return 0;
	// 	}
	// 	void *iter = json_object_iter(masterServerQueryResult);
	// 	while (iter)
	// 	{
	// 		const char *firstKey = json_object_iter_key(iter);
	// 		if (stricmp(firstKey, "GET")==0)
	// 		{
	// 			return json_object_iter_value(iter);
	// 		}
	// 		iter = json_object_iter_next(masterServerQueryResult, iter);
	// 		RakAssert(iter != 0);
	// 	}
	// 	return 0;
	// }
};




// Team represents a list of players
// It uses TM_Team from the TeamManager plugin to do actual team functionality, store which players are on which teams, and networking
// It derives from Replica3 in order to replicate the teams across the network
class Team : public Replica3
{
public:
	Team() {
		game->teams.Push(this, _FILE_AND_LINE_); tmTeam.SetOwner(this);
	}
	virtual ~Team() {
		game->teams.RemoveAtIndex(game->teams.GetIndexOf(this));
	}
	virtual void WriteAllocationID(RakNet::Connection_RM3 *destinationConnection, RakNet::BitStream *allocationIdBitstream) const {allocationIdBitstream->Write("Team");}
	virtual RM3ConstructionState QueryConstruction(RakNet::Connection_RM3 *destinationConnection, ReplicaManager3 *replicaManager3) {
		// This implementation has the host create the Team instances initially
		// If the original host disconnects, the new host as determined by FullyConnectedMesh2 takes over replication duties
		if (nm->fullyConnectedMesh2->IsConnectedHost()) {
			return QueryConstruction_PeerToPeer(destinationConnection, R3P2PM_MULTI_OWNER_CURRENTLY_AUTHORITATIVE);
		} else {
			return QueryConstruction_PeerToPeer(destinationConnection, R3P2PM_MULTI_OWNER_NOT_CURRENTLY_AUTHORITATIVE);
		}
	}
	virtual bool QueryRemoteConstruction(RakNet::Connection_RM3 *sourceConnection) {
		return true;
	}
	virtual void SerializeConstruction(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *destinationConnection) {
		constructionBitstream->Write(teamName);
	}
	virtual bool DeserializeConstruction(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *sourceConnection) {
		constructionBitstream->Read(teamName);
		printf("Downloaded team. name=%s\n", teamName.C_String());
		// When ReplicaManager3 creates the team from a network command, the TeamManager class has to be informed of the new TM_Team instance
		nm->teamManager->GetWorldAtIndex(0)->ReferenceTeam(&tmTeam, GetNetworkID(), false);
		return true;
	}

	virtual void PostSerializeConstruction(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *destinationConnection) {
		tmTeam.SerializeConstruction(constructionBitstream);
	}
	virtual void PostDeserializeConstruction(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *sourceConnection) {
		tmTeam.DeserializeConstruction(nm->teamManager, constructionBitstream);	
	}

	virtual void SerializeDestruction(RakNet::BitStream *destructionBitstream, RakNet::Connection_RM3 *destinationConnection) {}
	virtual bool DeserializeDestruction(RakNet::BitStream *destructionBitstream, RakNet::Connection_RM3 *sourceConnection) {
		return true;
	}
	virtual RakNet::RM3ActionOnPopConnection QueryActionOnPopConnection(RakNet::Connection_RM3 *droppedConnection) const {
		// Do not destroy the object when the connection that created it disconnects.
		return RM3AOPC_DO_NOTHING;
	}
	virtual void DeallocReplica(RakNet::Connection_RM3 *sourceConnection) {
		delete this;
	}
	virtual RakNet::RM3QuerySerializationResult QuerySerialization(RakNet::Connection_RM3 *destinationConnection) {
		// Whoever is currently the host serializes the class
		if (nm->fullyConnectedMesh2->IsConnectedHost()) {
			return QuerySerialization_PeerToPeer(destinationConnection, R3P2PM_MULTI_OWNER_CURRENTLY_AUTHORITATIVE);
		} else {
			return QuerySerialization_PeerToPeer(destinationConnection, R3P2PM_MULTI_OWNER_NOT_CURRENTLY_AUTHORITATIVE);
		}
	}
	virtual RM3SerializationResult Serialize(RakNet::SerializeParameters *serializeParameters) {
		return RM3SR_BROADCAST_IDENTICALLY;
	}
	virtual void Deserialize(RakNet::DeserializeParameters *deserializeParameters) {}

	// Team data managed by the TeamManager plugin
	TM_Team tmTeam;

	// Example of team data not managed by TeamManager
	RakString teamName;
};



class User : public Replica3 {
public:
	User() {
        game->users.Push(this, _FILE_AND_LINE_); 
        tmTeamMember.SetOwner(this); natType=NAT_TYPE_UNKNOWN;
    }
	virtual ~User() {
		game->users.RemoveAtIndex(game->users.GetIndexOf(this));
	}
	virtual void WriteAllocationID(RakNet::Connection_RM3 *destinationConnection, RakNet::BitStream *allocationIdBitstream) const {
		allocationIdBitstream->Write("User");
	}
	virtual RM3ConstructionState QueryConstruction(RakNet::Connection_RM3 *destinationConnection, ReplicaManager3 *replicaManager3) {
		// Whoever created the user replicates it.
		return QueryConstruction_PeerToPeer(destinationConnection);
	}
	virtual bool QueryRemoteConstruction(RakNet::Connection_RM3 *sourceConnection) {
		return true;
	}
	virtual void SerializeConstruction(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *destinationConnection) {
		constructionBitstream->Write(userName);
		constructionBitstream->WriteCasted<unsigned char>(natType);
		constructionBitstream->Write(playerGuid);
		constructionBitstream->Write(playerAddress);
	}
	virtual bool DeserializeConstruction(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *sourceConnection) {
		// The TeamManager plugin has to be informed of TM_TeamMember instances created over the network
		nm->teamManager->GetWorldAtIndex(0)->ReferenceTeamMember(&tmTeamMember, GetNetworkID());
		constructionBitstream->Read(userName);
		constructionBitstream->ReadCasted<unsigned char>(natType);
		constructionBitstream->Read(playerGuid);
		constructionBitstream->Read(playerAddress);
		return true;
	}
	virtual void PostSerializeConstruction(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *destinationConnection) {
		// TeamManager requires that TM_Team was created before TM_TeamMember that uses it.
		// PostSerializeConstruction and PostDeserializeConstruction ensure that all objects have been created before serialization
		tmTeamMember.SerializeConstruction(constructionBitstream);
	}
	virtual void PostDeserializeConstruction(RakNet::BitStream *constructionBitstream, RakNet::Connection_RM3 *sourceConnection) {
		tmTeamMember.DeserializeConstruction(nm->teamManager, constructionBitstream);	
		printf("Downloaded user. name=%s", userName.C_String());
		if (tmTeamMember.GetCurrentTeam() == 0) {
			printf(" not on a team\n");
        } else {
			printf(" on team %s\n", ((Team*)(tmTeamMember.GetCurrentTeam()->GetOwner()))->teamName.C_String());
        }

		// Update the user count on the master server as new users join
		if (nm->fullyConnectedMesh2->IsConnectedHost()) {
			PostRoomToMaster();
        }
	}

	virtual void SerializeDestruction(RakNet::BitStream *destructionBitstream, RakNet::Connection_RM3 *destinationConnection) {}
	virtual bool DeserializeDestruction(RakNet::BitStream *destructionBitstream, RakNet::Connection_RM3 *sourceConnection) {return true;}
	virtual RakNet::RM3ActionOnPopConnection QueryActionOnPopConnection(RakNet::Connection_RM3 *droppedConnection) const {return QueryActionOnPopConnection_PeerToPeer(droppedConnection);}
	virtual void DeallocReplica(RakNet::Connection_RM3 *sourceConnection) {delete this;}
	virtual RakNet::RM3QuerySerializationResult QuerySerialization(RakNet::Connection_RM3 *destinationConnection) {return QuerySerialization_PeerToPeer(destinationConnection);}
	virtual RM3SerializationResult Serialize(RakNet::SerializeParameters *serializeParameters) {return RM3SR_BROADCAST_IDENTICALLY;}
	virtual void Deserialize(RakNet::DeserializeParameters *deserializeParameters) {}

	// Team data managed by the TeamManager plugin
	TM_TeamMember tmTeamMember;
	RakString userName;
	NATTypeDetectionResult natType;
	RakNetGUID playerGuid;
	SystemAddress playerAddress;
};

// Required by ReplicaManager3. Acts as a class factory for Replica3 derived instances
class SimpleConnectionRM3 : public Connection_RM3 {
public:
    SimpleConnectionRM3(const SystemAddress &_systemAddress, RakNetGUID _guid) : Connection_RM3(_systemAddress, _guid) {}
    virtual ~SimpleConnectionRM3() {}
    virtual Replica3 *AllocReplica(RakNet::BitStream *allocationIdBitstream, ReplicaManager3 *replicaManager3) {
        RakString objectType;
        // Types are written by WriteAllocationID()
        allocationIdBitstream->Read(objectType);
        if (objectType=="User") {
            return new User;
        }
        if (objectType=="Team") {
            return new Team;
        }
        RakAssert("Unknown type in AllocReplica" && 0);
        return 0;
    }
};

// Required by ReplicaManager3. Acts as a class factory for Connection_RM3 derived instances
class SimpleRM3 : public ReplicaManager3
{
public:
	SimpleRM3() {}
	virtual ~SimpleRM3() {}
	virtual Connection_RM3* AllocConnection(const SystemAddress &systemAddress, RakNetGUID rakNetGUID) const {
        return new SimpleConnectionRM3(systemAddress,rakNetGUID);
    }
	virtual void DeallocConnection(Connection_RM3 *connection) const {
        delete connection;
    }
};


// ----------------------------------------------------------------------
// Helper functions
// ----------------------------------------------------------------------

// Demonstrates how to use the RPC4 plugin
void InGameChat(RakNet::BitStream *userData, Packet *packet)
{
	RakString rs;
	userData->Read(rs);
	printf("%s\n", rs.C_String());
}
// Register the function where it is defined, which is easier than maintaining a bunch of RegisterSlot() calls in main()
RPC4GlobalRegistration __InGameChat("InGameChat", InGameChat, 0);

// Write roomName and a list of NATTypeDetectionResult to a bitStream
void SerializeToJSON(RakString &outputString, RakString &roomName, DataStructures::List<NATTypeDetectionResult> &natTypes)
{
	outputString.Set("'roomName': '%s', 'guid': '%s', 'natTypes' : [ ", roomName.C_String(), nm->rakPeer->GetMyGUID().ToString());
	for (unsigned short i=0; i < natTypes.Size(); i++)
	{
		if (i != 0) {
			outputString += ", ";
        }
		RakString appendStr("{'type': %i}", natTypes[i]);
		outputString += appendStr;
	}
	outputString += " ] ";
}

// A system has connected and is ready to participate in the game
// Register this system with the plugins that need to know about new participants
// This operation happens after FullyConnectedMesh2 has told us about who the host is.
void RegisterGameParticipant(RakNetGUID guid)
{
	Connection_RM3 *connection = nm->replicaManager3->AllocConnection(nm->rakPeer->GetSystemAddressFromGuid(guid), guid);
	if (nm->replicaManager3->PushConnection(connection)==false) {
		nm->replicaManager3->DeallocConnection(connection);
    }
	nm->teamManager->GetWorldAtIndex(0)->AddParticipant(guid);
	nm->readyEvent->AddToWaitList(0, guid);
}

// Upload details about the current game state to the cloud
// This is the responsibility of the system that initially created that room.
// If that system disconnects, the new host, as determined by FullyConnectedMesh2 will reupload the room
void PostRoomToMaster(void)
{
	BitStream bsOut;
	RakString jsonSerializedRoom;
	DataStructures::List<NATTypeDetectionResult> natTypes;
	for (unsigned int i=0; i < game->users.Size(); i++)
		natTypes.Push(game->users[i]->natType, _FILE_AND_LINE_);
	SerializeToJSON(jsonSerializedRoom, game->gameName, natTypes);

	RakString rowStr;
	if (game->masterServerRow!=-1)
		rowStr.Set("\"__rowId\": %i,", game->masterServerRow);

	// See http://masterserver2.raknet.com/
	RakString rsRequest = RakString::FormatForPOST(
		(const char*) MASTER_SERVER_ADDRESS "/testServer",
		"text/plain; charset=UTF-8",
		RakString("{'__gameId': 'comprehensivePCGame', '__clientReqId': '0', %s '__timeoutSec': '30', %s }", rowStr.C_String(), jsonSerializedRoom.C_String()));

	// Refresh the room again slightly less than every 30 seconds
	game->whenToNextUpdateMasterServer = RakNet::GetTime() + 30000 - 1000;

	nm->httpConnection2->TransmitRequest(rsRequest, MASTER_SERVER_ADDRESS, MASTER_SERVER_PORT);

	printf("Posted game session. In room.\n");
}
void ReleaseRoomFromCloud(void)
{
	RakString rsRequest = RakString::FormatForDELETE(
		RakString(MASTER_SERVER_ADDRESS "/testServer?__gameId=comprehensivePCGame&__rowId=%i", game->masterServerRow));
	nm->httpConnection2->TransmitRequest(rsRequest, MASTER_SERVER_ADDRESS, MASTER_SERVER_PORT);
	game->masterServerRow=-1;
}

void CreateRoom(void)
{
	size_t arraySize;
	//if (game->GetMasterServerQueryResult())
	//	arraySize = json_array_size(game->GetMasterServerQueryResult());
	//else
		arraySize = 0;
	
	if (arraySize > 0)
	{
		printf("Enter room name: ");
		char rn[128];
		Gets(rn, 128);
		if (rn[0]==0)
			strcpy(rn, "Unnamed");
		game->gameName = rn;
	}
	else
	{
		game->gameName = "Default room name";
	}
	
	// Upload the room to the server
	PostRoomToMaster();

	// Room owner creates two teams and registers them for replication
	Team *team1 = new Team;
	team1->SetNetworkIDManager(nm->networkIDManager);
	team1->teamName = "Team1";
	nm->teamManager->GetWorldAtIndex(0)->ReferenceTeam(&team1->tmTeam, team1->GetNetworkID(), false);
	Team *team2 = new Team;
	team2->SetNetworkIDManager(nm->networkIDManager);
	team2->teamName = "Team2";
	nm->teamManager->GetWorldAtIndex(0)->ReferenceTeam(&team2->tmTeam, team2->GetNetworkID(), false);

	game->EnterPhase(Game::IN_LOBBY_WAITING_FOR_HOST);

	// So that time spent in single player does not count towards which system has been running the longest in multiplayer
	nm->fullyConnectedMesh2->ResetHostCalculation();

	printf("(E)xit session\n");
}

struct UPNPOpenWorkerArgs
{
	char buff[256];
	unsigned short portToOpen;
	unsigned int timeout;
	void *userData;
	void (*resultCallback)(bool success, unsigned short portToOpen, void *userData);
	void (*progressCallback)(const char *progressMsg, void *userData);
};
RAK_THREAD_DECLARATION(UPNPOpenWorker)
{
	UPNPOpenWorkerArgs *args = ( UPNPOpenWorkerArgs * ) arguments;
	bool success=false;

	// Behind a NAT. Try to open with UPNP to avoid doing NAT punchthrough
	struct UPNPDev * devlist = 0;
	RakNet::Time t1 = GetTime();
    FAST_RETURN;
	devlist = upnpDiscover(args->timeout, 0, 0, 0, 0, 0);
	RakNet::Time t2 = GetTime();
	if (devlist)
	{
        FAST_RETURN;
		if (args->progressCallback)
			args->progressCallback("List of UPNP devices found on the network :\n", args->userData);
		struct UPNPDev * device;
		for(device = devlist; device; device = device->pNext)
		{
            FAST_RETURN;
			sprintf(args->buff, " desc: %s\n st: %s\n\n", device->descURL, device->st);
			if (args->progressCallback)
				args->progressCallback(args->buff, args->userData);
		}

		char lanaddr[64];	/* my ip address on the LAN */
		struct UPNPUrls urls;
		struct IGDdatas data;
        FAST_RETURN;
		if (UPNP_GetValidIGD(devlist, &urls, &data, lanaddr, sizeof(lanaddr))==1)
		{
			char iport[32];
			Itoa(args->portToOpen, iport,10);
			char eport[32];
			strcpy(eport, iport);

            FAST_RETURN;
			int r = UPNP_AddPortMapping(urls.controlURL, data.first.servicetype,
				eport, iport, lanaddr, 0, "UDP", 0, "0");

			if (r != UPNPCOMMAND_SUCCESS) {
				printf("AddPortMapping(%s, %s, %s) failed with code %d (%s)\n",
				eport, iport, lanaddr, r, strupnperror(r));
			}

			char intPort[6];
			char intClient[16];

			// Version miniupnpc-1.6.20120410
			char desc[128];
			char enabled[128];
			char leaseDuration[128];
            FAST_RETURN;
			r = UPNP_GetSpecificPortMappingEntry(urls.controlURL,
				data.first.servicetype,
				eport, "UDP", NULL,
				intClient, intPort,
				desc, enabled, leaseDuration);

			if(r != UPNPCOMMAND_SUCCESS) {
				sprintf(args->buff, "GetSpecificPortMappingEntry() failed with code %d (%s)\n",
					r, strupnperror(r));
				if (args->progressCallback)
					args->progressCallback(args->buff, args->userData);
			}
			else {
				if (args->progressCallback)
					args->progressCallback("UPNP success.\n", args->userData);
				// game->myNatType=NAT_TYPE_SUPPORTS_UPNP;

				success=true;
			}
		}
	}

	if (args->resultCallback) {
		args->resultCallback(success, args->portToOpen, args->userData);
	}
	RakNet::OP_DELETE(args, _FILE_AND_LINE_);
	return NULL;
}

void UPNPOpenAsynch(unsigned short portToOpen,
					unsigned int timeout,
					void (*progressCallback)(const char *progressMsg, void *userData),
					void (*resultCallback)(bool success, unsigned short portToOpen, void *userData),
					void *userData
					)
{
	UPNPOpenWorkerArgs *args = RakNet::OP_NEW<UPNPOpenWorkerArgs>(_FILE_AND_LINE_);
	args->portToOpen = portToOpen;
	args->timeout = timeout;
	args->userData = userData;
	args->progressCallback = progressCallback;
	args->resultCallback = resultCallback;
	//RakThread::Create(UPNPOpenWorker, args);
    open_upnp_thread = std::thread(UPNPOpenWorker, args);
}

void UPNPProgressCallback(const char *progressMsg, void *userData)
{
	printf(progressMsg);
}
void UPNPResultCallback(bool success, unsigned short portToOpen, void *userData)
{
	if (success) {
		game->myNatType=NAT_TYPE_SUPPORTS_UPNP;
    }
	game->EnterPhase(Game::SEARCH_FOR_GAMES);
}

void OpenUPNP(void)
{
	printf("Discovering UPNP...\n");

	DataStructures::List<RakNetSocket2* > sockets;
	nm->rakPeer->GetSockets(sockets);
    // TODO: Change this to be a normal std thread and have it join on shutdown
    // Also add in more spots for a KILL THREAD statement
	UPNPOpenAsynch(sockets[0]->GetBoundAddress().GetPort(), 2000, UPNPProgressCallback, UPNPResultCallback, 0);
}



// initialize the network manager

void NetworkManager::init() {
    nm = (NetworkMger*) malloc(sizeof(NetworkMger));
    nm->rakPeer = RakPeerInterface::GetInstance();
	nm->teamManager = TeamManager::GetInstance();
	nm->fullyConnectedMesh2 = FullyConnectedMesh2::GetInstance();
	nm->networkIDManager = NetworkIDManager::GetInstance();
	nm->tcp = TCPInterface::GetInstance();
	nm->natPunchthroughClient = NatPunchthroughClient::GetInstance();
	nm->natTypeDetectionClient = NatTypeDetectionClient::GetInstance();
	nm->rpc4 = RPC4::GetInstance();
	nm->readyEvent = ReadyEvent::GetInstance();
	nm->replicaManager3 = new SimpleRM3();
	nm->httpConnection2 = HTTPConnection2::GetInstance();

	// ---------------------------------------------------------------------------------------------------------------------
	// Attach plugins
	// ---------------------------------------------------------------------------------------------------------------------
	nm->rakPeer->AttachPlugin(nm->fullyConnectedMesh2);
	nm->rakPeer->AttachPlugin(nm->teamManager);
	nm->rakPeer->AttachPlugin(nm->natPunchthroughClient);
	nm->rakPeer->AttachPlugin(nm->natTypeDetectionClient);
	nm->rakPeer->AttachPlugin(nm->rpc4);
	nm->rakPeer->AttachPlugin(nm->readyEvent);
	nm->rakPeer->AttachPlugin(nm->replicaManager3);
	/// TCPInterface supports plugins too
	nm->tcp->AttachPlugin(nm->httpConnection2);


	// ---------------------------------------------------------------------------------------------------------------------
	// Setup plugins: Disable automatically adding new connections. Allocate initial objects and register for replication
	// ---------------------------------------------------------------------------------------------------------------------
	// Allocate a world instance to be used for team operations
	nm->teamManager->AddWorld(0);
	// Do not automatically count new connections
	nm->teamManager->SetAutoManageConnections(false);

	// New connections do not count until after login.
	nm->fullyConnectedMesh2->SetAutoparticipateConnections(false);	

	// Tell ReplicaManager3 which networkIDManager to use for object lookup, used for automatic serialization
	nm->replicaManager3->SetNetworkIDManager(nm->networkIDManager);
	// Do not automatically count new connections, but do drop lost connections automatically
	nm->replicaManager3->SetAutoManageConnections(false, true);

	// Reference static game objects that always exist
	game = new Game();
	game->SetNetworkIDManager(nm->networkIDManager);
	game->SetNetworkID(0);
	nm->replicaManager3->Reference(game);

	// Setup my own user
	nm->user = new User();
	nm->user->SetNetworkIDManager(nm->networkIDManager);
	nm->user->userName = nm->rakPeer->GetMyGUID().ToString();
	// Inform TeamManager of my user's team member info
	nm->teamManager->GetWorldAtIndex(0)->ReferenceTeamMember(&nm->user->tmTeamMember, nm->user->GetNetworkID());

	// ------------------------------------------------------------------------------
	// Startup RakNet on first available port
	// ------------------------------------------------------------------------------
    //nm->sd = (RakNet::SocketDescriptor*) malloc(sizeof(RakNet::SocketDescriptor) * 2);
    //nm->sd[0].port=atoi(DEFAULT_SERVER_PORT);
    //nm->sd[0].socketFamily=AF_INET; // Test out IPV4
    //nm->sd[1].port=atoi(DEFAULT_SERVER_PORT);
    //nm->sd[1].socketFamily=AF_INET6; // Test out IPV6
    socketDescriptors[0].port=atoi(DEFAULT_SERVER_PORT);
    socketDescriptors[0].socketFamily=AF_INET; // Test out IPV4
    socketDescriptors[1].port=atoi(DEFAULT_SERVER_PORT);
    socketDescriptors[1].socketFamily=AF_INET6; // Test out IPV6
    auto startresult = nm->rakPeer->Startup(4, socketDescriptors, 2 );
    nm->rakPeer->SetMaximumIncomingConnections(4);
    if (startresult != RakNet::RAKNET_STARTED)
    {
        printf("Failed to start dual IPV4 and IPV6 ports. Trying IPV4 only.\n");
        // Try again, but leave out IPV6
        startresult = nm->rakPeer->Startup(4, socketDescriptors, 1 );
        if (startresult != RakNet::RAKNET_STARTED)
        {
            //std::cout << "Server failed to start. Terminating." << std::endl;
            //exit(1);
            // TODO better error handling of bad network states
	        RakAssert(startresult == RAKNET_STARTED);
        }
    }

	//StartupResult sr = nm->rakPeer->Startup(8, &nm->sd, 1);
	// ummm TODO, lets NOT have random asserts in the middle of the code :p
	RakAssert(startresult == RAKNET_STARTED);
	nm->rakPeer->SetMaximumIncomingConnections(8);
	nm->rakPeer->SetTimeoutTime(30000, UNASSIGNED_SYSTEM_ADDRESS);
}


//RAK_THREAD_DECLARATION(readPackets, NULL) {
unsigned int readPackets() {
	// Read packets loop
	char ch;
	Packet *packet;
    // TODO rethink this network code block.
	while (nm->isAlive && game->phase != Game::EXIT_SAMPLE) {
        for (packet = nm->rakPeer->Receive(); nm->isAlive && packet; nm->rakPeer->DeallocatePacket(packet), packet = nm->rakPeer->Receive()) {
            FAST_RETURN;
			switch (packet->data[0]) {
			case ID_NEW_INCOMING_CONNECTION: {
					//printf("ID_NEW_INCOMING_CONNECTION from %s. guid=%s.\n", 
					//	packet->systemAddress.ToString(true), packet->guid.ToString());
                    FAST_RETURN;
				}
				break;
			case ID_CONNECTION_REQUEST_ACCEPTED: {
					//printf("ID_CONNECTION_REQUEST_ACCEPTED from %s,guid=%s\n", 
					//	packet->systemAddress.ToString(true), packet->guid.ToString());
                    FAST_RETURN;
					if (game->phase == Game::CONNECTING_TO_SERVER) {
						game->natPunchServerAddress=packet->systemAddress;
						game->natPunchServerGuid=packet->guid;

						// PC self-hosted servers only: 
						// Use the NAT punch server to determine NAT type. 
						// Attempt to open router if needed.
						if (NAT_TYPE_DETECTION_SERVER) {
							game->EnterPhase(Game::DETERMINE_NAT_TYPE);
						} else {
							OpenUPNP();
						}
					}
					else if (game->phase == Game::CONNECTING_TO_GAME_HOST) {
						printf("Asking host to join session...\n");

						// So time in single player does not count towards which system has been running multiplayer the longest
						nm->fullyConnectedMesh2->ResetHostCalculation();

						// Custom message to ask to join the game
						// We first connect to the game host, and the game host is responsible for calling StartVerifiedJoin() for us to join the session
						BitStream bsOut;
						bsOut.Write((MessageID)ID_USER_PACKET_ENUM);
						nm->rakPeer->Send(&bsOut,HIGH_PRIORITY,RELIABLE_ORDERED,0,packet->guid,false);
					}
				}
				break;
			case ID_CONNECTION_LOST:
			case ID_DISCONNECTION_NOTIFICATION:
                FAST_RETURN;
				if (game->phase == Game::DETERMINE_NAT_TYPE) {
					printf("Lost connection during NAT type detection. Reason %s. Retrying...\n", 
						PacketLogger::BaseIDTOString(packet->data[0]));

					game->EnterPhase(Game::CONNECTING_TO_SERVER);
				} else if (game->phase == Game::NAT_PUNCH_TO_GAME_HOST) {
					printf("Lost connection during NAT punch to game host. Reason %s.\n", 
						PacketLogger::BaseIDTOString(packet->data[0]));

					game->EnterPhase(Game::SEARCH_FOR_GAMES);
				} else {
					if (packet->guid == game->natPunchServerGuid) {
						printf("Server connection lost. Reason %s.\nGame session is no longer searchable.\n", 
							PacketLogger::BaseIDTOString(packet->data[0]));
					}
					else
					{
						printf("Peer connection lost. Reason %s.\n", PacketLogger::BaseIDTOString(packet->data[0]));
					}
				}
				break;

			case ID_ALREADY_CONNECTED:
				printf("ID_ALREADY_CONNECTED with guid %" PRINTF_64_BIT_MODIFIER "u\n", packet->guid);
				break;

			case ID_INVALID_PASSWORD:
			case ID_NO_FREE_INCOMING_CONNECTIONS:
			case ID_CONNECTION_ATTEMPT_FAILED:
			case ID_CONNECTION_BANNED:
			case ID_IP_RECENTLY_CONNECTED:
			case ID_INCOMPATIBLE_PROTOCOL_VERSION:
				// Note: Failing to connect to another system does not automatically mean we cannot join a session, since that system may be disconnecting from the host simultaneously
				// FullyConnectedMesh2::StartVerifiedJoin() internally handles success or failure and notifies the client through ID_FCM2_VERIFIED_JOIN_FAILED if needed.
				printf("Failed to connect to %s. Reason %s\n", 
					packet->systemAddress.ToString(true), 
					PacketLogger::BaseIDTOString(packet->data[0]));

				if (game->phase==Game::CONNECTING_TO_SERVER) {
					game->EnterPhase(Game::EXIT_SAMPLE);
				}
				break;
				
			case ID_FCM2_NEW_HOST: {
					RakNet::BitStream bs(packet->data,packet->length,false);
					bs.IgnoreBytes(1);
					RakNetGUID oldHost;
					bs.Read(oldHost);

					if (packet->guid == nm->rakPeer->GetMyGUID()) {
						if (oldHost != UNASSIGNED_RAKNET_GUID) {
							if (game->phase==Game::IN_LOBBY_WAITING_FOR_HOST) {
								game->phase=Game::IN_LOBBY_WITH_HOST;
							}
							PostRoomToMaster();
							printf("ID_FCM2_NEW_HOST: Taking over as host from the old host.\nNew options:\n");
						} else {
							// Room not hosted if we become host the first time since this was done in CreateRoom() already
							printf("ID_FCM2_NEW_HOST: We have become host for the first time. New options:\n");
						}

						printf("(L)ock and unlock game\n");
					} else {
						if (oldHost != UNASSIGNED_RAKNET_GUID) {
							printf("ID_FCM2_NEW_HOST: A new system %s has become host, GUID=%s\n", 
								packet->systemAddress.ToString(true), packet->guid.ToString());
						} else {
							printf("ID_FCM2_NEW_HOST: System %s is host, GUID=%s\n", 
								packet->systemAddress.ToString(true), packet->guid.ToString());
						}
					}

					if (oldHost == UNASSIGNED_RAKNET_GUID) {
						// First time calculated host. Add existing connections to ReplicaManager3
						DataStructures::List<RakNetGUID> participantList;
						nm->fullyConnectedMesh2->GetParticipantList(participantList);
						for (unsigned int i=0; i < participantList.Size(); i++) {
							RegisterGameParticipant(participantList[i]);
						}

						// Reference previously created replicated objects, which cannot be serialized until host is known the first time
						if (packet->guid == nm->rakPeer->GetMyGUID()) {
							// As host, reference the teams we created
							for (unsigned int i=0; i < game->teams.Size(); i++) {
								nm->replicaManager3->Reference(game->teams[i]);
							}
						}

						// Reference the user we created (host or not)
						for (unsigned int i=0; i < game->users.Size(); i++) {
							nm->replicaManager3->Reference(game->users[i]);
						}
					}
				}
				break;
			case ID_TEAM_BALANCER_TEAM_ASSIGNED: {
					printf("ID_TEAM_BALANCER_TEAM_ASSIGNED for ");
					TM_World *world;
					TM_TeamMember *teamMember;
					nm->teamManager->DecodeTeamAssigned(packet, &world, &teamMember);
					printf("worldId=%i teamMember=%s", 
						world->GetWorldId(), 
						((User*)teamMember->GetOwner())->userName.C_String());
					if (teamMember->GetCurrentTeam() == 0) {
						printf(" not on team\n");
					} else {
						printf(" on team %s\n", ((Team*)(teamMember->GetCurrentTeam()->GetOwner()))->teamName.C_String());
					}
				}
				break;
			case ID_TEAM_BALANCER_REQUESTED_TEAM_FULL: {
					printf("ID_TEAM_BALANCER_REQUESTED_TEAM_FULL\n");
				}
				break;
			case ID_TEAM_BALANCER_REQUESTED_TEAM_LOCKED: {
					printf("ID_TEAM_BALANCER_REQUESTED_TEAM_LOCKED\n");
				}
				break;
			case ID_TEAM_BALANCER_TEAM_REQUESTED_CANCELLED: {
					printf("ID_TEAM_BALANCER_TEAM_REQUESTED_CANCELLED\n");
				}
				break;
			case ID_NAT_TARGET_NOT_CONNECTED:
			case ID_NAT_TARGET_UNRESPONSIVE:
			case ID_NAT_CONNECTION_TO_TARGET_LOST:
			case ID_NAT_PUNCHTHROUGH_FAILED: {
					// As with connection failed, this does not automatically mean we cannot join the session
					// We only fail on ID_FCM2_VERIFIED_JOIN_FAILED
					printf("NAT punch to %s failed. Reason %s\n", 
						packet->guid.ToString(), PacketLogger::BaseIDTOString(packet->data[0]));

					if (game->phase == Game::NAT_PUNCH_TO_GAME_HOST) {
						game->EnterPhase(Game::SEARCH_FOR_GAMES);
					}
				}

			case ID_NAT_ALREADY_IN_PROGRESS:
				// Can ignore this
				break;

			case ID_NAT_PUNCHTHROUGH_SUCCEEDED:
				{
                    FAST_RETURN;
					if (game->phase == Game::NAT_PUNCH_TO_GAME_HOST || game->phase == Game::VERIFIED_JOIN) {
						// Connect to the session host
						ConnectionAttemptResult car = nm->rakPeer->Connect(
									packet->systemAddress.ToString(false), 
									packet->systemAddress.GetPort(), 0, 0);
                        FAST_RETURN;
						if (car != RakNet::CONNECTION_ATTEMPT_STARTED) {
							printf("Failed connect call to %s. Code=%i\n", 
								packet->systemAddress.ToString(false), car);
							game->EnterPhase(Game::SEARCH_FOR_GAMES);
						} else {
							if (game->phase == Game::NAT_PUNCH_TO_GAME_HOST) {
								printf("NAT punch completed. Connecting to %s (game host)...\n", 
									packet->systemAddress.ToString(true));
								game->EnterPhase(Game::CONNECTING_TO_GAME_HOST);
							} else {
								printf("NAT punch completed. Connecting to %s (game client)...\n", 
									packet->systemAddress.ToString(true));
							}
						}
					}
				}
				break;
			case ID_NAT_TYPE_DETECTION_RESULT:
				{
					game->myNatType = (RakNet::NATTypeDetectionResult) packet->data[1];
					printf("NAT Type is %s (%s)\n", 
							NATTypeDetectionResultToString(game->myNatType), 
							NATTypeDetectionResultToStringFriendly(game->myNatType));

					if (game->myNatType != RakNet::NAT_TYPE_NONE) {
						OpenUPNP();
					}

					if (game->myNatType == RakNet::NAT_TYPE_PORT_RESTRICTED || game->myNatType == RakNet::NAT_TYPE_SYMMETRIC) {
						printf("Note: Your router must support UPNP or have the user manually forward ports.\n");
						printf("Otherwise NATPunchthrough may not always succeed.\n");
					}
                    FAST_RETURN;
					game->EnterPhase(Game::SEARCH_FOR_GAMES);
				}
				break;
				
			case ID_READY_EVENT_ALL_SET:
				printf("Got ID_READY_EVENT_ALL_SET from %s\n", 
					packet->systemAddress.ToString(true));
				printf("All users ready.\n");
				if (nm->fullyConnectedMesh2->IsConnectedHost()) {
					printf("New options:\n(B)egin gameplay\n");
				}
				break;

			case ID_READY_EVENT_SET:
				printf("Got ID_READY_EVENT_SET from %s\n", packet->systemAddress.ToString(true));
				break;

			case ID_READY_EVENT_UNSET:
				printf("Got ID_READY_EVENT_UNSET from %s\n", packet->systemAddress.ToString(true));
				break;

			// ID_USER_PACKET_ENUM is used by this sample as a custom message to ask to join a game
			case ID_USER_PACKET_ENUM:
                FAST_RETURN;
				if (game->phase > Game::SEARCH_FOR_GAMES) {
					printf("Got request from client to join session.\nExecuting StartVerifiedJoin()\n");
					nm->fullyConnectedMesh2->StartVerifiedJoin(packet->guid);
				} else {
					BitStream bsOut;
					bsOut.Write((MessageID)(ID_USER_PACKET_ENUM+1));
					nm->rakPeer->Send(&bsOut,HIGH_PRIORITY,RELIABLE_ORDERED,0,packet->guid,false);
				}
				break;
			// ID_USER_PACKET_ENUM+1 is used by this sample as a custom message to reject a join game request
			// Requests may also be later rejected through FullyConnectedMesh2::RespondOnVerifiedJoinCapable() to send ID_FCM2_VERIFIED_JOIN_REJECTED
			case (ID_USER_PACKET_ENUM+1):
				printf("Join request denied\n");
				game->EnterPhase(Game::SEARCH_FOR_GAMES);
				break;
			case ID_FCM2_VERIFIED_JOIN_START:
				{
					game->EnterPhase(Game::VERIFIED_JOIN);
                    FAST_RETURN;
					// This message means the session host sent us a list of systems in the session
					// Once we connect to, or fail to connect to, each of these systems we will get ID_FCM2_VERIFIED_JOIN_FAILED, ID_FCM2_VERIFIED_JOIN_ACCEPTED, or ID_FCM2_VERIFIED_JOIN_REJECTED
					printf("Host sent us system list. Doing NAT punch to each system...\n");
					DataStructures::List<SystemAddress> addresses;
					DataStructures::List<RakNetGUID> guids;
					DataStructures::List<RakNet::BitStream*> btStream;
					nm->fullyConnectedMesh2->GetVerifiedJoinRequiredProcessingList(packet->guid, addresses, guids, btStream);
					for (unsigned int i=0; i < guids.Size(); i++)
						nm->natPunchthroughClient->OpenNAT(guids[i], game->natPunchServerAddress);
				}
				break;

			case ID_FCM2_VERIFIED_JOIN_CAPABLE:
				printf("Client is capable of joining FullyConnectedMesh2.\n");
				if (game->lockGame) {
					RakNet::BitStream bsOut;
					bsOut.Write("Game is locked");
					nm->fullyConnectedMesh2->RespondOnVerifiedJoinCapable(packet, false, &bsOut);
				} else {
					nm->fullyConnectedMesh2->RespondOnVerifiedJoinCapable(packet, true, 0);
				}
				break;

			case ID_FCM2_VERIFIED_JOIN_ACCEPTED:
				{
					DataStructures::List<RakNetGUID> systemsAccepted;
					bool thisSystemAccepted;
					nm->fullyConnectedMesh2->GetVerifiedJoinAcceptedAdditionalData(packet, &thisSystemAccepted, systemsAccepted, 0);
					if (thisSystemAccepted) {
						printf("Game join request accepted\n");
					} else {
						printf("System %s joined the mesh\n", systemsAccepted[0].ToString());
					}

					// Add the new participant to the game if we already know who the host is. Otherwise do this
					// once ID_FCM2_NEW_HOST arrives
					if (nm->fullyConnectedMesh2->GetConnectedHost() != UNASSIGNED_RAKNET_GUID) {
						// FullyConnectedMesh2 already called AddParticipant() for each accepted system
						// Still need to add those systems to the other plugins though
						for (unsigned int i=0; i < systemsAccepted.Size(); i++) {
							RegisterGameParticipant(systemsAccepted[i]);
						}

						if (thisSystemAccepted) {
							game->EnterPhase(Game::IN_LOBBY_WITH_HOST);
						}
					} else {
						if (thisSystemAccepted) {
							game->EnterPhase(Game::IN_LOBBY_WAITING_FOR_HOST);
						}
					}

					printf("(E)xit room\n");
				}
				break;

			case ID_FCM2_VERIFIED_JOIN_REJECTED:
				{
					BitStream additionalData;
					nm->fullyConnectedMesh2->GetVerifiedJoinRejectedAdditionalData(packet, &additionalData);
                    FAST_RETURN;
					RakString reason;
					additionalData.Read(reason);
					printf("Join rejected. Reason=%s\n", reason.C_String());
					nm->rakPeer->CloseConnection(packet->guid, true);
					game->EnterPhase(Game::SEARCH_FOR_GAMES);
					break;
				}

			case ID_REPLICA_MANAGER_DOWNLOAD_COMPLETE:
				{
					if (nm->replicaManager3->GetAllConnectionDownloadsCompleted()) {
						printf("Completed all remote downloads\n");

						if (game->gameInLobby) {
							game->EnterPhase(Game::IN_LOBBY_WITH_HOST);
						} else {
							game->EnterPhase(Game::IN_GAME);
						}
					}

					break;
				}
			}
		}
        FAST_RETURN;
		// The following code is TCP operations for talking to the master server, and parsing the reply
		SystemAddress sa;
		// This is kind of crappy, but for TCP plugins, always do HasCompletedConnectionAttempt, then Receive(), then HasFailedConnectionAttempt(),HasLostConnection()
		sa = nm->tcp->HasCompletedConnectionAttempt();
		for (packet = nm->tcp->Receive(); packet; nm->tcp->DeallocatePacket(packet), packet = nm->tcp->Receive())
			;
		sa = nm->tcp->HasFailedConnectionAttempt();
		sa = nm->tcp->HasLostConnection();

		RakString stringTransmitted;
		RakString hostTransmitted;
		RakString responseReceived;
		SystemAddress hostReceived;
		int contentOffset;
        FAST_RETURN;
		if (nm->httpConnection2->GetResponse(stringTransmitted, hostTransmitted, responseReceived, hostReceived, contentOffset)) {
			if (!responseReceived.IsEmpty()) {
				if (contentOffset == -1) {
					// No content
					printf(responseReceived.C_String());
				} else {
                    FAST_RETURN;
					// TODO: Parse the JSON response with rapidjson
					rapidjson::Document t;
					t.Parse(responseReceived.C_String() + contentOffset);
					// Stringify the DOM
				    rapidjson::StringBuffer buffer;
				    rapidjson::Writer<rapidjson::StringBuffer> writer(buffer);
				    t.Accept(writer);
				    cocos2d::log("Testing response %s\n",buffer.GetString());
					// json_error_t error;
					// json_t *root = json_loads(responseReceived.C_String() + contentOffset, JSON_REJECT_DUPLICATES, &error);
					// if (!root)
					// {
					// 	printf("Error parsing JSON\n", __LINE__);
					// }
					// else
					// {
					// 	void *iter = json_object_iter(root);
					// 	while (iter)
					// 	{
					// 		const char *firstKey = json_object_iter_key(iter);
					// 		if (stricmp(firstKey, "GET")==0)
					// 		{
					// 			game->SetMasterServerQueryResult(root);
					// 			root=0;

					// 			json_t* jsonArray = json_object_iter_value(iter);
					// 			size_t arraySize = json_array_size(jsonArray);
					// 			for (unsigned int i=0; i < arraySize; i++)
					// 			{
					// 				json_t* object = json_array_get(jsonArray, i);
					// 				json_t* roomNameVal = json_object_get(object, "roomName");
					// 				RakAssert(roomNameVal->type==JSON_STRING);
					// 				json_t* natTypesVal = json_object_get(object, "natTypes");
					// 				RakAssert(natTypesVal->type==JSON_ARRAY);
					// 				size_t natTypesSize = json_array_size(natTypesVal);
					// 				printf("Room name: %s. Players: %i\n", json_string_value(roomNameVal), natTypesSize);
					// 			}

					// 			if (arraySize==0) {
					// 				printf("No rooms.\n");
					// 			}

					// 			printf("(J)oin room\n");
					// 			printf("(C)reate room\n");
					// 			printf("(S)earch rooms\n");
					// 			break;
					// 		}
					// 		else if (stricmp(firstKey, "POST")==0)
					// 		{
					// 			RakAssert(stricmp(firstKey, "POST")==0);

					// 			json_t* jsonObject = json_object_iter_value(iter);
					// 			json_t* val1 = json_object_get(jsonObject, "__rowId");
					// 			RakAssert(val1->type==JSON_INTEGER);
					// 			game->masterServerRow = (int) json_integer_value(val1);

					// 			printf("Session posted to row %i\n", game->masterServerRow);
					// 			break;
					// 		}
					// 		else
					// 		{
					// 			iter = json_object_iter_next(root, iter);
					// 			RakAssert(iter != 0);
					// 		}
					// 	}
						
					// 	json_decref(root);
					// }
				}
			}
		}
        FAST_RETURN;
		//if (kbhit()) {
		//	ch = getch();

		//	if (game->phase==Game::SEARCH_FOR_GAMES) {
		//		if (ch=='c' || ch=='C') {
		//			CreateRoom();
		//		}
		//		if (ch=='s' || ch=='S') {
		//			game->SearchForGames();
		//		}
		//		else if (ch=='j' || ch=='J') {
		//			// size_t arraySize = 0;
		//			// json_t *jsonArray = game->GetMasterServerQueryResult();
		//			// if (jsonArray) {
		//			// 	arraySize = json_array_size(jsonArray);
		//			// }

		//			// Join room
		//			// if (arraySize == 0) {
		//			// 	printf("No rooms to join.\n");
		//			// } else {
		//			// 	int index;
		//			// 	if (arraySize > 1) {
		//			// 		printf("Enter index of room to join.\n");
		//			// 		char indexstr[64];
		//			// 		Gets(indexstr,64);
		//			// 		index = atoi(indexstr);
		//			// 	} else {
		//			// 		index = 0;
		//			// 	}

		//			// 	if (index < 0 || (unsigned int) index >= arraySize) {
		//			// 		printf("Index out of range.\n");
		//			// 	} else {
		//			// 		json_t* object = json_array_get(jsonArray, index);
		//			// 		json_t* guidVal = json_object_get(object, "guid");
		//			// 		RakAssert(guidVal->type==JSON_STRING);
		//			// 		RakNetGUID clientGUID;
		//			// 		clientGUID.FromString(json_string_value(guidVal));
		//			// 		if (clientGUID != rakPeer->GetMyGUID()) {
		//			// 			nm->natPunchthroughClient->OpenNAT(clientGUID, game->natPunchServerAddress);
		//			// 			game->EnterPhase(Game::NAT_PUNCH_TO_GAME_HOST);
		//			// 		} else {
		//			// 			printf("Cannot join your own room\n");
		//			// 		}
		//			// 	}
		//			// }
		//		}
		//	} else {
		//		if (game->phase == Game::IN_GAME) {
		//			if (ch=='c' || ch=='C') {
		//				DataStructures::List<RakNetGUID> participantList;
		//				nm->fullyConnectedMesh2->GetParticipantList(participantList);

		//				if (participantList.Size() > 0)
		//				{
		//					printf("Enter in-game chat message: ");
		//					char str[256];
		//					Gets(str, 256);
		//					RakString rs;
		//					// Don't use RakString constructor to assign str, or will process % escape characters
		//					rs = str;
		//					BitStream bsOut;
		//					bsOut.Write(rs);
		//					for (unsigned int i=0; i < participantList.Size(); i++) {
		//						nm->rpc4->Signal("InGameChat", &bsOut, HIGH_PRIORITY, RELIABLE_ORDERED, 0, participantList[i], false, false);
		//					}
		//				}
		//			}
		//		}

		//		if (ch == '1') {
		//			nm->user->tmTeamMember.RequestTeamSwitch(&game->teams[0]->tmTeam, 0);
		//		} else if (ch == '2') {
		//			nm->user->tmTeamMember.RequestTeamSwitch(&game->teams[1]->tmTeam, 0);
		//		} else if (ch == 'r' || ch == 'R') {
		//			if (nm->readyEvent->SetEvent(0, true))
		//				printf("We are ready to start.\n");
		//		} else if (ch == 'u' || ch == 'U') {
		//			if (nm->readyEvent->SetEvent(0, false)) {
		//				printf("We are no longer ready to start.\n");
		//			}
		//		} else if (ch == 'l' || ch == 'L') {
		//			if (nm->fullyConnectedMesh2->IsConnectedHost()) {
		//				if (game->lockGame) {
		//					printf("Game is no longer locked\n");
		//					game->lockGame=false;
		//				} else {
		//					printf("Game is now locked\n");
		//					game->lockGame=true;
		//				}
		//			}
		//		} else if (ch == 'b' || ch == 'B') {
		//			if (nm->fullyConnectedMesh2->IsConnectedHost()) {
		//				if (game->gameInLobby) {
		//					nm->readyEvent->ForceCompletion(0);
		//					game->gameInLobby=false;
		//					game->EnterPhase(Game::IN_GAME);
		//				} else {
		//					nm->readyEvent->DeleteEvent(0);
		//					printf("Game ended, and now in lobby\n");
		//					game->gameInLobby=true;
		//					game->EnterPhase(Game::IN_LOBBY_WITH_HOST);
		//				}
		//			}
		//		} else if (ch == 'e' || ch == 'E') {
		//			// Disconnect from FullyConnectedMesh2 participants
		//			DataStructures::List<RakNetGUID> participantList;
		//			nm->fullyConnectedMesh2->GetParticipantList(participantList);
		//			for (unsigned int i=0; i < participantList.Size(); i++) {
		//				nm->rakPeer->CloseConnection(participantList[i], true);
		//			}

		//			// User instances are deleted automatically from ReplicaManager3.
		//			// However, teams are not deleted since the Team class can migrate between systems. So delete Team instances manually
		//			while (game->teams.Size()) {
		//				delete game->teams[game->teams.Size()-1];
		//			}

		//			// If we were the host, no longer list this session
		//			// The new host will call PostRoomToCloud to reupload under a new IP address on ID_FCM2_NEW_HOST
		//			ReleaseRoomFromCloud();

		//			// Clear out state data from plugins
		//			nm->fullyConnectedMesh2->Clear();
		//			nm->readyEvent->DeleteEvent(0);
		//			nm->replicaManager3->Clear(false);
		//			nm->replicaManager3->Reference(game);

		//			game->Reset();
		//			game->EnterPhase(Game::SEARCH_FOR_GAMES);
		//		} else if (ch == 'q' || ch == 'Q') {
		//			printf("Quitting.\n");

		//			RakString rspost = RakString::FormatForGET(
		//				RakString(MASTER_SERVER_ADDRESS "/testServer?row=%i", game->masterServerRow));
		//			nm->httpConnection2->TransmitRequest(rspost, MASTER_SERVER_ADDRESS, MASTER_SERVER_PORT);

		//			game->EnterPhase(Game::EXIT_SAMPLE);
		//		}
		//	}
		//}

		// The game host updates the master server
		RakNet::Time t = RakNet::GetTime();
		if ((nm->fullyConnectedMesh2->IsConnectedHost() || game->users.Size()==1) &&
				t > game->whenToNextUpdateMasterServer &&
				(game->phase == Game::IN_LOBBY_WITH_HOST ||
				game->phase == Game::IN_GAME ||
				game->phase == Game::IN_LOBBY_WAITING_FOR_HOST)
			)
		{
			PostRoomToMaster();
		}
        FAST_RETURN;
		RakSleep(30);
	}
}

void NetworkManager::startNetwork() {
	// Start TCPInterface and begin connecting to the NAT punchthrough server
	nm->tcp->Start(0,0,1);

	// Connect to hosting server
	game->EnterPhase(Game::CONNECTING_TO_SERVER);
    
    nm->isAlive = true;
    read_thread = std::thread(readPackets);
	// TODO: make this the interruptable class and store it in the nm
	//RakThread::Create(readPackets, NULL);
}

void NetworkManager::searchForGames() {

}

bool NetworkManager::connect() {
	if (!NetworkManager::isConnected()) {
		return false;
	}
	return true;
}

bool NetworkManager::isConnected() {
	return false;
}

void NetworkManager::lock() {

}

void NetworkManager::unlock() {

}

void NetworkManager::isLocked() {

}

void NetworkManager::endNetwork() {
	// TODO: shutdown the packet reading thread?
}

void NetworkManager::destroy() {
    nm->isAlive = false;
    if (open_upnp_thread.joinable()) {
        open_upnp_thread.join();
    }
    if (read_thread.joinable()) {
        read_thread.join();
    }
    nm->rakPeer->Shutdown(100);
    while (game->teams.Size()) {
        delete game->teams[game->teams.Size()-1];
    }
    while (game->users.Size()) {
        delete game->users[game->users.Size()-1];
    }
    delete game;

    RakPeerInterface::DestroyInstance(nm->rakPeer);
    TeamManager::DestroyInstance(nm->teamManager);
    FullyConnectedMesh2::DestroyInstance(nm->fullyConnectedMesh2);
    NatPunchthroughClient::DestroyInstance(nm->natPunchthroughClient);
    NatTypeDetectionClient::DestroyInstance(nm->natTypeDetectionClient);
    RPC4::DestroyInstance(nm->rpc4);
    ReadyEvent::DestroyInstance(nm->readyEvent);
    delete nm->replicaManager3;
    NetworkIDManager::DestroyInstance(nm->networkIDManager);
    HTTPConnection2::DestroyInstance(nm->httpConnection2);
    free(nm);

    // also remove the SocketDescriptors
}
