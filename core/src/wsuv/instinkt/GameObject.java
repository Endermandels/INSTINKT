package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class GameObject {
    private TextureRegion img;

    protected float imgX;
    protected float imgY;

    private int priority; // Determines depth of drawing this sprite (higher = background; 0 = foreground)

    public GameObject(TextureRegion img, float imgX, float imgY, int priority) {
        this.img = img;
        this.imgX = imgX;
        this.imgY = imgY;
        this.priority = priority;
    }

    public GameObject(Game game, int row, int col, ArrayList<Integer[]> ssTiles, String fileName, int priority) {
        Integer[] ssTopLeft = ssTiles.get(0);
        Integer[] ssBottomRight = ssTiles.get(1);
        int yDown = 0;
        if (ssTiles.size() > 2) yDown = ssTiles.get(2)[0];

        int x = ssTopLeft[1]*PlayScreen.TILE_SIZE;
        int y = ssTopLeft[0]*PlayScreen.TILE_SIZE;
        int width = ssBottomRight[1]*PlayScreen.TILE_SIZE - x + PlayScreen.TILE_SIZE;
        int height = ssBottomRight[0]*PlayScreen.TILE_SIZE - y + PlayScreen.TILE_SIZE;

        this.priority = priority;

        img = new TextureRegion((Texture) game.am.get(fileName), x, y, width, height);
        int dif = ssBottomRight[1] - ssTopLeft[1];
        this.imgX = (col - (float) (dif / 2)) * PlayScreen.TILE_SCALED_SIZE;
        this.imgY = (row - yDown) * PlayScreen.TILE_SCALED_SIZE;
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

    public int getPriority() {
        return priority;
    }
}
