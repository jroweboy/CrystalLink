#ifndef __HUDLAYER_H__
#define __HUDLAYER_H__

#pragma warnings(push, 0)
#pragma GCC system_header
#include "cocos2d.h"
#pragma GCC system_header
#include "CocosGUI.h"
#pragma GCC system_header
#include "cocos-ext.h"
#pragma warning(pop)
#pragma warnings(pop)

using namespace cocos2d;

class HudLayer : public cocos2d::CCLayer
{
private:
    Label* _label;
    
public:
    // Method 'init' in cocos2d-x returns bool, instead of 'id' in cocos2d-iphone (an object pointer)
    virtual bool init();
    
    // there's no 'id' in cpp, so we recommend to return the class instance pointer
    static Scene* scene();
    
    // a selector callback
    void menuCloseCallback(Ref* pSender);
    
    // preprocessor macro for "static create()" constructor ( node() deprecated )
    CREATE_FUNC(HudLayer);
    
    void numCollectedChanged (int numCollected);
};

#endif // __HUDLAYER_H__