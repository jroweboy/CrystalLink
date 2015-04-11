package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.game.system.*;

import static com.badlogic.gdx.Input.*;

public class GameScreen extends ScreenAdapter {
    static final int GAME_READY = 0;
    static final int GAME_RUNNING = 1;
    static final int GAME_PAUSED = 2;
    static final int GAME_LEVEL_END = 3;
    static final int GAME_OVER = 4;

    CrystalLink game;
    OrthographicCamera guiCam;
//    Vector3 touchPoint;
    public World world;
    public PhysicsSystem physics;
    public RenderingSystem renderer;
    public CameraSystem cameraSystem;

//    CollisionSystem.CollisionListener collisionListener;

    Engine engine;

    private int state;

    private String typed = "";

    public GameScreen (CrystalLink game) {
        this.game = game;

        state = GAME_READY;
        guiCam = new OrthographicCamera(320, 480);
        guiCam.position.set(320 / 2, 480 / 2, 0);

        engine = new Engine();
        physics = new PhysicsSystem();
        world = new World(engine, physics);

        engine.addSystem(new InputSystem());
        engine.addSystem(new PlayerSystem(world));
        AISystem pathfinder = new AISystem();
        engine.addSystem(pathfinder);
//        engine.addSystem(new SquirrelSystem());
//        engine.addSystem(new PlatformSystem());
        cameraSystem = new CameraSystem();
        engine.addSystem(cameraSystem);
        engine.addSystem(new BackgroundSystem());
//        engine.addSystem(new GravitySystem());
        engine.addSystem(new MovementSystem());
        engine.addSystem(new BoundsSystem());
        engine.addSystem(new StateSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new CollisionSystem(world));
        engine.addSystem(physics);
        renderer = new RenderingSystem(physics.world);
        engine.addSystem(renderer);
        engine.addSystem(new NetworkSystem(game, engine));
        LevelSystem levelSystem = new LevelSystem();
        engine.addSystem(levelSystem);
        engine.getSystem(BackgroundSystem.class).setCamera(engine.getSystem(RenderingSystem.class).getCamera());
        // maybe make a Level system?
//        engine.getSystem(BackgroundSystem.class).setMap(Assets.currentMap);

        game.assets.addObserver(levelSystem);
        game.assets.addObserver(cameraSystem);
        game.assets.addObserver(renderer);
        game.assets.addObserver(pathfinder);
        world.create();
    }

    public void update (float deltaTime) {
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        engine.update(deltaTime);

        switch (state) {
            case GAME_READY:
                updateReady();
                break;
            case GAME_RUNNING:
                updateRunning(deltaTime);
                break;
            case GAME_PAUSED:
                updatePaused();
                break;
            case GAME_LEVEL_END:
//                updateLevelEnd();
                break;
            case GAME_OVER:
//                updateGameOver();
                break;
        }
    }

    private void updateReady () {
        if (Gdx.input.justTouched()) {
            state = GAME_RUNNING;
            resumeSystems();
        }
    }

    private void updateRunning (float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Keys.P)) {
            if (game.server.isRunning) {
                Gdx.app.log("Server", "stopping server");
                game.server.stopServer();
            } else {
                Gdx.app.log("Server", "starting server");
                game.server.startServer(engine);
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.O)) {
            Gdx.input.getTextInput(new TextInputListener(){
                @Override
                public void input(String text) {
                    typed = text;
                    game.client.joinGame(text, engine);
                    resume();
                }
                @Override
                public void canceled() {
                    typed = "";
                }
            }, "Enter your friend\'s IP address", "", "");
        }
//        if (Gdx.input.justTouched()) {
//            guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
//
//            if (pauseBounds.contains(touchPoint.x, touchPoint.y)) {
////                Assets.playSound(Assets.clickSound);
//                state = GAME_PAUSED;
//                pauseSystems();
//                return;
//            }
//        }
//
//        ApplicationType appType = Gdx.app.getType();

        // should work also with Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)
//        float  = 0.0f;
//
////        if (appType == ApplicationType.Android || appType == ApplicationType.iOS) {
////            moving = Gdx.input.getAccelerometerX();
////        } else {
//        if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
//            moving = 5f;
//        } else if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT) || Gdx.input.isKeyPressed(Keys.A)) {
//            moving = -5f;
//        }
////        }
//
//        if

