package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class Tile {

    private ArrayList<Object> entities;
    private TextureRegion img;

    private float imgX;
    private float imgY;

    // Tile coordinates
    private int x;
    private int y;

    public Tile(Game game, int x, int y, float imgX, float imgY) {
        entities = new ArrayList<Object>();
        this.imgX = imgX;
        this.imgY = imgY;
        this.x = x;
        this.y = y;
        Texture spriteSheetImg = game.am.get(Game.RSC_SS_GRASS_IMG);
        img = new TextureRegion(spriteSheetImg
                , 0 * PlayScreen.TILE_SIZE
                , 3 * PlayScreen.TILE_SIZE
                , PlayScreen.TILE_SIZE
                , PlayScreen.TILE_SIZE);
    }

    public void addEntity(Object e) {
        entities.add(e);
    }

    public TextureRegion getImg() {
        return img;
    }

    public float getImgX() {
        return imgX;
    }

    public float getImgY() {
        return imgY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
