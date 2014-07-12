#ifndef LEVEL__H
#define LEVEL__H

#include "Player.h"
#include "cocos2d.h"
#include "HudLayer.h"

using namespace cocos2d;

class Level: public Layer
{
private:

public:
    // TODO: Do these need to be private?
    Scene *_scene;
    HudLayer *_hud;

    // 
    std::vector<TMXLayer *> background;
    std::vector<TMXLayer *> foreground;
    TMXLayer *meta;

    // Characters
    TMXTiledMap *tileMap;
    std::vector<std::unique_ptr<Player>> player;
    std::map<std::string, std::unique_ptr<Entity>> npc;
    std::vector<std::unique_ptr<Entity>> monster;

    // Methods
    virtual bool init();
    static Scene* createScene();

    void setViewPointCenter(Point position);
    void setPlayerPosition(Point position);
    Point tileCoordForPosition(Point position);
    CREATE_FUNC(Level);
};

#endif