#include "NetworkMenu.h"
#include "NetworkManager.h"

using namespace cocos2d;
using namespace cocos2d::ui;

Scene* NetworkMenu::scene() {
    Scene* scene = NULL;
        // 'scene' is an autorelease object
    scene = Scene::create();
    if (!scene) {
        return NULL;
    }

        // 'layer' is an autorelease object
    NetworkMenu *layer = NetworkMenu::create();
    if (!layer) {
        return NULL;
    }

    // add layer as a child to scene
    scene->addChild(layer);

    // return the Main menu scene
    return scene;
}

void NetworkMenu::update(float dt) {
    super::update(dt);
    _timeSinceLastUpdate += dt;
    if (_timeSinceLastUpdate > 1) {
        _timeSinceLastUpdate = 0;
        //NetworkManager::gameList();
    }
    if (KEYPRESSED(K_UP)) {
    } else if (KEYPRESSED(K_RIGHT)) {
    } else if (KEYPRESSED(K_DOWN)) {
	} else if (KEYPRESSED(K_LEFT)) {
	} else if (KEYPRESSED(K_MENU)) {
        keysPressed.erase(K_MENU);
        Director::getInstance()->popScene();
    } else { // add all the other keys as an else if
    }
}

// on "init" you need to initialize your instance
bool NetworkMenu::init() {
    if (!Layer::init()) {
        return false;
    }

    // Enable touch/click actions
    this->setTouchEnabled(true);

    // TODO: Integrate cocos studio for this crap :p
    auto size = Director::getInstance()->getWinSize();
    CheckBox* checkBox = CheckBox::create();
    checkBox->setTouchEnabled(true);
    checkBox->loadTextures("cocosgui/check_box_normal.png",
                           "cocosgui/check_box_normal_press.png",
                           "cocosgui/check_box_active.png",
                           "cocosgui/check_box_normal_disable.png",
                           "cocosgui/check_box_active_disable.png");
    checkBox->setPosition(Point(size.width / 3.0f, size.height - 50));
    checkBox->addEventListenerCheckBox(this, checkboxselectedeventselector(NetworkMenu::selectedEvent));
    online_label = Text::create();
    online_label->setText("Offline");
    online_label->setFontSize(32);
    //online_label->setAnchorPoint(Point(0.5f, -1));
    online_label->setPosition(Point(size.width / 3.0f + 75, size.height-50));
    //gamename = TextField::create("Game Name:", "Veranda", 14);
    //gamename->setPosition(Point(size.width / 3.0f + 75, size.height-100));
    //gamename->setMaxLength(12);
    //gamename->addEventListener(CC_CALLBACK_2(NetworkMenu::textFieldEvent, this));

    gamename = cocos2d::extension::EditBox::create(Size(200,50), cocos2d::extension::Scale9Sprite::create("cocosgui/green_edit.png"));
    gamename->setPosition(Point(size.width / 3.0f + 75, size.height-100));
    gamename->setFontColor(Color3B(255,105,180));
    gamename->setPlaceHolder("Game Name:");
    gamename->setMaxLength(8);
    gamename->setReturnType(cocos2d::extension::EditBox::KeyboardReturnType::DONE);
    this->gamename->retain();

    this->addChild(checkBox);
    this->addChild(online_label);
    //this->addChild(gamename);
    //MenuItemFont* item1 = MenuItemFont::create( "Save", this, menu_selector(NetworkMenu::SelectGame) );
    
    this->scheduleUpdate();
    return true;
}

void NetworkMenu::touchEvent(void) {

}

void NetworkMenu::SelectGame(Ref* pSender) {

}

void NetworkMenu::selectedEvent(Ref* pSender, CheckBoxEventType type)
{
    switch (type)
    {
        case CheckBox::EventType::SELECTED:
            NetworkManager::init();
            NetworkManager::startNetwork();
            this->addChild(gamename);
            //this->addChild(menu);
            online_label->setString("Online");
            break;

        case CheckBox::EventType::UNSELECTED:
            NetworkManager::destroy();
            //this->removeChild(menu);
            this->removeChild(gamename);
            online_label->setString("Offline");
            break;

        default:
            break;
    }
}

//void NetworkMenu::onSave(Ref* pSender) {
//    
//}
//
//void NetworkMenu::onLoad(Ref* pSender) {
//    
//}
//
//void NetworkMenu::onLoadLevel(Ref* pSender) {
//    
//}
//
//void PauseMenu::onNetwork(Ref* pSender) {
//    Director::getInstance()->pushScene();
//}
//
//void PauseMenu::onOptions(Ref* pSender) {
//    // TODO: Add keyboard binding options, graphics options (what options lol resolution?) and others.
//}
//
//
//void PauseMenu::onQuit(Ref* pSender) {
//    // TODO: We probably want a separate Quit to main menu and Quit button?
//    Director::sharedDirector()->end();
//#if (CC_TARGET_PLATFORM == CC_PLATFORM_IOS)
//    exit(0);
//#endif
//}
//
//void PauseMenu::onKeyPressed(EventKeyboard::KeyCode keyCode, Event* event) {
//    keysPressed.insert(keyCode);
//}
//
//void PauseMenu::onKeyReleased(EventKeyboard::KeyCode keyCode, Event* event) {
//    keysPressed.erase(keyCode);
//}