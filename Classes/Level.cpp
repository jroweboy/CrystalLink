#include "Level.h"
#include "SimpleAudioEngine.h"
#include "PauseMenu.h"

using namespace cocos2d;

#define KEYPRESSED(x) (std::find(keysPressed.begin(), keysPressed.end(), x) != keysPressed.end())

//TODO convert this into a LUA file with most the work done in C++
Scene* Level::createScene()
{
    // 'scene' is an autorelease object
    Scene *scene = Scene::create();
    
    // 'layer' is an autorelease object
    Level *layer = Level::create();
    
    // add layer as a child to scene
    scene->addChild(layer);
    
    HudLayer *hud = new HudLayer();
    hud->init();
    scene->addChild(hud);
    layer->_hud = hud;
    
    // return the scene
    return scene;
}

// on "init" you need to initialize your instance
bool Level::init()
{
    if ( !Layer::init() )
    {
        return false;
    }
    
    // Add keyboard listeners
    auto listener = EventListenerKeyboard::create();
    listener->onKeyPressed = CC_CALLBACK_2(Level::onKeyPressed, this);
    listener->onKeyReleased = CC_CALLBACK_2(Level::onKeyReleased, this);
    _eventDispatcher->addEventListenerWithSceneGraphPriority(listener, this);

    //CocosDenshion::SimpleAudioEngine::sharedEngine()->preloadEffect("pickup.caf");
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->preloadEffect("hit.caf");
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->preloadEffect("move.caf");
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->playBackgroundMusic("TileMap.caf");
    tileMap = TMXTiledMap::create("maps/AdventurerPath.tmx");
    background.push_back(tileMap->getLayer("Ground"));
    background.push_back(tileMap->getLayer("GroundHelper"));
	background.push_back(tileMap->getLayer("Path"));
	
    
    meta = tileMap->getLayer("Meta");
	if (meta) {
	    meta->setVisible(false);
	}

    this->addChild(tileMap);
    
    TMXObjectGroup *objectGroup = tileMap->getObjectGroup("Objects");
    
    if(objectGroup == NULL){
        log("tile map has no objects object layer");
        return false;
    }
    
    auto spawnPoint = objectGroup->getObject("SpawnPoint");
    
    int x = spawnPoint["x"].asInt();
    int y = spawnPoint["y"].asInt();
    //Sprite::create("TileGameResources/Player.png");
    auto player1 = std::unique_ptr<Player>(new Player);
    player1->initWithFilename("soldier");
    player1->setPosition(Vec2(x,y));
    this->setViewPointCenter(player1->getPosition());

    // TODO: I don't know how I'm going to make the players work in multiplayer online :p
    player.push_back(std::move(player1));
    
    for (auto it = player.begin(); it != player.end(); ++it) {
        this->addChild((*it)->getBatchNode());
    }

    this->scheduleUpdate();

    return true;
}

void Level::setViewPointCenter(Point position)
{
    Size winSize = Director::getInstance()->getWinSize();
    
    int x = MAX(position.x, winSize.width/2);
    int y = MAX(position.y, winSize.height/2);
    x = MIN(x, (tileMap->getMapSize().width * this->tileMap->getTileSize().width) - winSize.width / 2);
    y = MIN(y, (tileMap->getMapSize().height * tileMap->getTileSize().height) - winSize.height/2);
    Point actualPosition = Vec2(x, y);
    
    Point centerOfView = Vec2(winSize.width/2, winSize.height/2);
    Point viewPoint = centerOfView - actualPosition;
    this->setPosition(viewPoint);
}

void Level::update(float dt) {
    super::update(dt);
    if (KEYPRESSED(K_UP)) {
        if (KEYPRESSED(K_RIGHT)) {
            player[0]->move(Direction::UPRIGHT);
        } else if (KEYPRESSED(K_LEFT)) {
            player[0]->move(Direction::LEFTUP);
        } else {
            player[0]->move(Direction::UP);
        }
    } else if (KEYPRESSED(K_RIGHT)) {
        if (KEYPRESSED(K_DOWN)) {
            player[0]->move(Direction::RIGHTDOWN);
        } else {
            player[0]->move(Direction::RIGHT);
        }
    } else if (KEYPRESSED(K_DOWN)) {
		if (KEYPRESSED(K_LEFT)) {
			player[0]->move(Direction::DOWNLEFT);
		} else {
			player[0]->move(Direction::DOWN);
		}
	} else if (KEYPRESSED(K_LEFT)) {
		player[0]->move(Direction::LEFT);
	} else if (KEYPRESSED(K_MENU)) {
        Director::getInstance()->pushScene(PauseMenu::scene());
    } else { // add all the other keys as an else if
        player[0]->stopMoving();
    }
}

void Level::onKeyPressed(EventKeyboard::KeyCode keyCode, Event* event)
{
    keysPressed.insert(keyCode);
}

void Level::onKeyReleased(EventKeyboard::KeyCode keyCode, Event* event)
{
    keysPressed.erase(keyCode);
}

void Level::checkCollision(Point position)
{
    // TODO: Implement position checking?
    // This should be abstracted to a single location

    //Point tileCoord = this->tileCoordForPosition(position);
    //int tileGid = _meta->tileGIDAt(tileCoord);
    //if (tileGid) {
    //    auto properties = _tileMap->propertiesForGID(tileGid).asValueMap();
    //    if (!properties.empty()) {
    //        String *collision = new String();
    //        collision = properties["Collidable"].asString();
    //        if (collision && (collision->compare("True") == 0)) {
    //            //CocosDenshion::SimpleAudioEngine::sharedEngine()->playEffect("hit.caf");
    //            return;
    //        }
    //        CCString *collectible = new CCString();
    //        *collectible = *properties->valueForKey("Collectable");
    //        if (collectible && (collectible->compare("True") == 0)) {
    //            _meta->removeTileAt(tileCoord);
    //            _foreground->removeTileAt(tileCoord);
    //            _numCollected++;
    //            _hud->numCollectedChanged(_numCollected);
    //            //CocosDenshion::SimpleAudioEngine::sharedEngine()->playEffect("pickup.caf");
    //        }
    //    }
    //}
    //player->setPosition(position);
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->playEffect("move.caf");
}

Point Level::tileCoordForPosition(Point position)
{
    int x = position.x / tileMap->getTileSize().width;
    int y = ((tileMap->getMapSize().height * tileMap->getTileSize().height) - position.y) / tileMap->getTileSize().height;
    return Vec2(x, y);
}

void Level::initInput() {
    // TODO save input and load them from file here
}