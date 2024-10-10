package wsuv.instinkt;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Spray extends GameObject {

    private int tileX;
    private int tileY;

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

    public void setShown(boolean shown, boolean flipped) {
        this.shown = shown;
        this.flipped = flipped;
        am.restartOneShotAnimation();
    }

    public TextureRegion getImg() {
        if (!shown) return null;
        return am.getCurrentImage(flipped);
    }
}
