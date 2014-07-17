#include "NetworkManager.h"
#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include <iostream>
using namespace RakNet;

class Game;
class Team;
class User;

NetworkMger *nm;
Game *game;


#define DEFAULT_SERVER_PORT "61111"
// Public test server
#define DEFAULT_SERVER_ADDRESS "natpunch.jenkinssoftware.com"
#define NAT_TYPE_DETECTION_SERVER 0
#define MASTER_SERVER_ADDRESS "masterserver2.raknet.com"
//#define MASTER_SERVER_ADDRESS "localhost"
#define MASTER_SERVER_PORT 80





void PostRoomToMaster();

class Game : public Replica3
{
public:
    
    enum Phase
    {
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
	// RakNetGUID natPunchServerGuid;
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
	virtual RM3SerializationResult Serialize(SerializeParameters *serializeParameters)
	{
		serializeParameters->outputBitstream[0].Write(lockGame);
		serializeParameters->outputBitstream[0].Write(gameInLobby);
		serializeParameters->outputBitstream[0].Write(masterServerRow);
		return RM3SR_BROADCAST_IDENTICALLY;
	}
	virtual void Deserialize(DeserializeParameters *deserializeParameters)
	{
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
					game->EnterPhase(Game::IN_LOBBY_WITH_HOST);
					printf("Game is now in the lobby\n");
				} else {
					nm->readyEvent->ForceCompletion(0);
					game->EnterPhase(Game::IN_GAME);
				}
			}
			deserializeParameters->serializationBitstream[0].Read(masterServerRow);
		}
	}
	void EnterPhase(Phase newPhase)
	{
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
		printf("Downloading rooms...\n");

		RakString rsRequest = RakString::FormatForGET(
			MASTER_SERVER_ADDRESS "/testServer?__gameId=comprehensivePCGame");
		nm->httpConnection2->TransmitRequest(rsRequest, MASTER_SERVER_ADDRESS, MASTER_SERVER_PORT);
	}

	// Helper function to store and read the JSON from the GET request
	// void SetMasterServerQueryResult(json_t *root) {
	//     if (masterServerQueryResult) {
	// 	       json_decref(masterServerQueryResult);
	//     }
	//     masterServerQueryResult = root;
	// }

	// Document* GetMasterServerQueryResult(void)
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
		if (nm->fullyConnectedMesh2->IsConnectedHost())
			return QueryConstruction_PeerToPeer(destinationConnection, R3P2PM_MULTI_OWNER_CURRENTLY_AUTHORITATIVE);
		else
			return QueryConstruction_PeerToPeer(destinationConnection, R3P2PM_MULTI_OWNER_NOT_CURRENTLY_AUTHORITATIVE);
	}
	virtual bool QueryRemoteConstruction(RakNet::Connection_RM3 *sourceConnection) {return true;}
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
	virtual bool DeserializeDestruction(RakNet::BitStream *destructionBitstream, RakNet::Connection_RM3 *sourceConnection) {return true;}
	virtual RakNet::RM3ActionOnPopConnection QueryActionOnPopConnection(RakNet::Connection_RM3 *droppedConnection) const {
		// Do not destroy the object when the connection that created it disconnects.
		return RM3AOPC_DO_NOTHING;
	}
	virtual void DeallocReplica(RakNet::Connection_RM3 *sourceConnection) {delete this;}
	virtual RakNet::RM3QuerySerializationResult QuerySerialization(RakNet::Connection_RM3 *destinationConnection) {
		// Whoever is currently the host serializes the class
		if (nm->fullyConnectedMesh2->IsConnectedHost())
			return QuerySerialization_PeerToPeer(destinationConnection, R3P2PM_MULTI_OWNER_CURRENTLY_AUTHORITATIVE);
		else
			return QuerySerialization_PeerToPeer(destinationConnection, R3P2PM_MULTI_OWNER_NOT_CURRENTLY_AUTHORITATIVE);
	}
	virtual RM3SerializationResult Serialize(RakNet::SerializeParameters *serializeParameters) {return RM3SR_BROADCAST_IDENTICALLY;}
	virtual void Deserialize(RakNet::DeserializeParameters *deserializeParameters) {}

	// Team data managed by the TeamManager plugin
	TM_Team tmTeam;

	// Example of team data not managed by TeamManager
	RakString teamName;
};



