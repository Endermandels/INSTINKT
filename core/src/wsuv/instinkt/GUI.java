package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GUI {

    private Texture bg; // Background
    private HealthBar hb;
    private SprayBar sb;
    private BitmapFont font;
    private Player player;
    private PlayScreen playScreen;
    private BerryCounter berryCounter;
    private ShopSelect shopSelect;

    private int lastPlayerHP;
    private int lastPlayerSprayCount;

    private boolean shopOpen;
    private boolean selectKeyPressed;

    public GUI(Game game, Player player, BerryManager berryManager, PlayScreen playScreen) {
        hb = new HealthBar(game);
        sb = new SprayBar(game);
        shopSelect = new ShopSelect(game);
        font = game.am.get(Game.RSC_DPCOMIC_FONT_GUI);
        font.setColor(0, 0, 0, 1);
        bg = game.am.get(Game.RSC_GUI_AREA_IMG);
        this.player = player;
        this.playScreen = playScreen;
        this.berryCounter = new BerryCounter(game, berryManager);
        lastPlayerHP = player.getStats().getHp();
        lastPlayerSprayCount = player.getSpraysLeft();

        shopOpen = false;
        selectKeyPressed = false;
    }

    public void update() {
        if (lastPlayerHP != player.getStats().getHp()) {
            lastPlayerHP = player.getStats().getHp();
            int idx = (int) (8f * (1f-((float)lastPlayerHP/(float)player.getStats().getMaxHP())));
            hb.getHit(idx);
        }

        if (lastPlayerSprayCount != player.getSpraysLeft()) {
            lastPlayerSprayCount = player.getSpraysLeft();
            int idx = (int) (8f * (1f-((float)lastPlayerSprayCount/(float)player.getMaxSprays())));
            sb.useSpray(idx);
        }

        if (shopOpen) {
            if (!selectKeyPressed) {
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                    selectKeyPressed = true;
                    shopSelect.goRight();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                    selectKeyPressed = true;
                    shopSelect.goLeft();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                    selectKeyPressed = true;
                    shopSelect.goUp();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                    selectKeyPressed = true;
                    shopSelect.goDown();
                }
            } else if (!Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                    && !Gdx.input.isKeyPressed(Input.Keys.LEFT)
                    && !Gdx.input.isKeyPressed(Input.Keys.UP)
                    && !Gdx.input.isKeyPressed(Input.Keys.DOWN)
                    && !Gdx.input.isKeyPressed(Input.Keys.W)
                    && !Gdx.input.isKeyPressed(Input.Keys.A)
                    && !Gdx.input.isKeyPressed(Input.Keys.S)
                    && !Gdx.input.isKeyPressed(Input.Keys.D)) {
                selectKeyPressed = false;
            }

            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                int selectedIdx = shopSelect.getSelectedIdx();
                if (selectedIdx >= 0) {
                    upgradePlayer(selectedIdx);
                }
            }
        }

        hb.update();
        sb.update();
        berryCounter.update();
    }

    public void draw(Batch batch) {
        batch.draw(bg, 0f, 0f, bg.getWidth()*8f, bg.getHeight()*8f);
        hb.draw(batch, 32f);
        sb.draw(batch, 332f);
        font.draw(batch, "Wave: " + playScreen.getWave(), 634f, 82f);
        font.setColor(1,1,1,1);
        font.draw(batch, "Wave: " + playScreen.getWave(), 630f, 86f);
        font.setColor(0,0,0,1);
        berryCounter.draw(batch, 764f);
        if (shopOpen) shopSelect.draw(batch);
    }

    public void upgradePlayer(int selectedIdx) {

    }

    public boolean isShopOpen() {
        return shopOpen;
    }

    public void setShopOpen(boolean shopOpen) {
        this.shopOpen = shopOpen;
    }

    public void startEnemyWave() {
        shopSelect.resetIdx();
    }
}






class ShopSelect {
    private AnimationManager am;

