package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class Tile {

    public static int SIZE = 32;

    private ArrayList<Object> entities;
    private TextureRegion tileImg;


    public Tile(Game game, int row, int col) {
        entities = new ArrayList<Object>();
        Texture spriteSheetImg = game.am.get(Game.RSC_SS_GRASS_IMG);
        tileImg = new TextureRegion(spriteSheetImg, col * SIZE, row * SIZE, SIZE, SIZE);
    }

    public TextureRegion getTileImg() {
        return tileImg;
    }
}
