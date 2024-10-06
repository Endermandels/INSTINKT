package wsuv.instinkt;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GUI {

    private HealthBar hb;
    private Player player;
    private int lastPlayerHP;

    public GUI(Game game, Player player) {
        hb = new HealthBar(game);
        this.player = player;
        lastPlayerHP = player.getStats().getHp();
    }

    public void update() {
        if (lastPlayerHP != player.getStats().getHp()) {
            lastPlayerHP = player.getStats().getHp();
            int idx = (player.getStats().getMaxHP() - lastPlayerHP);
            hb.getAm().switchAnimState(idx);
            hb.getAm().setOneShot(true);
        }

        hb.update();
    }

    public void draw(Batch batch) {
        TextureRegion image = hb.getImage();
        batch.draw(image, 100, 100, image.getRegionWidth()*PlayScreen.TILE_SCALE,
                image.getRegionHeight()*PlayScreen.TILE_SCALE);
    }
}

class HealthBar {

    private AnimationManager am;

    public HealthBar(Game game) {
        am = new AnimationManager(game.am.get(Game.RSC_SS_HEALTH_BAR_IMG)
                , new ArrayList<>(Arrays.asList(2,6,6,6,6,6,6,6,6))
                , new HashMap<>() {{
            put("FULL", 0);
            put("HIT1", 1);
            put("HIT2", 2);
            put("HIT3", 3);
            put("HIT4", 4);
            put("HIT5", 5);
            put("HIT6", 6);
            put("HIT7", 7);
            put("HIT8", 8);
        }}
                , 0.3f, 64, 16, true
        );
        am.setOneShot(true);
    }

    public void update() {
        am.update();
    }

    public AnimationManager getAm() {
        return am;
    }

    public TextureRegion getImage() {
        return am.getCurrentImage(false);
    }
}
