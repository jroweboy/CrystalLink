package com.mygdx.game.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.Assets;
import com.mygdx.game.OrthogonalTiledMapRendererWithSprites;
import com.mygdx.game.World;
import com.mygdx.game.component.BackgroundComponent;
import com.mygdx.game.component.CollisionComponent;
import com.mygdx.game.component.TextureComponent;
import com.mygdx.game.component.TransformComponent;

import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;



public class RenderingSystem extends IteratingSystem implements Observer {
    private SpriteBatch batch;
    private Array<Entity> renderQueue;
    private Comparator<Entity> comparator;
    private OrthographicCamera cam;
    private ScreenViewport viewport;
    private OrthogonalTiledMapRendererWithSprites tiledMapRenderer;
    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private com.badlogic.gdx.physics.box2d.World world;
    public static float unitScale = 1 / 8f;

    //our constants...
    public static final float DEFAULT_LIGHT_Z = .1f;
    public static final float AMBIENT_INTENSITY = .4f;
    public static final float LIGHT_INTENSITY = 3f;

    public static Vector3 LIGHT_POS = new Vector3(.5f, .5f,DEFAULT_LIGHT_Z);
    public static Vector3 LIGHT_POS2 = new Vector3(.7f, .1f,DEFAULT_LIGHT_Z);
    public static Vector3 LIGHT_POS3 = new Vector3(.0f, .9f,DEFAULT_LIGHT_Z);

    //Light RGB and intensity (alpha)
    public static final Vector3 LIGHT_COLOR = new Vector3(1f, 0.8f, 0.6f);
    public static final Vector3 LIGHT_COLOR2 = new Vector3(1f, 0f, 0f);
    public static final Vector3 LIGHT_COLOR3 = new Vector3(0f, 1f, 0f);

    //Ambient RGB and intensity (alpha)
    public static final Vector3 AMBIENT_COLOR = new Vector3(0.5f, .5f, .5f);

    //Attenuation coefficients for light falloff
    public static final Vector3 FALLOFF = new Vector3(.4f, 4f, 20);

    private FPSLogger fpsLogger = new FPSLogger();

    private ComponentMapper<TextureComponent> textureM;
    private ComponentMapper<TransformComponent> transformM;

    private ShaderProgram shader;

    public RenderingSystem(com.badlogic.gdx.physics.box2d.World world) {
        super(Family.getFor(ComponentType.getBitsFor(TransformComponent.class),
                ComponentType.getBitsFor(BackgroundComponent.class, TextureComponent.class, CollisionComponent.class), new Bits()));
//        super(Family.getFor(TransformComponent.class, TextureComponent.class));
//        this.shader = shader;

        shader = setupShader();
        batch = new SpriteBatch(1000, shader);
        batch.setShader(shader);
        textureM = ComponentMapper.getFor(TextureComponent.class);
        transformM = ComponentMapper.getFor(TransformComponent.class);
//        boundsM = ComponentMapper.getFor(CollisionComponent.class);

        renderQueue = new Array<Entity>();

        comparator = new Comparator<Entity>() {
            @Override
            public int compare(Entity entityA, Entity entityB) {
                return (int)Math.signum(transformM.get(entityB).c.pos.y -
                        transformM.get(entityA).c.pos.y);
            }
        };

        this.batch = batch;
        this.world = world;

        onMapLoad(Assets.get().currentMap);
    }

    private void onMapLoad(TiledMap map) {
        cam = new OrthographicCamera();
        viewport = new ScreenViewport(cam);
        viewport.setUnitsPerPixel(unitScale);
        cam.setToOrtho(false, World.WIDTH, World.HEIGHT);
//        new SpriteBatch(1000, setupShader())
        tiledMapRenderer = new OrthogonalTiledMapRendererWithSprites(map, unitScale, batch);
    }

    @Override
    public void update(float deltaTime) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.update(deltaTime);
        renderQueue.sort(comparator);

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        tiledMapRenderer.setView(cam);
        tiledMapRenderer.renderBack();
        batch.begin();

