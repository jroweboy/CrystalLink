package com.mygdx.game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.system.RenderingSystem;

import java.util.Map;

public class OrthogonalTiledMapRendererWithSprites extends OrthogonalTiledMapRenderer {
    private int drawSpritesAfterLayer;
    private static Texture square;

    public OrthogonalTiledMapRendererWithSprites(TiledMap map, float unitScale, SpriteBatch b) {
        super(map, unitScale, b);
        setMap(map);
        if (square == null) {
            Pixmap p = new Pixmap(2048, 2048, Pixmap.Format.RGBA8888);
            p.setColor(.33f, .33f, .33f, .5f);
            p.fill();
            square = new Texture(p);
            p.dispose();
        }
    }

    @Override
    public void setMap(TiledMap nmap){
        super.setMap(nmap);
        if (nmap != null) {
            int iter = 1;
            for (MapLayer layer : nmap.getLayers()) {
                if (layer.getName().equals("Sprites")) {
                    drawSpritesAfterLayer = iter;
                    break;
                }
                ++iter;
            }
        }
    }

    public void renderBack() {
        for (int i=0; i<drawSpritesAfterLayer; ++i) {
            MapLayer layer = map.getLayers().get(i);
            if (layer.isVisible()) {
                if (layer instanceof TiledMapTileLayer) {
                    renderTileLayer((TiledMapTileLayer)layer);
                } else {
                    for (MapObject object : layer.getObjects()) {
                        renderObject(object);
                    }
                }
            }
        }
    }
    public void renderFront() {
        for (int i=drawSpritesAfterLayer; i<map.getLayers().getCount(); ++i) {
            MapLayer layer = map.getLayers().get(i);
            if (layer.isVisible()) {
                if (layer instanceof TiledMapTileLayer) {
                    renderTileLayer((TiledMapTileLayer)layer);
                } else {
                    boolean debug_draw_walls = true;
                    if (debug_draw_walls && layer.getName().equals("Collisions")) {
                        for (MapObject object : layer.getObjects()) {
                            renderObject(object);
                        }
                    }
                }
            }
        }
    }
    @Override
    public void renderObject(MapObject obj) {
        if (obj instanceof RectangleMapObject) {
            Rectangle o = ((RectangleMapObject) obj).getRectangle();
            TextureRegion reg = new TextureRegion(square, 0, 0,
                    o.width, o.height);
//            spriteBatch.draw(square,0,0);
            spriteBatch.draw(square, o.x * RenderingSystem.unitScale, o.y* RenderingSystem.unitScale, o.width* RenderingSystem.unitScale, o.height* RenderingSystem.unitScale);
        }
//        Gdx.app.log("test", "keys: " + obj.getProperties().getKeys() + " values: "+ obj.getProperties().getValues());
    }

}