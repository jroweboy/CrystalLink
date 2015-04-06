package com.mygdx.game.component;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureComponent extends Component {
    public TextureRegion region = null;
//    public TextureMapObject obj = null;

    private TextureComponent() {}

    public TextureComponent(TextureRegion t){
        region = t;
    }
}