package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Obstacle {
    private TextureRegion img;

    private float imgX;
    private float imgY;

    public Obstacle(Game game, float imgX, float imgY) {
        img = new TextureRegion((Texture) game.am.get(Game.RSC_SS_PLANTS_IMG), 96, 192, 32, 32);
        this.imgX = imgX;
        this.imgY = imgY;
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
