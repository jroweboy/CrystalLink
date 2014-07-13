#ifndef ENTITY_H
#define ENTITY_H

#pragma warning(push, 0)
#pragma GCC system_header
#include "cocos2d.h"
#pragma warning(pop)
// #include "Box2D/Box2D.h"

// #define PTM_RATIO 32.0

typedef int STATE;
enum State {
    STAND,
    WALK,
    SPRINT, // Maybe we can add this somehow?
    FLY,
};

typedef char DIRECTION;
static char _ALL_DIRECTIONS[4] = {'l', 'u', 'r', 'd'};
class Direction {
public:
    static char const LEFT = 'l';
    //LEFTUP,
    static char const UP = 'u';
    //UPRIGHT,
    static char const RIGHT = 'r';
    //RIGHTDOWN,
    static char const DOWN = 'd';
    //DOWNLEFT
};

typedef int TAG;
enum Tag {
    PLAYER,
    MONSTER,
    BOSS,
    NPC,
    ENTITY
};


class Entity
{

public:
    
    Entity() {}
    Entity(DIRECTION d) : direction(d){}
    ~Entity() {}
    void setBatchNode(cocos2d::SpriteBatchNode *batchNode);
    cocos2d::SpriteBatchNode* getBatchNode() { return this->batchNode; }
    cocos2d::Sprite* getSprite() { return this->sprite; }
    DIRECTION getDirection();
    STATE getState();
    TAG getTag() { return Tag::ENTITY; }
    cocos2d::Vec2 getPosition() { return this->sprite->getPosition();}
    void setPosition(cocos2d::Vec2 p) { this->sprite->setPosition(p); }

protected:

    cocos2d::Sprite *sprite;
    // cocos2d::RepeatForever *actionStateIdle;
    // cocos2d::Animate *animateDefault;
    // cocos2d::Animation *animationDefault;

    std::map<DIRECTION, cocos2d::RepeatForever*> actionStateMoving;
    std::map<DIRECTION, cocos2d::Animate*> animateMoving;
    std::map<DIRECTION, cocos2d::Animation*> animationMoving;
    cocos2d::SpriteBatchNode *batchNode;

    DIRECTION direction;
    //b2Body *body;
    //b2World *world;
    STATE state;

};

#endif // ENTITY_H
