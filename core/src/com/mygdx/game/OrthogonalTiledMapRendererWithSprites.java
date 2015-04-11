package com.mygdx.game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class OrthogonalTiledMapRendererWithSprites extends OrthogonalTiledMapRenderer {
    private int drawSpritesAfterLayer;
    private static Texture square;
    private SpriteBatch batch;

//    public OrthogonalTiledMapRendererWithSprites(TiledMap map, float unitScale) {
//        super(map, unitScale);
//        setMap(map);
//    }

    public OrthogonalTiledMapRendererWithSprites(TiledMap map, float unitScale, SpriteBatch batch) {
        super(map, unitScale, batch);
        setMap(map);
        this.batch = batch;

//        if (square == null) {
//            Pixmap p = new Pixmap(2048, 2048, Pixmap.Format.RGBA8888);
//            p.setColor(.33f, .33f, .33f, .5f);
//            p.fill();
//            square = new Texture(p);
//            p.dispose();
//        }
    }

    @Override
    public void setMap(TiledMap nmap){
        super.setMap(nmap);

//        int map_width = map.getProperties().get("width", Integer.class);
//        int map_height = map.getProperties().get("height", Integer.class);
//        rockTarget = new FrameBuffer(Pixmap.Format.RGBA8888, map_width, map_height, false);
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
        batch.begin();
        for (int i=0; i<drawSpritesAfterLayer; ++i) {
            MapLayer layer = map.getLayers().get(i);
            if (layer.isVisible()) {
                if (layer instanceof TiledMapTileLayer) {
                   /* Assets.get().mapBackNormals.bind(1);
                    Assets.get().mapBackAmbient.bind(0);*/
                    if (layer.getName().toLowerCase().equals("grass")) {
                        Assets.get().grassNormals.bind(1);
                        Assets.get().grassAmbient.bind(0);
                    }
                    /*if (layer.getName().toLowerCase().equals("rocks")) {
                        Assets.get().cliffNormals.bind(1);
                        Assets.get().cliffAmbient.bind(0);
                    }*/

                    renderTileLayer((TiledMapTileLayer) layer);
//                    else {
//                        Gdx.app.log("OrthoRender", layer.getName().toLowerCase());
//                        renderTileLayer((TiledMapTileLayer)layer);
//                    }
                } else {
                    for (MapObject object : layer.getObjects()) {
                        renderObject(object);
                    }
                }
            }
        }
        batch.end();
    } //end renderBack

    public void renderFront() {
        for (int i=drawSpritesAfterLayer; i<map.getLayers().getCount(); ++i) {
            MapLayer layer = map.getLayers().get(i);
            if (layer.isVisible()) {
                if (layer instanceof TiledMapTileLayer) {
                    if (layer.getName().toLowerCase().equals("trees")) {
                        Assets.get().treesNormals.bind(1);
                        Assets.get().treesAmbient.bind(0);
                    }

                    renderTileLayer((TiledMapTileLayer)layer);
                }
//                else {
//                    boolean debug_draw_walls = false;
//                    if (debug_draw_walls && layer.getName().equals("Collisions")) {
//                        for (MapObject object : layer.getObjects()) {
//                            renderObject(object);
//                        }
//                    }
//                }
            }
        }
    }
    @Override
    public void renderObject(MapObject obj) {
//        if (obj instanceof RectangleMapObject) {
//            Rectangle o = ((RectangleMapObject) obj).getRectangle();
//            TextureRegion reg = new TextureRegion(square, 0, 0,
//                    o.width, o.height);
////            spriteBatch.draw(square,0,0);
//            spriteBatch.draw(square, o.x * RenderingSystem.unitScale, o.y* RenderingSystem.unitScale, o.width* RenderingSystem.unitScale, o.height* RenderingSystem.unitScale);
//        }
//        Gdx.app.log("test", "keys: " + obj.getProperties().getKeys() + " values: "+ obj.getProperties().getValues());
    }

}