    public ShopSelect(Game game) {
        am = new AnimationManager(game.am.get(Game.RSC_SS_SHOP_SELECT_IMG)
                , new ArrayList<>(Arrays.asList(1,1,1,1,1,1,1,1,1))
                , new HashMap<>() {{
            put("SEL0", 0);
            put("SEL1", 1);
            put("SEL2", 2);
            put("SEL3", 3);
            put("SEL4", 4);
            put("SEL5", 5);
            put("SEL6", 6);
            put("SEL7", 7);
            put("SEL8", 8);
        }}
                , 0f, 77, 39, true
        );
        am.setOneShot(true);
    }

    public void draw(Batch batch) {
        TextureRegion image = getImage();
        float width = image.getRegionWidth()*PlayScreen.TILE_SCALE*4f;
        float height = image.getRegionHeight()*PlayScreen.TILE_SCALE*4f;
        float x = Gdx.graphics.getWidth()/2f - width/2f;
        float y = Gdx.graphics.getHeight()/2f - height/2f + PlayScreen.GUI_SPACE/2f;
        batch.draw(image, x, y, width, height);
    }

    public void goRight() {
        if (am.getCurrentRow() < 8) {
            am.switchAnimState(am.getCurrentRow()+1);
        } else {
            am.switchAnimState(1);
        }
    }

    public void goLeft() {
        if (am.getCurrentRow() > 1) {
            am.switchAnimState(am.getCurrentRow()-1);
        } else if (am.getCurrentRow() == 0) {
            am.switchAnimState(1);
        } else {
            am.switchAnimState(8);
        }
    }

    public void goDown() {
        if (am.getCurrentRow() < 5 && am.getCurrentRow() != 0) {
            am.switchAnimState(am.getCurrentRow()+4);
        } else if (am.getCurrentRow() == 0) {
            am.switchAnimState(1);
        }
    }

    public void goUp() {
        if (am.getCurrentRow() > 4) {
            am.switchAnimState(am.getCurrentRow()-4);
        } else if (am.getCurrentRow() == 0) {
            am.switchAnimState(1);
        }
    }


    public int getSelectedIdx() {
        return am.getCurrentRow()-1;
    }

    public void resetIdx() {
        am.switchAnimState(0);
    }

    public TextureRegion getImage() {
        return am.getCurrentImage(false);
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

    public void draw(Batch batch, float x) {
        TextureRegion image = getImage();
        batch.draw(image, x+shakeX, 32, image.getRegionWidth()*PlayScreen.TILE_SCALE*2f,
                image.getRegionHeight()*PlayScreen.TILE_SCALE*2f);
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

    public void draw(Batch batch, float x) {
        TextureRegion image = getImage();
        batch.draw(image, x, 32+shakeY, image.getRegionWidth()*PlayScreen.TILE_SCALE*2f,
                image.getRegionHeight()*PlayScreen.TILE_SCALE*2f);
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

    private int shakeY;
    private long lastShaked;
    private final long FREQUENCY = 50L;

    private int lastBerryCount;

    public BerryCounter(Game game, BerryManager berryManager) {
        this.berryManager = berryManager;
        font = game.am.get(Game.RSC_DPCOMIC_FONT_GUI);

        ArrayList<Integer[]> berryIconLocation = new ArrayList<>(Arrays.asList(
                new Integer[]{1,0},
                new Integer[]{1,0}
        ));

        berryIcon = GameObject.getImgRegion(game, berryIconLocation, Game.RSC_SS_BERRIES_IMG);

        shakeY = 0;
        lastShaked = -1L;

        lastBerryCount = berryManager.getBerriesCollected();
    }

    public void update() {
        long time = System.currentTimeMillis();
        if (shakeY != 0 && time > lastShaked + FREQUENCY) {
            shakeY = (Math.abs(shakeY) - 2) * -Integer.signum(shakeY);
            lastShaked = time;
        }

        if (lastBerryCount != berryManager.getBerriesCollected()) {
            lastBerryCount = berryManager.getBerriesCollected();
            shakeY = 8;
        }
    }

    public void draw(Batch batch, float x) {
        batch.draw(berryIcon, x, -68f+shakeY, berryIcon.getRegionWidth()*8, berryIcon.getRegionHeight()*8);
        font.draw(batch, Integer.toString(berryManager.getBerriesCollected()), x+184f, 82f);
        font.setColor(1,1,1,1);
        font.draw(batch, Integer.toString(berryManager.getBerriesCollected()), x+178f, 86f);
        font.setColor(0,0,0,1);
    }
}
