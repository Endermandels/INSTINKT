package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GUI {

    private PlayScreen playScreen;
    private AnimationManager am;
    private BitmapFont font;
    private Player player;

    private Sound switchSound;
    private Sound selectSound;
    private Sound invalidSound;

    private HealthBar hb;
    private SprayBar sb;
    private BerryCounter berryCounter;
    private ShopSelect shopSelect;
    private BerryManager berryManager;

    private ArrayList<Upgrade> upgrades;

    private int lastPlayerHP;
    private int lastPlayerSprayCount;

    private boolean wasShopOpen;
    private boolean shopOpen;
    private boolean selectKeyPressed;

    public GUI(Game game, Player player, BerryManager berryManager, PlayScreen playScreen) {
        hb = new HealthBar(game);
        sb = new SprayBar(game);
        shopSelect = new ShopSelect(game);

        font = game.am.get(Game.RSC_DPCOMIC_FONT_GUI);
        switchSound = game.am.get(Game.RSC_SWITCH_SFX);
        selectSound = game.am.get(Game.RSC_SELECT_SFX);
        invalidSound = game.am.get(Game.RSC_INVALID_SFX);

        am = new AnimationManager(game.am.get(Game.RSC_SS_GUI_AREA_IMG)
                , new ArrayList<>(Arrays.asList(1,3,2))
                , new HashMap<>() {{
            put("SHORT", 0);
            put("EXPANDED", 1);
            put("COLLAPSING", 2);
        }}
                , 0.08f, 144, 64, true
        );
        am.setOneShot(true);

        this.player = player;
        this.playScreen = playScreen;
        this.berryManager = berryManager;
        this.berryCounter = new BerryCounter(game, berryManager);
        lastPlayerHP = player.getStats().getHp();
        lastPlayerSprayCount = player.getSpraysLeft();

        shopOpen = false;
        wasShopOpen = false;
        selectKeyPressed = false;

        upgrades = new ArrayList<>();

        upgrades.add(new Upgrade(game.am.get(Game.RSC_SS_HEART_ICON_IMG)
                 ,"Thicker Skin", "+1 Max HP", 3, 0));
        upgrades.add(new Upgrade(game.am.get(Game.RSC_SS_HEART_ICON_IMG)
                ,"Sharper Claws", "+1 ATK", 5, 1));
        upgrades.add(new Upgrade(game.am.get(Game.RSC_SS_BERRY_ICON_IMG)
                ,"Quality Berries", "+1 HP/Spray Berry Regen", 5, 2));
        upgrades.add(new Upgrade(game.am.get(Game.RSC_SS_BERRY_ICON_IMG)
                ,"Fertilizer", "+2 Berry Gathering", 3, 3));
        upgrades.add(new Upgrade(game.am.get(Game.RSC_SS_SPRAY_ICON_IMG)
                ,"Extra Reserves", "+1 Max Spray", 4, 4));
        upgrades.add(new Upgrade(game.am.get(Game.RSC_SS_SPRAY_ICON_IMG)
                ,"Target Practice", "+1 Spray Length", 1, 5));
        upgrades.add(new Upgrade(game.am.get(Game.RSC_SS_SPRAY_ICON_IMG)
                ,"Ferment", "+1 Spray Radius", 20, 6));
        upgrades.add(new Upgrade(game.am.get(Game.RSC_SS_SPRAY_ICON_IMG)
                ,"Nuclear Spray", "Overwhelm sprayed Enemies", 100, 7));
    }

    public void update(boolean hudOpen) {
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

        // Shop Inputs
        if (shopOpen && !hudOpen) {
            if (!wasShopOpen) {
                wasShopOpen = true;
                switchSound.play(0.1f);
            }
            if (!selectKeyPressed) {
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                    movedSelection();
                    shopSelect.goRight();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                    movedSelection();
                    shopSelect.goLeft();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                    movedSelection();
                    shopSelect.goUp();
                }
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                    movedSelection();
                    shopSelect.goDown();
                }
            } else if (!Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                    && !Gdx.input.isKeyPressed(Input.Keys.LEFT)
                    && !Gdx.input.isKeyPressed(Input.Keys.UP)
                    && !Gdx.input.isKeyPressed(Input.Keys.DOWN)
                    && !Gdx.input.isKeyPressed(Input.Keys.W)
                    && !Gdx.input.isKeyPressed(Input.Keys.A)
                    && !Gdx.input.isKeyPressed(Input.Keys.S)
                    && !Gdx.input.isKeyPressed(Input.Keys.D)
                    && !Gdx.input.isKeyPressed(Input.Keys.E)
                    && !Gdx.input.isKeyPressed(Input.Keys.SPACE)
            ) {
                selectKeyPressed = false;
            }

            // Select Upgrade
            if ((Gdx.input.isKeyPressed(Input.Keys.E) || Gdx.input.isKeyPressed(Input.Keys.SPACE))
                    && !selectKeyPressed) {
                selectKeyPressed = true;
                int selectedIdx = shopSelect.getSelectedIdx();
                if (selectedIdx >= 0) {
                    if (upgradePlayer(selectedIdx)) {
                        selectSound.play(0.05f, 1.5f, 0f);
                    } else {
                        invalidSound.play(0.05f);
                    }
                }
                berryCounter.shake();
            }
        } else if (!shopOpen && wasShopOpen) {
            wasShopOpen = false;
            switchSound.play(0.1f);
        }

        hb.update();
        sb.update();
        berryCounter.update();

        for (Upgrade upgrade : upgrades) {
            upgrade.update();
        }

        if (shopOpen) am.switchAnimState("EXPANDED");
        else if (!am.getCurrentAnimState().equals("SHORT")) am.switchAnimState("COLLAPSING", "SHORT");
        am.setOneShot(true);
        am.update();
    }

    public void movedSelection() {
        selectKeyPressed = true;
        switchSound.play(0.1f);
    }

    private String getCurrentStat(int idx) {
        Stats stats = player.getStats();
        switch (idx) {
            case 0:
                return "Max HP: " + stats.getMaxHP();
            case 1:
                return "ATK: " + stats.getAtk();
            case 2:
                return "Regen: " + player.getBerryHPRegen();
            case 3:
                return "Max Production: " + berryManager.getBushBound();
            case 4:
                return "Max Sprays: " + player.getMaxSprays();
            case 5:
                return "Spray Length: " + player.getSprayLength();
            case 6:
                return "Spray Radius: " + player.getSprayRadius();
            case 7:
                return "Status: " + (player.isNuclearSpray() ? "Nuclear" : "Normal");
        }
        return "NA";
    }

    public void draw(Batch batch) {
        TextureRegion bg = am.getCurrentImage(false);
        batch.draw(bg, 0f, 0f, bg.getRegionWidth()*8f, bg.getRegionHeight()*8f);
        hb.draw(batch, 32f);
        sb.draw(batch, 332f);
        drawGUIText(batch, font, "Wave: " + playScreen.getWave(), 634f, 82f);
        berryCounter.draw(batch, 764f);
        if (shopOpen && am.isFinished()) {
            shopSelect.draw(batch);

            int selectedIdx = shopSelect.getSelectedIdx();
            for (Upgrade upgrade : upgrades) {
                upgrade.draw(batch, selectedIdx);
            }

            if (selectedIdx > -1 && selectedIdx < upgrades.size()) {
                float x = 100f;
                float y = 420f;
                float dy = -60f;
                Upgrade selectedUpgrade = upgrades.get(selectedIdx);
                drawGUIText(batch, font, "Cost: " + selectedUpgrade.cost, x, y + (0*dy));
                drawGUIText(batch, font, selectedUpgrade.desc, x, y + (2*dy));
                drawGUIText(batch, font, selectedUpgrade.details, x, y + (3*dy));
                drawGUIText(batch, font, getCurrentStat(selectedIdx), x + 500f, y);
            }
        }
    }

    /**
     * @return whether the upgrade completed successfully
     */
    public boolean upgradePlayer(int selectedIdx) {
        if (selectedIdx > -1 && selectedIdx < upgrades.size()) {
            Upgrade upgrade = upgrades.get(selectedIdx);
            if (berryManager.getBerriesCollected() >= upgrade.cost && upgrade.cost > 0) {
                berryManager.setBerriesCollected(berryManager.getBerriesCollected() - upgrade.cost);
                upgrade.increaseCost();
                Stats playerStats = player.getStats();
                switch (selectedIdx) {
                    case 0:
                        // Max HP
                        playerStats.setMaxHP(playerStats.getMaxHP()+1);
                        break;
                    case 1:
                        // ATK
                        playerStats.setAtk(playerStats.getAtk()+1);
                        break;
                    case 2:
                        // Berry Healing
                        player.setBerryHPRegen(player.getBerryHPRegen()+1);
                        player.setBerrySprayRegen(player.getBerrySprayRegen()+1);
                        break;
                    case 3:
                        // Berry Growth
                        berryManager.setBushBound(berryManager.getBushBound()+2);
                        break;
                    case 4:
                        // Max Sprays
                        player.setMaxSprays(player.getMaxSprays()+1);
                        break;
                    case 5:
                        // Spray Length (and cooldown)
                        player.setSprayLength(player.getSprayLength()+1);
                        player.setSprayCooldown(player.getSprayCooldown()-100L);
                        break;
                    case 6:
                        // Spray Radius (and duration)
                        player.setSprayRadius(player.getSprayRadius()+1);
                        player.setSprayDuration(player.getSprayDuration()+500L);
                        if (player.getSprayRadius() > 3) {
                            upgrade.cost = 999999999;
                            upgrade.desc = "Fully fermented";
                            upgrade.details = "-w-";
                        }
                        break;
                    case 7:
                        // Nuclear Spray
                        player.setNuclearSpray(true);
                        upgrade.cost = -1;
                        upgrade.desc = "One can only become nuclear once";
                        upgrade.details = ":3";
                        break;
                }
                return true;
            }
        }
        return false;
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

    public void reset() {
        for (Upgrade upgrade : upgrades) {
            upgrade.reset();
        }
        am.switchAnimState("SHORT");
        shopOpen = false;
    }

    public static void drawGUIText(Batch batch, BitmapFont font, String str, float x, float y) {
        font.setColor(0,0,0,1);
        font.draw(batch, str, x, y);
        font.setColor(1,1,1,1);
        font.draw(batch, str, x-4f, y+4f);
    }
}


