#ifndef __HELLOWORLD_SCENE_H__
#define __HELLOWORLD_SCENE_H__

#include "cocos2d.h"
#include "HudLayer.h"

using namespace cocos2d;

class TestScene: public cocos2d::CCLayer
{
private:
    TMXTiledMap *_tileMap;
    
    TMXLayer *_background;
    
    Sprite *_player;
    
    TMXLayer *_meta;
    
    TMXLayer *_foreground;
    
    HudLayer *_hud;
    
    int _numCollected;
    
public:
    // Method 'init' in cocos2d-x returns bool, instead of 'id' in cocos2d-iphone (an object pointer)
    virtual bool init();
    
    // there's no 'id' in cpp, so we recommend to return the class instance pointer
    static Scene* createScene();
    
    // a selector callback
    void menuCloseCallback(CCObject* pSender);
    
    void setViewPointCenter(CCPoint position);
    
    //void registerWithTouchDispatcher();
    
    void setPlayerPosition(CCPoint position);
    
    //bool ccTouchBegan(CCTouch *touch, CCEvent *event);
    
    //void ccTouchEnded(CCTouch *touch, CCEvent *event);
    
    CCPoint tileCoordForPosition(CCPoint position);

    // preprocessor macro for "static create()" constructor ( node() deprecated )
    CREATE_FUNC(TestScene);
};

#endif // __HELLOWORLD_SCENE_H__