package com.mygdx.game.component;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureComponent extends Component {
    public TextureRegion region = null;
    public TextureRegion normal = null;
//    public TextureMapObject obj = null;

    private TextureComponent() {}

    public TextureComponent(TextureRegion t, TextureRegion n){
        region = t;
        normal = n;
    }
}