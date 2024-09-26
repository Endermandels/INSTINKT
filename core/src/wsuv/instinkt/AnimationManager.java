package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.Dictionary;

public class AnimationManager {

    private Texture ss; // sprite sheet
    private ArrayList<Integer> framesPerRow;
    private Dictionary<String, Integer> animStates;
    private float speed;
    private int numRows, numCols;

    public AnimationManager(Texture spriteSheet, ArrayList<Integer> framesPerRow, Dictionary<String, Integer> animSates
            , float speed, int numRows, int numCols) {
        this.ss = spriteSheet;
        this.framesPerRow = framesPerRow;
        this.animStates = animSates;
        this.speed = speed;
        this.numRows = numRows;
        this.numCols = numCols;
    }
}