        for (Entity entity : renderQueue) {
            TextureComponent tex = textureM.get(entity);
            TransformComponent t = transformM.get(entity);
            if (tex != null) {
                float width = tex.region.getRegionWidth();
                float height = tex.region.getRegionHeight();
                float originX = width * 0.5f;
                float originY = height * 0.5f;

                //send a Vector4f to GLSL
                LIGHT_POS = new Vector3();
                LIGHT_POS.x = Gdx.input.getX();
                LIGHT_POS.y = viewport.getScreenHeight()- Gdx.input.getY();
                LIGHT_POS.z = DEFAULT_LIGHT_Z;
                LIGHT_POS = scaleScreenCoord(LIGHT_POS);
                shader.setUniformf("LightPos", LIGHT_POS);
                shader.setUniformf("LightPos2", LIGHT_POS2);
                shader.setUniformf("LightPos3", LIGHT_POS3);

                tex.normal.getTexture().bind(1);
                tex.region.getTexture().bind(0);

                batch.draw(tex.region,
                        t.c.pos.x - originX, t.c.pos.y - originY,
                        originX, originY,
                        width, height,
                        t.c.scale.x * unitScale, t.c.scale.y * unitScale,
                        MathUtils.radiansToDegrees * t.c.rotation);
            }
        }

        batch.end();

