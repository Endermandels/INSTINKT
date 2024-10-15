package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GUI {

    private HealthBar hb;
    private SprayBar sb;
    private Player player;
    private BerryCounter berryCounter;

    private int lastPlayerHP;
    private int lastPlayerSprayCount;

    public GUI(Game game, Player player, BerryManager berryManager) {
        hb = new HealthBar(game);
        sb = new SprayBar(game);
        this.player = player;
        this.berryCounter = new BerryCounter(game, berryManager);
        lastPlayerHP = player.getStats().getHp();
    }

    public void update() {
        if (lastPlayerHP != player.getStats().getHp()) {
            lastPlayerHP = player.getStats().getHp();
            int idx = (player.getStats().getMaxHP() - lastPlayerHP);
            hb.getHit(idx);
        }

        if (lastPlayerSprayCount != player.getSpraysLeft()) {
            lastPlayerSprayCount = player.getSpraysLeft();
            int idx = (player.getMaxSprays() - lastPlayerSprayCount);
            sb.useSpray(idx);
        }

        hb.update();
        sb.update();
    }

    public void draw(Batch batch) {
        hb.draw(batch);
        sb.draw(batch);
        berryCounter.draw(batch);
    }
}

class SprayBar {
    private AnimationManager am;
    private int shakeX;
    private long lastShaked;
    private final long FREQUENCY = 50L;

    public SprayBar(Game game) {
        am = new AnimationManager(game.am.get(Game.RSC_SS_SPRAY_BAR_IMG)
                , new ArrayList<>(Arrays.asList(1,5,5,6,5,5,4,4,6))
                , new HashMap<>() {{
            put("FULL", 0);
            put("LVL1", 1);
            put("LVL2", 2);
            put("LVL3", 3);
            put("LVL4", 4);
            put("LVL5", 5);
            put("LVL6", 6);
            put("LVL7", 7);
            put("LVL8", 8);
        }}
                , 0.08f, 64, 16, true
        );
        am.setOneShot(true);
        shakeX = 0;
        lastShaked = -1L;
    }


    public void update() {
        long time = System.currentTimeMillis();
        if (shakeX != 0 && time > lastShaked + FREQUENCY) {
            shakeX = (Math.abs(shakeX) - 2) * -Integer.signum(shakeX);
            lastShaked = time;
        }
        am.update();
    }

    public void draw(Batch batch) {
        TextureRegion image = getImage();
        batch.draw(image, 200+shakeX, 16, image.getRegionWidth()*PlayScreen.TILE_SCALE,
                image.getRegionHeight()*PlayScreen.TILE_SCALE);
    }

    public void useSpray(int idx) {
        am.switchAnimState(idx);
        am.setOneShot(true);
        shakeX = 10;
    }

    public TextureRegion getImage() {
        return am.getCurrentImage(false);
    }
}

class HealthBar {

    private AnimationManager am;
    private int shakeY;
    private long lastShaked;
    private final long FREQUENCY = 50L;

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


class BerryCounter {
    private TextureRegion berryIcon;
    private BerryManager berryManager;
    private BitmapFont font;

    public BerryCounter(Game game, BerryManager berryManager) {
        this.berryManager = berryManager;
        font = game.am.get(Game.RSC_DPCOMIC_FONT);

        ArrayList<Integer[]> berryIconLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{1,0},
                new Integer[]{1,0}
        ));

        berryIcon = GameObject.getImgRegion(game, berryIconLocation, Game.RSC_SS_BERRIES_IMG);
    }

    public void draw(Batch batch) {
        font.draw(batch, Integer.toString(berryManager.getBerriesCollected()), 420f, 40f);
        batch.draw(berryIcon, 320f, -35f, berryIcon.getRegionWidth()*4, berryIcon.getRegionHeight()*4);
    }
}
