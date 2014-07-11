#ifndef __HELLOWORLD_SCENE_H__
#define __HELLOWORLD_SCENE_H__

#pragma GCC system_header
#include "cocos2d.h"
#include "HudLayer.h"

using namespace cocos2d;

class TestScene: public cocos2d::Layer
{
private:
    TMXTiledMap *_tileMap;
    
    std::vector<TMXLayer *> _background;
    
    Sprite *_player;
    
    TMXLayer *_meta;
    
    std::vector<TMXLayer *> _foreground;
    
    HudLayer *_hud;
    
    int _numCollected;
    
public:
    // Method 'init' in cocos2d-x returns bool, instead of 'id' in cocos2d-iphone (an object pointer)
    virtual bool init();
    
    // there's no 'id' in cpp, so we recommend to return the class instance pointer
    static Scene* createScene();
    
    // a selector callback
    // void menuCloseCallback(Ref* pSender);
    
    void setViewPointCenter(Point position);
    
    //void registerWithTouchDispatcher();
    
    void setPlayerPosition(Point position);
    
    //bool ccTouchBegan(Touch *touch, Event *event);
    
    //void ccTouchEnded(Touch *touch, Event *event);
    
    Point tileCoordForPosition(Point position);

    // preprocessor macro for "static create()" constructor ( node() deprecated )
    CREATE_FUNC(TestScene);
};

#endif // __HELLOWORLD_SCENE_H__