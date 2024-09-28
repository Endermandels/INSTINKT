package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class Obstacle {
    private TextureRegion img;

    private float imgX;
    private float imgY;

    public Obstacle(Game game, int row, int col, ArrayList<Integer[]> ssTiles) {
        Integer[] ssTopLeft = ssTiles.get(0);
        Integer[] ssBottomRight = ssTiles.get(1);
        int yDown = 0;
        if (ssTiles.size() > 2) yDown = ssTiles.get(2)[0];

        int x = ssTopLeft[1]*PlayScreen.TILE_SIZE;
        int y = ssTopLeft[0]*PlayScreen.TILE_SIZE;
        int width = ssBottomRight[1]*PlayScreen.TILE_SIZE - x + PlayScreen.TILE_SIZE;
        int height = ssBottomRight[0]*PlayScreen.TILE_SIZE - y + PlayScreen.TILE_SIZE;

        img = new TextureRegion((Texture) game.am.get(Game.RSC_SS_PLANTS_IMG), x, y, width, height);
        int dif = ssBottomRight[1] - ssTopLeft[1];
        this.imgX = (col - (float) (dif / 2)) * PlayScreen.TILE_SIZE * PlayScreen.TILE_SCALE;
        this.imgY = (row - yDown) * PlayScreen.TILE_SIZE * PlayScreen.TILE_SCALE;
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
}
