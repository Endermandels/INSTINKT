package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;

public class AnimationManager {

    private ArrayList<ArrayList<TextureRegion>> frames;
    private ArrayList<ArrayList<TextureRegion>> flippedFrames;
    private ArrayList<Integer> framesPerRow;
    private HashMap<String, Integer> animStates;
    private float speed;

    private int currentFrame;
    private int currentRow;
    private int nextRow;
    private float timer;
    private boolean oneShot;
    private boolean finishedAnimation;

    public AnimationManager(Texture spriteSheet, ArrayList<Integer> framesPerRow, HashMap<String, Integer> animSates
            , float speed, int frameWidth, int frameHeight) {
        this(spriteSheet, framesPerRow, animSates, speed, frameWidth, frameHeight, false);
    }

    public AnimationManager(Texture spriteSheet, ArrayList<Integer> framesPerRow, HashMap<String, Integer> animSates
            , float speed, int frameWidth, int frameHeight, boolean vertical) {
        this.framesPerRow = framesPerRow;
        this.animStates = animSates;
        this.speed = speed;

        currentFrame = 0;
        currentRow = 0;
        nextRow = -1;
        timer = 0;
        oneShot = false;
        finishedAnimation = false;

        frames = loadFrames(spriteSheet, frameWidth, frameHeight, false, vertical);
        flippedFrames = loadFrames(spriteSheet, frameWidth, frameHeight, true, vertical);
    }

    /**
     * Initialize frames ArrayList with frames from given sprite sheet.
     * Uses (does not modify) framesPerRow.
     *
     * When vertical is true, the frames are all vertically stacked.
     * Each "row" in frames per row is the number of frames down.
     *
     * @param ss sprite sheet
     * @param frameWidth width of every frame in ss
     * @param frameHeight height of every frame in ss
     *
     * @return 2D ArrayList of frames for every row
     */
    private ArrayList<ArrayList<TextureRegion>> loadFrames(Texture ss, int frameWidth, int frameHeight, boolean flipped
            , boolean vertical) {
        ArrayList<ArrayList<TextureRegion>> frames = new ArrayList<ArrayList<TextureRegion>>(framesPerRow.size());
        int numCols = 0;
        for (int row = 0; row < framesPerRow.size(); row++) {
            ArrayList<TextureRegion> framesRow = new ArrayList<TextureRegion>(framesPerRow.get(row));
            for (int col = 0; col < framesPerRow.get(row); col++) {
                if (!vertical) {
                    framesRow.add(new TextureRegion(ss
                            , col * frameWidth, row * frameHeight
                            , frameWidth, frameHeight));
                } else {
                    framesRow.add(new TextureRegion(ss
                            , 0, numCols * frameHeight + col * frameHeight
                            , frameWidth, frameHeight));
                }
                framesRow.get(col).flip(flipped, false);
            }
            frames.add(framesRow);
            numCols += framesPerRow.get(row);
        }
        return frames;
    }

    /**
     * Cycle through the animation frames every speed seconds.
     */
    public void update() {
        float time = Gdx.graphics.getDeltaTime();
        if (time > 1f) time = 1f / 60f;
        timer += time;

        if (timer > speed) {
            currentFrame += 1;
            timer = 0;
            if (currentFrame >= framesPerRow.get(currentRow)) {
                if (oneShot) {
                    if (nextRow < 0) {
                        currentFrame -= 1;
                        finishedAnimation = true;
                    } else {
                        switchAnimState(nextRow);
                    }
                }
                else currentFrame = 0;
            }
        }
    }

    public void switchAnimState(String state) {
        if (state.compareTo(getCurrentAnimState()) != 0) {
            currentRow = animStates.get(state);
            currentFrame = 0;
            finishedAnimation = false;
            oneShot = false;
            nextRow = -1;
        }
    }

    /**
     * Animate state, then immediately switch to nextState
     */
    public void switchAnimState(String state, String nextState) {
        if (state.compareTo(getCurrentAnimState()) != 0) {
            currentRow = animStates.get(state);
            nextRow = animStates.get(nextState);
            currentFrame = 0;
            finishedAnimation = false;
            oneShot = true;
        }
    }

    public void switchAnimState(int idx) {
        if (currentRow != idx) {
            currentRow = idx;
            currentFrame = 0;
            finishedAnimation = false;
            oneShot = false;
            nextRow = -1;
        }
    }

    public void setOneShot(boolean oneShot) {
        this.oneShot = oneShot;
    }

    public TextureRegion getCurrentImage(boolean flipped) {
        if (flipped) return flippedFrames.get(currentRow).get(currentFrame);
        return frames.get(currentRow).get(currentFrame);
    }

    public String getCurrentAnimState() {
        for (HashMap.Entry<String, Integer> entry : animStates.entrySet()) {
            if (entry.getValue().equals(currentRow)) {
                return entry.getKey(); // Return the animation state name
            }
        }
        return null;
    }

    public void restartOneShotAnimation() {
        oneShot = true;
        nextRow = -1;
        currentFrame = 0;
        finishedAnimation = false;
    }

    public boolean isFinished() {
        return finishedAnimation;
    }
}
