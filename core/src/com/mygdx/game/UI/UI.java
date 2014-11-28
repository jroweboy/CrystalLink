package com.mygdx.game.UI;

/**
 * Created by robert on 11/28/14.
 */
public class UI {

    UIState state;

    public UI() {
        state = UIState.MENU;
    }

    public UIState getState() {
        return state;
    }


}
