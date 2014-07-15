#include "Player.h"

USING_NS_CC;

Player::Player()
{
    direction = Direction::DOWN;
    state     = State::STAND;
}

Player::Player(DIRECTION d) 
    : Entity(d) {
    state = State::STAND;
}

Player::~Player(void)
{
    //actionStateDefault->release();
    for (auto it = actionStateMoving.begin(); it != actionStateMoving.end(); ++it) {
        it->second->release();
    }
}

bool Player::init() {
    return true;
}

bool Player::initWithFilename(std::string filename)
{
    // some variables
    Size visibleSize = Director::getInstance()->getVisibleSize();
    Point origin = Director::getInstance()->getVisibleOrigin();
    
    // node and spite
    batchNode = SpriteBatchNode::create(StringUtils::format("chars/%s.png", filename.c_str()));
    
    SpriteFrameCache::getInstance()->addSpriteFramesWithFile(StringUtils::format("chars/%s.plist", filename.c_str()));
    sprite = Sprite::createWithSpriteFrameName(StringUtils::format("%s_%c_0.png", filename.c_str(), this->direction));
    
    batchNode->setPosition(Point(visibleSize.width/2 + origin.x, visibleSize.height/2 + origin.y));
    
    // TODO: Implement idle animations.
    // For now we just return to the 0 frame and it'll look fine
    
    // animationIdle = Animation::create();
    // char szImageFileName[128] = {0};
    // sprintf(szImageFileName, "Orc_move_right00%02i.png", i);
    // animationIdle->addSpriteFrame(SpriteFrameCache::getInstance()->getSpriteFrameByName(szImageFileName));
    // animationIdle->setDelayPerUnit(1.0 / 1);
    // animationIdle->setRestoreOriginalFrame(true);
    
    // animateIdle = Animate::create(animationIdle);
    // actionStateDefault = RepeatForever::create(animateIdle);
    // actionStateDefault->retain();

    // TODO: implement moving animations :p
    for (int i=0; i < sizeof(_ALL_DIRECTIONS); ++i) {
        for (int j=0; j < 9; ++j) {
            if (j==0) {
                animationMoving[_ALL_DIRECTIONS[i]] = Animation::create();
            }
            animationMoving[_ALL_DIRECTIONS[i]]->addSpriteFrame(
                SpriteFrameCache::getInstance()->getSpriteFrameByName(
                    StringUtils::format("%s_%c_%d.png", filename.c_str(), _ALL_DIRECTIONS[i], j)
                )
            );
            animationMoving[_ALL_DIRECTIONS[i]]->setDelayPerUnit(0.04f);
            animationMoving[_ALL_DIRECTIONS[i]]->setRestoreOriginalFrame(true);
            
            animateMoving[_ALL_DIRECTIONS[i]] = Animate::create(animationMoving[_ALL_DIRECTIONS[i]]);
            actionStateMoving[_ALL_DIRECTIONS[i]] = RepeatForever::create(animateMoving[_ALL_DIRECTIONS[i]]);
            actionStateMoving[_ALL_DIRECTIONS[i]]->retain();
        }
    }
    //animationMoving = Animation::create();
    //
    //for (int i = 1; i < 17; i++)
    //{
    //    char szImageFileName[128] = {0};
    //    sprintf(szImageFileName, "Orc_move_right00%02i.png", i);
    //    animationMoving->addSpriteFrame(SpriteFrameCache::getInstance()->getSpriteFrameByName(szImageFileName));
    //}
    //
    //
    //animateMoving = Animate::create(animationMoving);
    //actionStateMoving = RepeatForever::create(animateMoving);
    //actionStateMoving->retain();

    batchNode->addChild(sprite);
    
    return true;
}

void Player::move(DIRECTION d)
{
    switch (d) {
    case Direction::UPRIGHT:
        changeDirection(Direction::UP);
        setPosition(getPosition() + Vec2(0.707,0.707));
        setStateMoving();
        break;
    case Direction::UP:
        changeDirection(Direction::UP);
        setPosition(getPosition() + Vec2(0,1));
        setStateMoving();
        break;
    case Direction::RIGHT:
        changeDirection(Direction::RIGHT);
        setPosition(getPosition() + Vec2(1,0));
        setStateMoving();
        break;
    case Direction::RIGHTDOWN:
		changeDirection(Direction::DOWN);
        setPosition(getPosition() + Vec2(0.707,-0.707));
        setStateMoving();
        break;
    case Direction::DOWN:
		changeDirection(Direction::DOWN);
        setPosition(getPosition() + Vec2(0,-1));
        setStateMoving();
        break;
    case Direction::DOWNLEFT:
		changeDirection(Direction::DOWN);
        setPosition(getPosition() + Vec2(-0.707,-0.707));
        setStateMoving();
        break;
    case Direction::LEFT:
		changeDirection(Direction::LEFT);
        setPosition(getPosition() + Vec2(-1,0));
        setStateMoving();
        break;
    case Direction::LEFTUP:
		changeDirection(Direction::UP);
        setPosition(getPosition() + Vec2(-0.707,0.707));
        setStateMoving();
        break;
    default:
        break;
    }
}

void Player::setStateDefault()
{
    if (state == State::WALK)
    {
        state = State::STAND;
		sprite->stopAllActions();
        //sprite->runAction(actionStateDefault);
    }
}

void Player::setStateMoving()
{
    if (state == State::STAND)
    {
        state = State::WALK;
		sprite->stopAllActions();
        sprite->runAction(actionStateMoving[direction]);
    }
}

void Player::stopMoving()
{
    if (state == State::WALK)
    {
        state = State::STAND;
        sprite->stopAllActions();
    }
}

//void Player::actionButtonPressed(int button)
//{
//    //if (button == 1)
//    //{
//    //    body->ApplyLinearImpulse(b2Vec2(0, body->GetMass() * 3), body->GetWorldCenter());
//    //}
//}

void Player::changeDirection(DIRECTION d)
{
    this->direction = d;
}

TAG Player::getTag()
{
    return Tag::PLAYER;
}