class User : public Replica3
{
public:
	User() {
        game->users.Push(this, _FILE_AND_LINE_); 
        tmTeamMember.SetOwner(this); natType=NAT_TYPE_UNKNOWN;
    }
	virtual ~User() {
		game->users.RemoveAtIndex(game->users.GetIndexOf(this));
	}
	virtual void WriteAllocationID(RakNet::Connection_RM3 *destinationConnection, RakNet::BitStream *allocationIdBitstream) const {allocationIdBitstream->Write("User");}
	virtual RM3ConstructionState QueryConstruction(RakNet::Connection_RM3 *destinationConnection, ReplicaManager3 *replicaManager3)
	{
		// Whoever created the user replicates it.
		return QueryConstruction_PeerToPeer(destinationConnection);
	}
	virtual bool QueryRemoteConstruction(RakNet::Connection_RM3 *sourceConnection) {return true;}
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
class SimpleConnectionRM3 : public Connection_RM3
{
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
	devlist = upnpDiscover(args->timeout, 0, 0, 0, 0, 0);
	RakNet::Time t2 = GetTime();
	if (devlist)
	{
		if (args->progressCallback)
			args->progressCallback("List of UPNP devices found on the network :\n", args->userData);
		struct UPNPDev * device;
		for(device = devlist; device; device = device->pNext)
		{
			sprintf(args->buff, " desc: %s\n st: %s\n\n", device->descURL, device->st);
			if (args->progressCallback)
				args->progressCallback(args->buff, args->userData);
		}

		char lanaddr[64];	/* my ip address on the LAN */
		struct UPNPUrls urls;
		struct IGDdatas data;
		if (UPNP_GetValidIGD(devlist, &urls, &data, lanaddr, sizeof(lanaddr))==1)
		{
			char iport[32];
			Itoa(args->portToOpen, iport,10);
			char eport[32];
			strcpy(eport, iport);

			// Version miniupnpc-1.6.20120410
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
	RakThread::Create(UPNPOpenWorker, args);
}

void UPNPProgressCallback(const char *progressMsg, void *userData)
{
	printf(progressMsg);
}
void UPNPResultCallback(bool success, unsigned short portToOpen, void *userData)
{
	if (success)
		game->myNatType=NAT_TYPE_SUPPORTS_UPNP;
	game->EnterPhase(Game::SEARCH_FOR_GAMES);
}

void OpenUPNP(void)
{
	printf("Discovering UPNP...\n");

	DataStructures::List<RakNetSocket2* > sockets;
	nm->rakPeer->GetSockets(sockets);
	UPNPOpenAsynch(sockets[0]->GetBoundAddress().GetPort(), 2000, UPNPProgressCallback, UPNPResultCallback, 0);
}



// initialize the network manager

void NetworkManager::init() {
    nm = (NetworkMger*) malloc(sizeof(NetworkMger));
    game = new Game();
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
	User *user = new User();
	user->SetNetworkIDManager(nm->networkIDManager);
	user->userName = nm->rakPeer->GetMyGUID().ToString();
	// Inform TeamManager of my user's team member info
	nm->teamManager->GetWorldAtIndex(0)->ReferenceTeamMember(&user->tmTeamMember, user->GetNetworkID());

	// ------------------------------------------------------------------------------
	// Startup RakNet on first available port
	// ------------------------------------------------------------------------------
	nm->sd.socketFamily = AF_INET; // Only IPV4 supports broadcast on 255.255.255.255
	nm->sd.port = 0;
	StartupResult sr = nm->rakPeer->Startup(8, &nm->sd, 1);
	// ummm TODO, lets NOT have random asserts in the middle of the code :p
	RakAssert(sr == RAKNET_STARTED);
	nm->rakPeer->SetMaximumIncomingConnections(8);
	nm->rakPeer->SetTimeoutTime(30000, UNASSIGNED_SYSTEM_ADDRESS);
}

void NetworkManager::startSever() {

}

void NetworkManager::searchForGames() {

}

void NetworkManager::connect() {

}

void NetworkManager::isConnected() {

}

void NetworkManager::lock() {

}

void NetworkManager::unlock() {

}

void NetworkManager::isLocked() {

}

void NetworkManager::endConnection() {

}

void NetworkManager::destroy() {
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
    delete nm;
}
