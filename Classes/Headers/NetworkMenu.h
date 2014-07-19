#ifndef _NETWORKMENU_SCENE_H_
#define _NETWORKMENU_SCENE_H_

#include "cocos2d.h"
#include "Input.h"
#include "CocosGUI.h"
#include "cocos-ext.h"

class NetworkMenu : public cocos2d::Layer
{
private:
    typedef cocos2d::Layer super;
    cocos2d::ui::Text *online_label;
    cocos2d::extension::EditBox *gamename;
    cocos2d::Menu *menu;
    float _timeSinceLastUpdate;

public:
    virtual bool init();

    static cocos2d::Scene* scene();

    void update(float dt);

    // functions for click even handling
    //void onSave(Ref* pSender);
    //void onLoad(Ref* pSender);
    //void onLoadLevel(Ref* pSender);
    //void onNetwork(Ref* pSender);
    //void onOptions(Ref* pSender);
    //void onQuit(Ref* pSender);

    //virtual void onKeyPressed(cocos2d::EventKeyboard::KeyCode keyCode, cocos2d::Event* event);
    //virtual void onKeyReleased(cocos2d::EventKeyboard::KeyCode keyCode, cocos2d::Event* event);
    void touchEvent(void);

    
    void SelectGame(cocos2d::Ref* pSender);
    
    void textFieldEvent(cocos2d::Ref *pSender, cocos2d::ui::TextField::EventType type);
    void selectedEvent(cocos2d::Ref* pSender, cocos2d::ui::CheckBoxEventType type);

    // implement the “static node()” method manually
    CREATE_FUNC(NetworkMenu);
};

#endif