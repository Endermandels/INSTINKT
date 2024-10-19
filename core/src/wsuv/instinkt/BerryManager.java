package wsuv.instinkt;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;

public class BerryManager {

    private Game game;

    private final int BUSH_PRICE = 2;

    private final int START_BERRY_COUNT = 2;
    private int berriesCollected;

    private final int START_BUSH_BOUND = 5;
    private int bushBound;

    private ArrayList<BerryBush> bushes;

    private Sound plantSeed;

    public BerryManager(Game game, ArrayList<GameObject> gameObjects) {
        this.game = game;

        berriesCollected = START_BERRY_COUNT;
        bushBound = START_BUSH_BOUND;

        bushes = new ArrayList<>();
        plantSeed = game.am.get(Game.RSC_SEED_SFX);

        ArrayList<Integer[]> blueBushSpriteLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{0,2},
                new Integer[]{0,2}
        ));
        ArrayList<Integer[]> pinkBushSpriteLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{0,3},
                new Integer[]{0,3}
        ));

        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-1, 0, blueBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(0));
        bushes.get(0).grow();
        bushes.get(0).grow();
        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-1, 1, pinkBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(1));
        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-1, 2, blueBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(2));
        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-1, 3, blueBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(3));
        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-1, 4, pinkBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(4));

        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-2, 0, pinkBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(5));
        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-2, 1, blueBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(6));
        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-2, 2, blueBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(7));
        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-2, 3, pinkBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(8));
        bushes.add(new BerryBush(game, PlayScreen.TILE_ROWS-2, 4, pinkBushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
        gameObjects.add(bushes.get(9));
    }

    public void plantNewBerryBush() {
        if (berriesCollected < BUSH_PRICE) return;
        for (BerryBush bb : bushes) {
            if (bb.state == BerryBush.State.UNPLANTED) {
                bb.grow();
                berriesCollected-=BUSH_PRICE;
                plantSeed.play(0.05f);
                break;
            }
        }
    }

    public void startOfCooldown() {
        for (BerryBush bb : bushes) {
            if (bb.state == BerryBush.State.PLANTED) bb.grow();
            else if (bb.state == BerryBush.State.GROWN) {
                berriesCollected += game.random.nextInt(2 + bushBound/START_BUSH_BOUND,bushBound);
            }
        }
    }

    public void setBushBound(int bushBound) {
        this.bushBound = bushBound;
    }

    public int getBushBound() {
        return bushBound;
    }

    public int getBerriesCollected() {
        return berriesCollected;
    }

    public int setBerriesCollected(int berriesCollected) {
        this.berriesCollected = Math.max(0,berriesCollected);
        return this.berriesCollected;
    }

    public void reset() {
        berriesCollected = START_BERRY_COUNT;
        bushBound = START_BUSH_BOUND;
        boolean first = true;
        for (BerryBush bb : bushes) {
            if (first) {
                first = false;
                continue;
            }
            bb.resetState();
        }
    }

}

class BerryBush extends GameObject {
    public enum State { UNPLANTED, PLANTED, GROWN }

    private TextureRegion seedlingTexture;
    private TextureRegion bushTexture;

    public State state;

    public BerryBush(Game game, int row, int col, ArrayList<Integer[]> ssTiles, String fileName, int priority) {
        super(game, row, col, ssTiles, fileName, priority);

        bushTexture = super.getImg();

        ArrayList<Integer[]> seedlingSpriteLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{0,1},
                new Integer[]{0,1}
        ));

        seedlingTexture = GameObject.getImgRegion(game, seedlingSpriteLocation, Game.RSC_SS_BERRIES_IMG);

        state = State.UNPLANTED;
    }

    public void grow() {
        if (state == State.UNPLANTED) state = State.PLANTED;
        else state = State.GROWN;
    }

    public void resetState() {
        state = State.UNPLANTED;
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
