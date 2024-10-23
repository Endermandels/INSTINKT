package wsuv.instinkt;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;

public class BerryManager {

    private Game game;
    private Integer[] berryPileLocation;

    private final int BUSH_PRICE = 2;

    private final int START_BERRY_COUNT = 6;
    private int berriesCollected;

    private boolean infBerries;

    private final int START_BUSH_BOUND = 5;
    private final int BUSH_LOWER_BOUND = 3;
    private int bushBound;

    private ArrayList<BerryBush> bushes;
    private ArrayList<Berry> berries;

    private Sound plantSeed;
    private Sound collectBerrySound;

    public BerryManager(Game game, ArrayList<GameObject> gameObjects, Integer[] berryPileLocation) {
        this.game = game;
        this.berryPileLocation = berryPileLocation;

        berriesCollected = START_BERRY_COUNT;
        bushBound = START_BUSH_BOUND;
        infBerries = false;

        bushes = new ArrayList<>();
        plantSeed = game.am.get(Game.RSC_SEED_SFX);
        collectBerrySound = game.am.get(Game.RSC_SELECT_SFX);

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


        berries = new ArrayList<>();
    }

    public void plantNewBerryBush() {
        if (berriesCollected < BUSH_PRICE && !infBerries) return;
        for (BerryBush bb : bushes) {
            if (bb.state == BerryBush.State.UNPLANTED && !bb.expectingBerry) {
                if (!infBerries) berriesCollected-=BUSH_PRICE;
                bb.expectingBerry = true;
                berries.add(new Berry(game
                        , berryPileLocation[0]*PlayScreen.TILE_SCALED_SIZE
                        , berryPileLocation[1]*PlayScreen.TILE_SCALED_SIZE + PlayScreen.GUI_SPACE
                        , bb.getImgX(), bb.getImgY() + PlayScreen.GUI_SPACE));
                break;
            }
        }
    }

    public void startOfCooldown() {
        float delay = 0f;
        float offset = 0f;
        for (BerryBush bb : bushes) {
            if (bb.state == BerryBush.State.PLANTED) bb.grow();
            else if (bb.state == BerryBush.State.GROWN) {
                int x = BUSH_LOWER_BOUND + bushBound/START_BUSH_BOUND;
                int berriesToCollect = x+game.random.nextInt(bushBound-x);
                for (int i = 0; i < berriesToCollect; i++) {
                    berries.add(new Berry(game, bb.getImgX(), bb.getImgY()+PlayScreen.GUI_SPACE
                        , berryPileLocation[0]*PlayScreen.TILE_SCALED_SIZE
                        , berryPileLocation[1]*PlayScreen.TILE_SCALED_SIZE + PlayScreen.GUI_SPACE
                        , delay+offset));
                    delay += 3f/60f;
                }
                delay = 0f;
                offset += 3f/60f;
            }
        }
    }

    public void berryArrived(Berry berry) {
        if (berry.getTargetX() == berryPileLocation[0]*PlayScreen.TILE_SCALED_SIZE
                && berry.getTargetY() == berryPileLocation[1]*PlayScreen.TILE_SCALED_SIZE + PlayScreen.GUI_SPACE) {
            if (!infBerries) berriesCollected += 1;
            berries.remove(berry);
            collectBerrySound.play(0.01f, 1.5f, 0f);
        } else {
            for (BerryBush bb : bushes) {
                if (bb.imgX == berry.getTargetX() && bb.imgY == berry.getTargetY() - PlayScreen.GUI_SPACE) {
                    bb.grow();
                    plantSeed.play(0.05f);
                    berries.remove(berry);
                }
            }
        }
    }

    public void setInfBerries(boolean infBerries) {
        this.infBerries = infBerries;
        if (infBerries) berriesCollected = (int)Tile.INF - 1;
    }

    public void setBushBound(int bushBound) {
        this.bushBound = bushBound;
    }

    public int getBushBound() {
        return bushBound;
    }

    public ArrayList<Berry> getBerries() {
        return berries;
    }

    public int getBerriesCollected() {
        return berriesCollected;
    }

    public int setBerriesCollected(int berriesCollected) {
        if (infBerries) this.berriesCollected = (int) Tile.INF - 1;
        else this.berriesCollected = Math.max(0,berriesCollected);
        return this.berriesCollected;
    }

    public void reset() {
        berriesCollected = START_BERRY_COUNT;
        bushBound = START_BUSH_BOUND;
        infBerries = false;
        boolean first = true;
        for (BerryBush bb : bushes) {
            if (first) {
                first = false;
                continue;
            }
            bb.resetState();
        }
        berries.clear();
    }
}

class BerryBush extends GameObject {
    public enum State { UNPLANTED, PLANTED, GROWN }

    private TextureRegion seedlingTexture;
    private TextureRegion bushTexture;

    public State state;
    public boolean expectingBerry;

    public BerryBush(Game game, int row, int col, ArrayList<Integer[]> ssTiles, String fileName, int priority) {
        super(game, row, col, ssTiles, fileName, priority);

        bushTexture = super.getImg();

        ArrayList<Integer[]> seedlingSpriteLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{0,1},
                new Integer[]{0,1}
        ));

        seedlingTexture = GameObject.getImgRegion(game, seedlingSpriteLocation, Game.RSC_SS_BERRIES_IMG);

        state = State.UNPLANTED;
        expectingBerry = false;
    }

    public void grow() {
        if (state == State.UNPLANTED) state = State.PLANTED;
        else state = State.GROWN;
    }

    public void resetState() {
        state = State.UNPLANTED;
        expectingBerry = false;
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
