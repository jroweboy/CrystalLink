#ifndef _PAUSEMENU_SCENE_H_
#define _PAUSEMENU_SCENE_H_

#include "cocos2d.h"
#include "Input.h"

class PauseMenu : public cocos2d::CCLayer
{
private:
    typedef cocos2d::Layer super;
public:
    virtual bool init();

    static cocos2d::Scene* scene();

    void update(float dt);

    // functions for click even handling
    void onSave(Ref* pSender);
    void onLoad(Ref* pSender);
    void onLoadLevel(Ref* pSender);
    void onNetwork(Ref* pSender);
    void onOptions(Ref* pSender);
    void onQuit(Ref* pSender);

    virtual void onKeyPressed(cocos2d::EventKeyboard::KeyCode keyCode, cocos2d::Event* event);
    virtual void onKeyReleased(cocos2d::EventKeyboard::KeyCode keyCode, cocos2d::Event* event);

    // implement the “static node()” method manually
    CREATE_FUNC(PauseMenu);
};

#endif