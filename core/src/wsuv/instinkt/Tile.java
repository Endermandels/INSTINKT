package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class Tile {

    private TextureRegion img;

    private float imgX;
    private float imgY;

    // Tile coordinates
    private int x;
    private int y;

    private boolean containsObstacle;

    public Tile(Game game, int x, int y, float imgX, float imgY) {
        this.imgX = imgX;
        this.imgY = imgY;
        this.x = x;
        this.y = y;
        containsObstacle = false;
        Texture spriteSheetImg = game.am.get(Game.RSC_SS_GRASS_IMG);
        img = new TextureRegion(spriteSheetImg
                , 0 * PlayScreen.TILE_SIZE
                , 3 * PlayScreen.TILE_SIZE
                , PlayScreen.TILE_SIZE
                , PlayScreen.TILE_SIZE);
    }

    public void setContainsObstacle(boolean containsObstacle) {
        this.containsObstacle = containsObstacle;
    }

    public boolean isObstacle() {
        return containsObstacle;
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
