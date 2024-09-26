package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;

public class AnimationManager {

    private ArrayList<ArrayList<TextureRegion>> frames;
    private ArrayList<Integer> framesPerRow;
    private HashMap<String, Integer> animStates;
    private float speed;

    private int currentFrame;
    private int currentRow;

    public AnimationManager(Texture spriteSheet, ArrayList<Integer> framesPerRow, HashMap<String, Integer> animSates
            , float speed, int frameWidth, int frameHeight) {
        this.framesPerRow = framesPerRow;
        this.animStates = animSates;
        this.speed = speed;

        currentFrame = 0;
        currentRow = 0;

        frames = loadFrames(spriteSheet, frameWidth, frameHeight);
    }

    /**
     * Initialize frames ArrayList with frames from given sprite sheet.
     * Uses (does not modify) framesPerRow
     *
     * @param ss sprite sheet
     * @param frameWidth width of every frame in ss
     * @param frameHeight height of every frame in ss
     *
     * @return 2D ArrayList of frames for every row
     */
    private ArrayList<ArrayList<TextureRegion>> loadFrames(Texture ss, int frameWidth, int frameHeight) {
        ArrayList<ArrayList<TextureRegion>> frames = new ArrayList<ArrayList<TextureRegion>>(framesPerRow.size());
        for (int row = 0; row < framesPerRow.size(); row++) {
            ArrayList<TextureRegion> framesRow = new ArrayList<TextureRegion>(framesPerRow.get(row));
            for (int col = 0; col < framesPerRow.get(row); col++) {
                framesRow.add(new TextureRegion(ss
                        , col * frameWidth, row * frameHeight
                        , frameWidth, frameHeight));
            }
            frames.add(framesRow);
        }
        return frames;
    }

    public TextureRegion getCurrentImage() {
        return frames.get(currentRow).get(currentFrame);
    }
}
