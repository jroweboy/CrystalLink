#ifndef __LEVEL__H__
#define __LEVEL__H__

#include "Player.h"
#include "cocos2d.h"
#include "HudLayer.h"
#include "NetworkManager.h"
#include "Input.h"

class Level: public cocos2d::Layer
{
private:
    typedef cocos2d::Layer super;
    void initInput();

public:

    // TODO: Do these need to be private?
    cocos2d::Scene *_scene;
    HudLayer *_hud;

    // 
    std::vector<cocos2d::TMXLayer *> background;
    std::vector<cocos2d::TMXLayer *> foreground;
    cocos2d::TMXLayer *meta;

    // Characters
    cocos2d::TMXTiledMap *tileMap;
    std::vector<std::unique_ptr<Player>> player;
    std::map<std::string, std::unique_ptr<Entity>> npc;
    std::vector<std::unique_ptr<Entity>> monster;

    // Methods
    void setViewPointCenter(cocos2d::Point position);
    void checkCollision(cocos2d::Point position);
    Point tileCoordForPosition(cocos2d::Point position);

    // overrides
    virtual bool init();
    static cocos2d::Scene* createScene();
    virtual void onKeyPressed(cocos2d::EventKeyboard::KeyCode keyCode, Event* event);
    virtual void onKeyReleased(cocos2d::EventKeyboard::KeyCode keyCode, Event* event);
    virtual void update(float dt);
    CREATE_FUNC(Level);
};

#endif