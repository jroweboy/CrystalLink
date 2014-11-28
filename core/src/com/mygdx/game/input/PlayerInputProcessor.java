package com.mygdx.game.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.mygdx.game.actor.Player;

/**
 * Created by robert on 11/27/14.
 */
public class PlayerInputProcessor extends InputAdapter {

    Player player;

    public PlayerInputProcessor(Player player) {
        this.player = player;
    }

    public boolean keyDown(int keycode) {
        boolean ret = false;
        switch(keycode) {
            case Input.Keys.A:
                player.beginLeft();
                ret = true;
                break;
            case Input.Keys.D:
                player.beginRight();
                ret = true;
                break;
            case Input.Keys.W:
                player.beginUp();
                ret = true;
                break;
            case Input.Keys.S:
                player.beginDown();
                ret = true;
                break;
        }
        return ret;
    }

    public boolean keyUp(int keycode) {
        boolean ret = false;
        switch(keycode) {
            case Input.Keys.A:
                player.endLeft();
                ret = true;
                break;
            case Input.Keys.D:
                player.endRight();
                ret = true;
                break;
            case Input.Keys.W:
                player.endUp();
                ret = true;
                break;
            case Input.Keys.S:
                player.endDown();
                ret = true;
                break;
        }
        return ret;
    }

}
