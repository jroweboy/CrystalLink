#ifndef _PAUSEMENU_SCENE_H_
#define _PAUSEMENU_SCENE_H_

#include "cocos2d.h"

class PauseMenu : public cocos2d::CCLayer
{
public:
    virtual bool init();

    static cocos2d::Scene* scene();

    // functions for click even handling
    void onSave(Ref* pSender);
    void onLoad(Ref* pSender);
    void onLoadLevel(Ref* pSender);
    void onNetwork(Ref* pSender);
    void onOptions(Ref* pSender);
    void onQuit(Ref* pSender);

    // implement the “static node()” method manually
    CREATE_FUNC(PauseMenu);
};

#endif