        batch.begin();
        tiledMapRenderer.renderFront();
        batch.end();
//        debugRenderer.render(world, cam.combined);
        renderQueue.clear();
//        fpsLogger.log();
    }

    private Vector3 scaleScreenCoord(Vector3 position) {
        float x = position.x / (float)viewport.getScreenWidth();
        float y = position.y / (float)viewport.getScreenHeight();

        return new Vector3(x, y, position.z);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        BackgroundComponent bc = entity.getComponent(BackgroundComponent.class);
        if (bc != null && bc.tiledmap != null) {
            if (bc.tiledmap != tiledMapRenderer.getMap()) {
                tiledMapRenderer.setMap(bc.tiledmap);
            }
        } else {
            renderQueue.add(entity);
        }
    }

    public OrthographicCamera getCamera() {
        return cam;
    }

    public void resize(int w, int h) {
        viewport.update(w, h , true);
        shader.begin();
        shader.setUniformf("Resolution", w, h);
        shader.end();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof TiledMap) {
            onMapLoad((TiledMap) arg);
        }
    }

    public static final String VERT =
            "attribute vec4 "+ ShaderProgram.POSITION_ATTRIBUTE+";\n" +
                    "attribute vec4 "+ShaderProgram.COLOR_ATTRIBUTE+";\n" +
                    "attribute vec2 "+ShaderProgram.TEXCOORD_ATTRIBUTE+"0;\n" +

                    "uniform mat4 u_projTrans;\n" +
                    " \n" +
                    "varying vec4 vColor;\n" +
                    "varying vec2 vTexCoord;\n" +

                    "void main() {\n" +
                    "	vColor = "+ShaderProgram.COLOR_ATTRIBUTE+";\n" +
                    "	vTexCoord = "+ShaderProgram.TEXCOORD_ATTRIBUTE+"0;\n" +
                    "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                    "}";

    //no changes except for LOWP for color values
    //we would store this in a file for increased readability
    public static final String FRAG =
            //GL ES specific stuff
            "#ifdef GL_ES\n" //
                    + "#define LOWP lowp\n" //
                    + "precision mediump float;\n" //
                    + "#else\n" //
                    + "#define LOWP \n" //
                    + "#endif\n" + //
                    "//attributes from vertex shader\n" +
                    "varying LOWP vec4 vColor;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "\n" +
                    "//our texture samplers\n" +
                    "uniform sampler2D u_texture;   //diffuse map\n" +
                    "uniform sampler2D u_normals;   //normal map\n" +
                    "\n" +
                    "//values used for shading algorithm...\n" +
                    "uniform vec2 Resolution;         //resolution of screen\n" +
                    "uniform vec3 LightPos;           //light position, normalized\n" +
                    "uniform vec3 LightPos2;\n" +
                    "uniform vec3 LightPos3;\n" +
                    "uniform LOWP vec4 LightColor;    //light RGBA -- alpha is intensity\n" +
                    "uniform LOWP vec4 LightColor2;" +
                    "uniform LOWP vec4 LightColor3;" +
                    "uniform LOWP vec4 AmbientColor;  //ambient RGBA -- alpha is intensity \n" +
                    "uniform vec3 Falloff;            //attenuation coefficients\n" +
                    "\n" +
                    "vec3 calculateLight(vec3 posLight, vec4 colorLight, vec4 DiffuseColor) {\n" +
                    "	//RGB of our normal map\n" +
                    "	vec3 NormalMap = texture2D(u_normals, vTexCoord).rgb;\n" +
                    "	\n" +
                    "	//The delta position of light\n" +
                    "	vec3 LightDir = vec3(posLight.xy - (gl_FragCoord.xy / Resolution.xy), posLight.z);\n" +
                    "	\n" +
                    "	//Correct for aspect ratio\n" +
                    "	LightDir.x *= Resolution.x / Resolution.y;\n" +
                    "	\n" +
                    "	//Determine distance (used for attenuation) BEFORE we normalize our LightDir\n" +
                    "	float D = length(LightDir);\n" +
                    "	\n" +
                    "	//normalize our vectors\n" +
                    "	vec3 N = normalize(NormalMap * 2.0 - 1.0);\n" +
                    "	vec3 L = normalize(LightDir);\n" +
                    "	\n" +
                    "	//Pre-multiply light color with intensity\n" +
                    "	//Then perform \"N dot L\" to determine our diffuse term\n" +
                    "	vec3 Diffuse = (colorLight.rgb * colorLight.a) * max(dot(N, L), 0.0);\n" +
                    "\n" +
                    "	//pre-multiply ambient color with intensity\n" +
                    "	vec3 Ambient = AmbientColor.rgb * AmbientColor.a;\n" +
                    "	\n" +
                    "	//calculate attenuation\n" +
                    "	float Attenuation = 1.0 / ( Falloff.x + (Falloff.y*D) + (Falloff.z*D*D) );\n" +
                    "	\n" +
                    "	//the calculation which brings it all together\n" +
                    "	vec3 Intensity = Ambient + Diffuse * Attenuation;\n" +
                    "	return DiffuseColor.rgb * Intensity;\n" +
                    "}\n" +
                    "\n" +
                    "void main() {\n" +
                    "   //sum of all of the light in the world\n" +
                    "   vec3 sumOfLight = vec3(0.0);\n" +
                    "	//RGBA of our diffuse color\n" +
                    "	vec4 DiffuseColor = texture2D(u_texture, vTexCoord);\n" +
                    "   sumOfLight += calculateLight(LightPos, LightColor, DiffuseColor);\n" +
                    "   sumOfLight += calculateLight(LightPos2, LightColor2, DiffuseColor);\n" +
                    "   sumOfLight += calculateLight(LightPos3, LightColor3, DiffuseColor);\n" +
                    "	gl_FragColor = vec4(sumOfLight, DiffuseColor.a);\n" +
                    "}\n" +
                    "\n";

    private ShaderProgram setupShader() {
        ShaderProgram.pedantic = false;
        ShaderProgram shader = new ShaderProgram(VERT, FRAG);
        //ensure it compiled
        if (!shader.isCompiled())
            throw new GdxRuntimeException("Could not compile shader: "+shader.getLog());
        //print any warnings
        if (shader.getLog().length()!=0)
            System.out.println(shader.getLog());

        shader.begin();

        //our normal map
        shader.setUniformi("u_normals", 1); //GL_TEXTURE1

        //light/ambient colors
        //LibGDX doesn't have Vector4 class at the moment, so we pass them individually...
        shader.setUniformf("LightColor", LIGHT_COLOR.x, LIGHT_COLOR.y, LIGHT_COLOR.z, LIGHT_INTENSITY);
        shader.setUniformf("LightColor2", LIGHT_COLOR2.x, LIGHT_COLOR2.y, LIGHT_COLOR2.z, LIGHT_INTENSITY);
        shader.setUniformf("LightColor3", LIGHT_COLOR3.x, LIGHT_COLOR3.y, LIGHT_COLOR3.z, LIGHT_INTENSITY);
        shader.setUniformf("AmbientColor", AMBIENT_COLOR.x, AMBIENT_COLOR.y, AMBIENT_COLOR.z, AMBIENT_INTENSITY);
        shader.setUniformf("Falloff", FALLOFF);

        //LibGDX likes us to end the shader program
        shader.end();

        return shader;
    } //end setupShader
}