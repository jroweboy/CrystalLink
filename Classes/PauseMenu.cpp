#include "PauseMenu.h"
#include "NetworkMenu.h"

using namespace cocos2d;

Scene* PauseMenu::scene() {
    Scene* scene = NULL;
        // 'scene' is an autorelease object
    scene = Scene::create();
    if (!scene) {
        return NULL;
    }

        // 'layer' is an autorelease object
    PauseMenu *layer = PauseMenu::create();
    if (!layer) {
        return NULL;
    }

    // add layer as a child to scene
    scene->addChild(layer);

    // return the Main menu scene
    return scene;
}

void PauseMenu::update(float dt) {
    super::update(dt);
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
bool PauseMenu::init() {
    if (!Layer::init()) {
        return false;
    }

    // Enable touch/click actions
    this->setTouchEnabled(true);

    auto listener = EventListenerKeyboard::create();
    listener->onKeyPressed = CC_CALLBACK_2(PauseMenu::onKeyPressed, this);
    listener->onKeyReleased = CC_CALLBACK_2(PauseMenu::onKeyReleased, this);
    _eventDispatcher->addEventListenerWithSceneGraphPriority(listener, this);

    MenuItemFont* item1 = MenuItemFont::create( "Save", this, menu_selector(PauseMenu::onSave) );
    MenuItemFont* item2 = MenuItemFont::create( "Load", this, menu_selector(PauseMenu::onLoad) );
    MenuItemFont* item3 = MenuItemFont::create( "Load Level (debug)", this, menu_selector(PauseMenu::onLoadLevel) );
    MenuItemFont* item4 = MenuItemFont::create( "Online Play", this, menu_selector(PauseMenu::onNetwork) );
    MenuItemFont* item5 = MenuItemFont::create( "Options", this, menu_selector(PauseMenu::onOptions) );
    MenuItemFont* item6 = MenuItemFont::create( "Quit", this, menu_selector(PauseMenu::onQuit) );

    // combine to form a menu and allign Vertically
    Menu* menu = Menu::create( item1, item2, item3, item4, item5, item6, NULL );
    menu->alignItemsVertically();

    // add this to the layer
    this->addChild( menu, 1 );
    
    this->scheduleUpdate();
    return true;
}


void PauseMenu::onSave(Ref* pSender) {
    
}

void PauseMenu::onLoad(Ref* pSender) {
    
}

void PauseMenu::onLoadLevel(Ref* pSender) {
    
}

void PauseMenu::onNetwork(Ref* pSender) {
    Director::getInstance()->pushScene(NetworkMenu::scene());
}

void PauseMenu::onOptions(Ref* pSender) {
    // TODO: Add keyboard binding options, graphics options (what options lol resolution?) and others.
}


void PauseMenu::onQuit(Ref* pSender) {
    // TODO: We probably want a separate Quit to main menu and Quit button?
    Director::sharedDirector()->end();
#if (CC_TARGET_PLATFORM == CC_PLATFORM_IOS)
    exit(0);
#endif
}

void PauseMenu::onKeyPressed(EventKeyboard::KeyCode keyCode, Event* event) {
    keysPressed.insert(keyCode);
}

void PauseMenu::onKeyReleased(EventKeyboard::KeyCode keyCode, Event* event) {
    keysPressed.erase(keyCode);
}