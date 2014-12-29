package com.mygdx.game;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class OrthogonalTiledMapRendererWithSprites extends OrthogonalTiledMapRenderer {

    public OrthogonalTiledMapRendererWithSprites(TiledMap map) {
        super(map);
    }
    public OrthogonalTiledMapRendererWithSprites(TiledMap map, float scale) {
        super(map, scale);
    }

    @Override
    public void renderObject(MapObject object) {
        if(object instanceof TextureMapObject) {
            TextureMapObject textureObj = (TextureMapObject) object;
            spriteBatch.draw(textureObj.getTextureRegion(), textureObj.getX(), textureObj.getY());
        }
    }
}