#ifndef LEVEL__H
#define LEVEL__H

#include "Player.h"
#include "cocos2d.h"
#include "HudLayer.h"
#include "NetworkManager.h"

using namespace cocos2d;

static EventKeyboard::KeyCode K_UP = EventKeyboard::KeyCode::KEY_W;
static EventKeyboard::KeyCode K_DOWN = EventKeyboard::KeyCode::KEY_S;
static EventKeyboard::KeyCode K_LEFT = EventKeyboard::KeyCode::KEY_A;
static EventKeyboard::KeyCode K_RIGHT = EventKeyboard::KeyCode::KEY_D;
static EventKeyboard::KeyCode K_SPELL = EventKeyboard::KeyCode::KEY_L;
static EventKeyboard::KeyCode K_ATTACK = EventKeyboard::KeyCode::KEY_K;
static EventKeyboard::KeyCode K_SWITCH_LEFT = EventKeyboard::KeyCode::KEY_I;
static EventKeyboard::KeyCode K_SWITCH_RIGHT = EventKeyboard::KeyCode::KEY_O;
static EventKeyboard::KeyCode K_MENU = EventKeyboard::KeyCode::KEY_ESCAPE;

class Level: public Layer
{
private:
    typedef Layer super;
    std::set<EventKeyboard::KeyCode> keysPressed;
    void initInput();

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
    void setViewPointCenter(Point position);
    void checkCollision(Point position);
    Point tileCoordForPosition(Point position);

    // overrides
    virtual bool init();
    static Scene* createScene();
    virtual void onKeyPressed(EventKeyboard::KeyCode keyCode, Event* event);
    virtual void onKeyReleased(EventKeyboard::KeyCode keyCode, Event* event);
    virtual void update(float dt);
    CREATE_FUNC(Level);
};

#endif