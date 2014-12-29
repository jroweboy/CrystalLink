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

    public Vector2 position;
    private MovingActorState movementState;
    private TextureAtlas spriteSheet;
    private Animation walk_d;
    private Animation walk_l;
    private Animation walk_u;
    private Animation walk_r;

    // animation specific vars
//    private final int ANIMATION_LENGTH = 8;
    private float frameLength = 1.0f / 16;
    private float animDuration = 0;
    private float walkSpeed = 0.1f;
    private float runSpeedMultiplier = 2.0f;
    //    private int currentFrame;
    private TextureRegion currentFrame;

    public Player(CrystalLink game, Vector2 position) {
        //new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getWidth() / 2);
        this.position = position;
//        movementState = new MovingActorState();
//        spriteSheet = game.assetManager.get("soldier.txt");
//        walk_d = new Animation(frameLength, spriteSheet.createSprites("soldier_d"));
//        walk_u = new Animation(frameLength, spriteSheet.createSprites("soldier_u"));
//        walk_l = new Animation(frameLength, spriteSheet.createSprites("soldier_l"));
//        walk_r = new Animation(frameLength, spriteSheet.createSprites("soldier_r"));
        setCurrentFrame(Direction.DOWN, 0);
    }

    private void setCurrentFrame(Direction d, float duration) {
        if (d == Direction.DOWN) {
            currentFrame = walk_d.getKeyFrame(duration, true);
        } else if (d == Direction.UP) {
            currentFrame = walk_u.getKeyFrame(duration, true);
        } else if (d == Direction.RIGHT) {
            currentFrame = walk_r.getKeyFrame(duration, true);
        } else if (d == Direction.LEFT) {
            currentFrame = walk_l.getKeyFrame(duration, true);

        }
    }
    @Override
    public void act(float dt) {
        processMovement(dt);
    }

    @Override
    public void draw(Batch batch, float alpha) {
//        batch.draw(walk_d.get(currentFrame).getTexture(), position.x, position.y);
//        currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        // TODO: If we reach the edge of the screen, then the camera should snap to the wall
        // and somehow the player should have free walking from here on out
//        batch.draw(currentFrame, position.x, position.y);
        batch.draw(currentFrame, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    // The following 3 methods are for knowing which sprite to draw
    public Direction getXFacing() {
        return movementState.getXDirection();
    }

    public Direction getYFacing() {
        return movementState.getYDirection();
    }

    public MoveAction getMoveState() {
        return movementState.getMoveAction();
    }

    private void processMovement(float dt) {
        // TODO handle collisions
        animDuration += dt;
        MoveAction moveAction = movementState.getMoveAction();
        Direction xDirection = movementState.getXDirection();
        Direction yDirection = movementState.getYDirection();

        if (moveAction == MoveAction.IDLE) {
            // stop the animation and return it to idle state
            setCurrentFrame(movementState.getFacingDirection(), 0);
            return;
        }

        float dx = 0;
        float dy = 0;

        if (xDirection == XDirection.LEFT) {
            dx = -walkSpeed;
            movementState.setFacingDirection(XDirection.LEFT);
            setCurrentFrame(Direction.LEFT, animDuration);
        } else if (xDirection == XDirection.RIGHT) {
            dx = walkSpeed;
            movementState.setFacingDirection(XDirection.RIGHT);
            setCurrentFrame(Direction.RIGHT, animDuration);
        }

        if (yDirection == YDirection.UP) {
            dy = walkSpeed;
            movementState.setFacingDirection(YDirection.UP);
            setCurrentFrame(Direction.UP, animDuration);
        } else if (yDirection == YDirection.DOWN) {
            dy = -walkSpeed;
            movementState.setFacingDirection(YDirection.DOWN);
            setCurrentFrame(Direction.DOWN, animDuration);
        }

        if (moveAction == MoveAction.RUN) {
            dx *= runSpeedMultiplier;
            dy *= runSpeedMultiplier;
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