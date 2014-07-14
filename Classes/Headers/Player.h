#ifndef PLAYER_H
#define PLAYER_H

// #include "Box2D/Box2D.h"
#include "Entity.h"

// #define PTM_RATIO 32.0

class Player : public Entity
{
private:

public:
    Player();
    Player(DIRECTION d) ;
    ~Player();
    virtual bool init();
    bool initWithFilename(std::string filename);
    //void update(float dt);
    //void updateVelocity(cocos2d::Point velocity);
    void move(DIRECTION d);
    void stopMoving();
    //void actionButtonPressed(int button);
    
    void setStateDefault();
    void setStateMoving();
    void changeDirection(DIRECTION d);

    TAG getTag();
    
    //CREATE_FUNC(Player);
};

#endif
