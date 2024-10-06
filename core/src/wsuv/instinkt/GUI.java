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
            hb.getHit(idx);
        }

        hb.update();
    }

    public void draw(Batch batch) {
        hb.draw(batch);
    }
}

class HealthBar {

    private AnimationManager am;
    private int shakeY;
    private long lastShaked;
    private final long FREQUENCY = 10L;

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
        shakeY = 0;
        lastShaked = -1L;
    }

    public void update() {
        long time = System.currentTimeMillis();
        if (shakeY != 0 && time > lastShaked + FREQUENCY) {
            shakeY = (Math.abs(shakeY) - 2) * -Integer.signum(shakeY);
            lastShaked = time;
        }
        am.update();
    }

    public void draw(Batch batch) {
        TextureRegion image = getImage();
        batch.draw(image, 8, 16+shakeY, image.getRegionWidth()*PlayScreen.TILE_SCALE,
                image.getRegionHeight()*PlayScreen.TILE_SCALE);
    }

    public void getHit(int idx) {
        am.switchAnimState(idx);
        am.setOneShot(true);
        shakeY = 10;
    }

    public TextureRegion getImage() {
        return am.getCurrentImage(false);
    }
}
