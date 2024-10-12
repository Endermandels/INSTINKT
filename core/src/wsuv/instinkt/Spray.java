package wsuv.instinkt;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Spray extends GameObject {

    private int length;

    private AnimationManager am;

    boolean shown;
    boolean flipped;

    public Spray(Game game) {
        super(null, 0f, 0f, 11);
        length = 2;
        am = new AnimationManager(game.am.get(Game.RSC_SS_SPRAY_IMG)
                , new ArrayList<Integer>(Arrays.asList(5))
                , new HashMap<String, Integer>() {{
            put("SPRAY", 0);
        }}
                , 0.08f, 32, 32, true
        );
    }

    public void update() {
        if (shown) {
            am.update();
            if (am.isFinished()) {
                shown = false;
            }
        }
    }

    public void show(boolean flipped, float imgX, float imgY, int length) {
        shown = true;
        this.flipped = flipped;
        this.length = length;
        if (flipped) {
            this.imgX = imgX + PlayScreen.TILE_SCALED_SIZE / 2f;
        } else {
            this.imgX = imgX - PlayScreen.TILE_SCALED_SIZE / 2f;
        }
        this.imgY = imgY;

        am.restartOneShotAnimation();
    }

    public TextureRegion getImg() {
        if (!shown) return null;
        return am.getCurrentImage(flipped);
    }

    public int getLength() {
        return length;
    }

    public boolean isFlipped() {
        return flipped;
    }
}