//        engine.getSystem(PlayerSystem.class).setAccelX(accelX);
//
//        if (world.score != lastScore) {
//            lastScore = world.score;
//            scoreString = "SCORE: " + lastScore;
//        }
//        if (world.state == World.WORLD_STATE_NEXT_LEVEL) {
////            game.setScreen(new WinScreen(game));
//        }
//        if (world.state == World.WORLD_STATE_GAME_OVER) {
//            state = GAME_OVER;
//            if (lastScore >= Settings.highscores[4])
//                scoreString = "NEW HIGHSCORE: " + lastScore;
//            else
//                scoreString = "SCORE: " + lastScore;
//            pauseSystems();
//            Settings.addScore(lastScore);
//            Settings.save();
//        }
    }

    private void updatePaused () {
//        if (Gdx.input.justTouched()) {
//            guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
//
//            if (resumeBounds.contains(touchPoint.x, touchPoint.y)) {
////                Assets.playSound(Assets.clickSound);
//                state = GAME_RUNNING;
//                resumeSystems();
//                return;
//            }
//
//            if (quitBounds.contains(touchPoint.x, touchPoint.y)) {
////                Assets.playSound(Assets.clickSound);
////                game.setScreen(new MainMenuScreen(game));
//                return;
//            }
//        }
    }

//    private void updateLevelEnd () {
//        if (Gdx.input.justTouched()) {
//            engine.removeAllEntities();
//            world = new World(engine);
//            world.score = lastScore;
//            state = GAME_READY;
//        }
//    }

//    private void updateGameOver () {
//        if (Gdx.input.justTouched()) {
////            game.setScreen(new MainMenuScreen(game));
//        }
//    }

    public void drawUI () {
        guiCam.update();
        game.batch.setProjectionMatrix(guiCam.combined);
        game.batch.begin();
        switch (state) {
            case GAME_READY:
//                presentReady();
                break;
            case GAME_RUNNING:
//                presentRunning();
                break;
            case GAME_PAUSED:
//                presentPaused();
                break;
            case GAME_LEVEL_END:
//                presentLevelEnd();
                break;
            case GAME_OVER:
//                presentGameOver();
                break;
        }
        game.batch.end();
    }

//    private void presentReady () {
////        game.batch.draw(Assets.ready, 160 - 192 / 2, 240 - 32 / 2, 192, 32);
//    }
//
//    private void presentRunning () {
////        game.batch.draw(Assets.pause, 320 - 64, 480 - 64, 64, 64);
////        Assets.font.draw(game.batcher, scoreString, 16, 480 - 20);
//    }
//
//    private void presentPaused () {
////        game.batcher.draw(Assets.pauseMenu, 160 - 192 / 2, 240 - 96 / 2, 192, 96);
////        Assets.font.draw(game.batcher, scoreString, 16, 480 - 20);
//    }
//
//    private void presentLevelEnd () {
////        String topText = "the princess is ...";
////        String bottomText = "in another castle!";
////        float topWidth = Assets.font.getBounds(topText).width;
////        float bottomWidth = Assets.font.getBounds(bottomText).width;
////        Assets.font.draw(game.batcher, topText, 160 - topWidth / 2, 480 - 40);
////        Assets.font.draw(game.batcher, bottomText, 160 - bottomWidth / 2, 40);
//    }
//
//    private void presentGameOver () {
////        game.batcher.draw(Assets.gameOver, 160 - 160 / 2, 240 - 96 / 2, 160, 96);
////        float scoreWidth = Assets.font.getBounds(scoreString).width;
////        Assets.font.draw(game.batcher, scoreString, 160 - scoreWidth / 2, 480 - 20);
//    }

    private void pauseSystems() {
//        for (EntitySystem e : (EntitySystem[]) engine.getSystems().toArray(EntitySystem.class)) {
//            e.setProcessing(false);
//        }
        // don't let the network stop! (not sure if this system does anything right now anyway
//        engine.getSystem(NetworkSystem.class).setProcessing(true);

//        engine.getSystem(PlayerSystem.class).setProcessing(false);
//        engine.getSystem(MovementSystem.class).setProcessing(false);
//        engine.getSystem(BoundsSystem.class).setProcessing(false);
//        engine.getSystem(StateSystem.class).setProcessing(false);
//        engine.getSystem(AnimationSystem.class).setProcessing(false);
//        engine.getSystem(CollisionSystem.class).setProcessing(false);
    }

    private void resumeSystems() {
        for (EntitySystem e : (EntitySystem[]) engine.getSystems().toArray(EntitySystem.class)) {
            e.setProcessing(true);
        }
//        engine.getSystem(PlayerSystem.class).setProcessing(true);
//        engine.getSystem(MovementSystem.class).setProcessing(true);
//        engine.getSystem(BoundsSystem.class).setProcessing(true);
//        engine.getSystem(StateSystem.class).setProcessing(true);
//        engine.getSystem(AnimationSystem.class).setProcessing(true);
//        engine.getSystem(CollisionSystem.class).setProcessing(true);
    }

    @Override
    public void render (float delta) {
        update(delta);
        drawUI();
    }

    @Override
    public void pause () {
        if (state == GAME_RUNNING) {
            state = GAME_PAUSED;
            pauseSystems();
        }
    }

    public void resume() {
        if (state == GAME_PAUSED) {
            state = GAME_RUNNING;
            resumeSystems();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        renderer.resize(width, height);
        cameraSystem.resize(width, height);
    }
}