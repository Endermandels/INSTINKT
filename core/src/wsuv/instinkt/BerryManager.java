package wsuv.instinkt;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;

public class BerryManager {

    private final int BUSH_PRICE = 2;

    private final int START_BERRY_COUNT = 2;
    private int berriesCollected;

    private ArrayList<BerryBush> bushes;

    public BerryManager(Game game, ArrayList<GameObject> gameObjects) {
        berriesCollected = START_BERRY_COUNT;
        bushes = new ArrayList<>();

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
                break;
            }
        }
    }

    public void startOfCooldown() {
        for (BerryBush bb : bushes) {
            if (bb.state == BerryBush.State.PLANTED) bb.grow();
            else if (bb.state == BerryBush.State.GROWN) berriesCollected += bb.collectBerries();
        }
    }

    public int getBerriesCollected() {
        return berriesCollected;
    }

    public void setBerriesCollected(int berriesCollected) {
        this.berriesCollected = Math.max(0,berriesCollected);
    }

    public void reset() {
        berriesCollected = START_BERRY_COUNT;
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
    private Game game;

    public BerryBush(Game game, int row, int col, ArrayList<Integer[]> ssTiles, String fileName, int priority) {
        super(game, row, col, ssTiles, fileName, priority);

        this.game = game;
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

    public int collectBerries() {
        return game.random.nextInt(2, 5);
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
