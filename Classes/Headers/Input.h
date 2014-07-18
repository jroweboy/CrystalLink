#ifndef __INPUT_HEADER_H__
#define __INPUT_HEADER_H__

#include "Entity.h"
#include "cocos2d.h"

static cocos2d::EventKeyboard::KeyCode K_UP = cocos2d::EventKeyboard::KeyCode::KEY_W;
static cocos2d::EventKeyboard::KeyCode K_DOWN = cocos2d::EventKeyboard::KeyCode::KEY_S;
static cocos2d::EventKeyboard::KeyCode K_LEFT = cocos2d::EventKeyboard::KeyCode::KEY_A;
static cocos2d::EventKeyboard::KeyCode K_RIGHT = cocos2d::EventKeyboard::KeyCode::KEY_D;
static cocos2d::EventKeyboard::KeyCode K_SPELL = cocos2d::EventKeyboard::KeyCode::KEY_L;
static cocos2d::EventKeyboard::KeyCode K_ATTACK = cocos2d::EventKeyboard::KeyCode::KEY_K;
static cocos2d::EventKeyboard::KeyCode K_SWITCH_LEFT = cocos2d::EventKeyboard::KeyCode::KEY_I;
static cocos2d::EventKeyboard::KeyCode K_SWITCH_RIGHT = cocos2d::EventKeyboard::KeyCode::KEY_O;
static cocos2d::EventKeyboard::KeyCode K_MENU = cocos2d::EventKeyboard::KeyCode::KEY_ESCAPE;

#define KEYPRESSED(x) (std::find(keysPressed.begin(), keysPressed.end(), x) != keysPressed.end())

extern std::set<cocos2d::EventKeyboard::KeyCode> keysPressed;

// TODO probably want to put in the Input handler code here.
// Ya know, the stuff to load it from file and what not
#endif