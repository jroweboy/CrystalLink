package com.mygdx.game.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mygdx.game.CrystalLink;

/**
 * Created by robert on 11/27/14.
 */
public class Player extends Actor implements MovingActor {

    private MovingActorState movementState;
    public Vector2 position;
    private TextureAtlas spriteSheet;
    private Animation walk_d;
    private Animation walk_l;
    private Animation walk_u;
    private Animation walk_r;

    // animation specific vars
//    private final int ANIMATION_LENGTH = 8;
    private float frameLength = 1.0f / 16;
    private float animDuration = 0;
    private float walkSpeed = 1.0f;
    private float runSpeedMultiplier = 2.0f;
    //    private int currentFrame;
    private TextureRegion currentFrame;

    public Player(CrystalLink game) {
        position = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getWidth() / 2);
        movementState = new MovingActorState();
        spriteSheet = game.assetManager.get("soldier.txt");
        walk_d = new Animation(frameLength, spriteSheet.createSprites("soldier_d"));
        walk_u = new Animation(frameLength, spriteSheet.createSprites("soldier_u"));
        walk_l = new Animation(frameLength, spriteSheet.createSprites("soldier_l"));
        walk_r = new Animation(frameLength, spriteSheet.createSprites("soldier_r"));
    }

    @Override
    public void act(float dt) {
        processMovement(dt);
    }

    @Override
    public void draw(Batch batch, float alpha) {
//        batch.draw(walk_d.get(currentFrame).getTexture(), position.x, position.y);
//        currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, position.x, position.y);
    }

    // The following 3 methods are for knowing which sprite to draw
    public XDirection getXFacing() {
        return movementState.getXDirection();
    }

    public YDirection getYFacing() {
        return movementState.getYDirection();
    }

    public MoveAction getMoveState() {
        return movementState.getMoveAction();
    }

    private void processMovement(float dt) {
        // TODO handle collisions
        animDuration += dt;
        MoveAction moveAction = movementState.getMoveAction();
        XDirection xDirection = movementState.getXDirection();
        YDirection yDirection = movementState.getYDirection();

        if (moveAction == MoveAction.IDLE) {
            if (yDirection == YDirection.UP) {
                currentFrame = walk_u.getKeyFrame(0, true);
            } else if (yDirection == YDirection.DOWN) {
                currentFrame = walk_d.getKeyFrame(0, true);
            } else if (xDirection == XDirection.LEFT) {
                currentFrame = walk_l.getKeyFrame(0, true);
            } else if (xDirection == XDirection.RIGHT) {
                currentFrame = walk_r.getKeyFrame(0, true);
            }
            return;
        }

        float dx = 0;
        float dy = 0;

        if (xDirection == XDirection.LEFT) {
            dx = -walkSpeed;
            currentFrame = walk_l.getKeyFrame(animDuration, true);
        } else if (xDirection == XDirection.RIGHT) {
            dx = walkSpeed;
            currentFrame = walk_r.getKeyFrame(animDuration, true);
        }

        if (yDirection == YDirection.UP) {
            dy = walkSpeed;
            currentFrame = walk_u.getKeyFrame(animDuration, true);
        } else if (yDirection == YDirection.DOWN) {
            dy = -walkSpeed;
            currentFrame = walk_d.getKeyFrame(animDuration, true);
        }

        if (moveAction == MoveAction.RUN) {
            dx *= 2;
            dy *= 2;
        }
        // prevent faster diagonal movement by slowing it down by sqrt(2)
        if (yDirection != YDirection.NONE && xDirection != XDirection.NONE) {
            dx /= 1.41421356237f;
            dy /= 1.41421356237f;
        }

        position.add(dx, dy);
    }

    @Override
    public void beginRun() {
        movementState.beginRun();
    }

    @Override
    public void beginUp() {
        movementState.beginUp();
    }

    @Override
    public void beginDown() {
        movementState.beginDown();
    }

    @Override
    public void beginLeft() {
        movementState.beginLeft();
    }

    @Override
    public void beginRight() {
        movementState.beginRight();
    }

    @Override
    public void endRun() {
        movementState.endRun();
    }

    @Override
    public void endUp() {
        movementState.endUp();
    }

    @Override
    public void endDown() {
        movementState.endDown();
    }

    @Override
    public void endLeft() {
        movementState.endLeft();
    }

    @Override
    public void endRight() {
        movementState.endRight();
    }

}