class Upgrade {

    private AnimationManager am;
    private int startCost;

    private int idx;
    public int cost;
    public String desc;
    public String details;
    public String startDesc;
    public String startDetails;

    public Upgrade(Texture spriteSheet, String description, String details, int cost, int idx) {
        this.am = new AnimationManager(
                spriteSheet
                , new ArrayList<>(Arrays.asList(1,10))
                , new HashMap<>() {{
            put("STILL", 0);
            put("MOVING", 1);
        }}
                , 0.04f, 24, 22, true
        );
        this.idx = idx;

        this.desc = description;
        this.details = details;
        startDesc = description;
        startDetails = details;
        this.cost = cost;
        startCost = cost;
    }

    public void update() {
        am.update();
    }

    public void draw(Batch batch, int selectedIdx) {
        TextureRegion img = am.getCurrentImage(false);
        float width = img.getRegionWidth()*PlayScreen.TILE_SCALE*2f;
        float height = img.getRegionHeight()*PlayScreen.TILE_SCALE*2f;
        float x = Gdx.graphics.getWidth()/2f - width/2f - 190f;
        x += (idx%4)*128f;
        float y = Gdx.graphics.getHeight()/2f - height/2f + 210f + PlayScreen.GUI_SPACE;
        y -= idx > 3 ? 128f : 0f;
        if (idx == selectedIdx) {
            am.switchAnimState("MOVING");
            switch (idx) {
                case 0:
                    x -= 32f;
                    y += 8f;
                    break;
                case 1:
                    x -= 8f;
                    y += 8f;
                    break;
                case 2:
                    x += 8f;
                    y += 8f;
                    break;
                case 3:
                    x += 32f;
                    y += 8f;
                    break;
                case 4:
                    x -= 32f;
                    y -= 8f;
                    break;
                case 5:
                    x -= 8f;
                    y -= 8f;
                    break;
                case 6:
                    x += 8f;
                    y -= 8f;
                    break;
                case 7:
                    x += 32f;
                    y -= 8f;
                    break;
            }
        } else {
            am.switchAnimState("STILL");
        }
        batch.draw(img, x, y, width, height);
    }

    public void reset() {
        desc = startDesc;
        details = startDetails;
        cost = startCost;
    }

    public void increaseCost() {
        cost *= 2;
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
        float y = Gdx.graphics.getHeight()/2f - height/2f + 140f + PlayScreen.GUI_SPACE;
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
            shake();
        }
    }

    public void draw(Batch batch, float x) {
        batch.draw(berryIcon, x, -68f+shakeY, berryIcon.getRegionWidth()*8, berryIcon.getRegionHeight()*8);
        GUI.drawGUIText(batch, font, Integer.toString(berryManager.getBerriesCollected()), x+184f, 82f);
    }

    public void shake() {
        shakeY = 8;
    }
}
