package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {

    private Texture spriteSheet;
    private TextureRegion img;

    public Player(Game game) {
        spriteSheet = game.am.get(Game.RSC_SS_SKUNK_IMG);
        img = new TextureRegion(spriteSheet, 0, 0, Tile.SIZE, Tile.SIZE);
    }

    public TextureRegion getImg() {
        return img;
    }
}
