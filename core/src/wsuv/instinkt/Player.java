package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;

public class Player {

    private AnimationManager am;

    public Player(Game game) {
        am = new AnimationManager(game.am.get(Game.RSC_SS_SKUNK_IMG)
                , new ArrayList<Integer>(Arrays.asList(6,8,5,4,7))
                , new HashMap<String, Integer>() {{
                    put("IDLE", 0);
                    put("RUN", 1);
                    put("SPRAY", 2);
                    put("HURT", 3);
                    put("DEAD", 4);
                    }}
                , 0.08f, 32, 32
        );
    }

    public void update() {
        am.update();
    }

    public TextureRegion getImg() {
        return am.getCurrentImage();
    }

    public AnimationManager getAm() {
        return am;
    }
}
