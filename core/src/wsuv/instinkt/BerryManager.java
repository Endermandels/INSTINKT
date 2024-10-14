package wsuv.instinkt;

import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.ArrayList;
import java.util.Arrays;

public class BerryManager {

    private int berriesCollected;

    public BerryManager(Game game, ArrayList<GameObject> gameObjects) {
        berriesCollected = 0;

        ArrayList<Integer[]> bushSpriteLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{0,2},
                new Integer[]{0,2}
        ));

        gameObjects.add(new GameObject(game, PlayScreen.TILE_ROWS-1, 0, bushSpriteLocation, Game.RSC_SS_BERRIES_IMG, 0));
    }
}
