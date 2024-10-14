package wsuv.instinkt;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;

public class BerryManager {

    private int berriesCollected;
    private ArrayList<BerryBush> bushes;

    public BerryManager(Game game, ArrayList<GameObject> gameObjects) {
        berriesCollected = 0;
        bushes = new ArrayList<>();

        ArrayList<Integer[]> bushSpriteLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{0,2},
                new Integer[]{0,2}
        ));

        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-1, 0, bushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(0));
    }
}

class BerryBush extends GameObject {
    private enum State { UNPLANTED, PLANTED, GROWN }

    private TextureRegion seedlingTexture;
    private TextureRegion bushTexture;

    private State state;

    public BerryBush(Game game, int row, int col, ArrayList<Integer[]> ssTiles, String fileName, int priority) {
        super(game, row, col, ssTiles, fileName, priority);

        bushTexture = super.getImg();

        ArrayList<Integer[]> seedlingSpriteLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{0,1},
                new Integer[]{0,1}
        ));

        seedlingTexture = GameObject.getImgRegion(game, seedlingSpriteLocation, Game.RSC_SS_BERRIES_IMG);

        state = State.GROWN;
    }

    public void grow() {
        state = State.GROWN;
    }

    @Override
    public TextureRegion getImg() {
        if (state == State.PLANTED)
            return seedlingTexture;
        if (state == State.GROWN)
            return bushTexture;
        return null;
    }
}
