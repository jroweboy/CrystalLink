#include "TestScene.h"
#include "SimpleAudioEngine.h"

using namespace cocos2d;

Scene* TestScene::createScene()
{
    // 'scene' is an autorelease object
    Scene *scene = Scene::create();
    
    // 'layer' is an autorelease object
    TestScene *layer = TestScene::create();
    
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
bool TestScene::init()
{
    if ( !Layer::init() )
    {
        return false;
    }
    
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->preloadEffect("pickup.caf");
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->preloadEffect("hit.caf");
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->preloadEffect("move.caf");
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->playBackgroundMusic("TileMap.caf");
    _tileMap = TMXTiledMap::create("maps/AdventurerPath.tmx");
    _background.push_back(_tileMap->layerNamed("Ground"));
    _background.push_back(_tileMap->layerNamed("GroundHelper"));
	_background.push_back(_tileMap->layerNamed("Path"));
	
    
    _meta = _tileMap->layerNamed("Meta");
	if (_meta) {
	    _meta->setVisible(false);
	}

    this->addChild(_tileMap);
    
    TMXObjectGroup *objectGroup = _tileMap->objectGroupNamed("Objects");
    
    if(objectGroup == NULL){
        CCLog("tile map has no objects object layer");
        return false;
    }
    
    auto spawnPoint = objectGroup->objectNamed("SpawnPoint");
    
    int x = spawnPoint["x"].asInt();
    int y = spawnPoint["y"].asInt();
    
    _player = Sprite::create("TileGameResources/Player.png");
    _player->setPosition(ccp(x,y));
    
    this->addChild(_player);
    this->setViewPointCenter(_player->getPosition());
    
    this->setTouchEnabled(true);
    
    return true;
}

void TestScene::setViewPointCenter(Point position)
{
    Size winSize = Director::sharedDirector()->getWinSize();
    
    int x = MAX(position.x, winSize.width/2);
    int y = MAX(position.y, winSize.height/2);
    x = MIN(x, (_tileMap->getMapSize().width * this->_tileMap->getTileSize().width) - winSize.width / 2);
    y = MIN(y, (_tileMap->getMapSize().height * _tileMap->getTileSize().height) - winSize.height/2);
    Point actualPosition = ccp(x, y);
    
    Point centerOfView = ccp(winSize.width/2, winSize.height/2);
    Point viewPoint = ccpSub(centerOfView, actualPosition);
    this->setPosition(viewPoint);
}

#pragma mark - handle touches

//void TestScene::registerWithTouchDispatcher()
//{
//    CCDirector::sharedDirector()->getTouchDispatcher()->addTargetedDelegate(this, 0, true);
//}

//bool TestScene::ccTouchBegan(CCTouch *touch, CCEvent *event)
//{
//    return true;
//}

void TestScene::setPlayerPosition(Point position)
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
    _player->setPosition(position);
    //CocosDenshion::SimpleAudioEngine::sharedEngine()->playEffect("move.caf");
}

//void TestScene::ccTouchEnded(CCTouch *touch, CCEvent *event)
//{
//    CCPoint touchLocation = touch->getLocationInView();
//    touchLocation = CCDirector::sharedDirector()->convertToGL(touchLocation);
//    touchLocation = this->convertToNodeSpace(touchLocation);
//    
//    CCPoint playerPos = _player->getPosition();
//    CCPoint diff = ccpSub(touchLocation, playerPos);
//    
//    if ( abs(diff.x) > abs(diff.y) ) {
//        if (diff.x > 0) {
//            playerPos.x += _tileMap->getTileSize().width;
//        } else {
//            playerPos.x -= _tileMap->getTileSize().width;
//        }
//    } else {
//        if (diff.y > 0) {
//            playerPos.y += _tileMap->getTileSize().height;
//        } else {
//            playerPos.y -= _tileMap->getTileSize().height;
//        }
//    }
//    
//    // safety check on the bounds of the map
//    if (playerPos.x <= (_tileMap->getMapSize().width * _tileMap->getTileSize().width) &&
//        playerPos.y <= (_tileMap->getMapSize().height * _tileMap->getTileSize().height) &&
//        playerPos.y >= 0 &&
//        playerPos.x >= 0 )
//    {
//        this->setPlayerPosition(playerPos);
//    }
//    
//    this->setViewPointCenter(_player->getPosition());
//}

Point TestScene::tileCoordForPosition(Point position)
{
    int x = position.x / _tileMap->getTileSize().width;
    int y = ((_tileMap->getMapSize().height * _tileMap->getTileSize().height) - position.y) / _tileMap->getTileSize().height;
    return ccp(x, y);
}
