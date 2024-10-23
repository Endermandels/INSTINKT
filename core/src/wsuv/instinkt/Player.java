package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

public class Player extends GameObject {

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Game game;
    private AnimationManager am;
    private Stats stats;
    private Sound hurtSound;
    private Sound deathSound;
    private Sound spraySound;
    private Sound eatSound;
    private Sound stepSound;

    // Track the position of the player's image separately from tile coordinates
    private float imgX;
    private float imgY;
    private float imgSpeed;

    // Tile coordinates
    private int startTileX;
    private int startTileY;
    private int tileX;
    private int tileY;

    private Tile targetTile; // When the player is moving
    private boolean movingHorizontal;
    private boolean movingVertical;
    private boolean flipped;
    private Direction dir;
    private Map<Direction, Long> pressedButtons; // Direction key, timestamp value (-1 if not pressed)

    private boolean takeInput;
    private boolean finishedDeathAnimation;
    private long timeFinishedDeathAnimation;

    // Spray Stats
    private boolean toSpray;
    private Spray spray;
    private final long SPRAY_DELAY = 200L;
    private long lastTimeSprayed;

    private long sprayCooldown;
    private long startSprayCooldown;
    private int maxSprays;
    private int startMaxSprays;
    private int spraysLeft;
    private int sprayRadius;
    private int startSprayRadius;
    private long sprayDuration;
    private long startSprayDuration;
    private int sprayLength;
    private int startSprayLength;

    private boolean nuclearSpray;

    // Eat Berry Stats
    private int berrySprayRegen;
    private int berryHPRegen;

    private boolean slowed; // for when cobra hits player
    private long timeSlowed;
    private long slowedDuration;
    private float slowedAmount;

    private final float STEP_SOUND_DURATION = 0.3f;
    private float stepTimer;

    public Player(Game game, int tileX, int tileY, ArrayList<GameObject> gameObjects) {
        super(null, tileX * PlayScreen.TILE_SCALED_SIZE
                , tileY * PlayScreen.TILE_SCALED_SIZE, 10);
        this.game = game;
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

        stats = new Stats(8, 1, 800L);

        hurtSound = game.am.get(Game.RSC_SQUIRREL_NOISE_SFX);
        deathSound = game.am.get(Game.RSC_SQUIRREL_NOISE_2_SFX);
        spraySound = game.am.get(Game.RSC_SPRAY_SFX);
        eatSound = game.am.get(Game.RSC_EAT_SFX);
        stepSound = game.am.get(Game.RSC_SMALL_STEP_SFX);

        imgX = tileX * PlayScreen.TILE_SCALED_SIZE;
        imgY = tileY * PlayScreen.TILE_SCALED_SIZE;

        imgSpeed = 250f;
        movingHorizontal = false;
        movingVertical = false;

        pressedButtons = new HashMap<>(4);
        pressedButtons.put(Direction.UP, -1L);
        pressedButtons.put(Direction.DOWN, -1L);
        pressedButtons.put(Direction.LEFT, -1L);
        pressedButtons.put(Direction.RIGHT, -1L);
        dir = null;

        takeInput = true;
        finishedDeathAnimation = false;
        timeFinishedDeathAnimation = -1L;

        lastTimeSprayed = -1L;

        sprayCooldown = 1000L;
        startSprayCooldown = sprayCooldown;
        maxSprays = 2;
        startMaxSprays = maxSprays;
        spraysLeft = maxSprays;
        sprayRadius = 1;
        startSprayRadius = sprayRadius;
        sprayDuration = 800L;
        startSprayDuration = sprayDuration;
        sprayLength = 2;
        startSprayLength = sprayLength;

        nuclearSpray = false;

        berrySprayRegen = 1;
        berryHPRegen = 1;

        toSpray = false;

        slowed = false;
        timeSlowed = -1L;
        slowedDuration = 2500L;
        slowedAmount = 200f;

        spray = new Spray(game);
        gameObjects.add(spray);

        this.startTileX = tileX;
        this.startTileY = tileY;
        this.tileX = tileX;
        this.tileY = tileY;
        targetTile = null;

        stepTimer = STEP_SOUND_DURATION;
    }

    /**
     * @return Whether the player has changed tile position
     */
    private boolean move(Tile[][] tileMap) {
        boolean onNewTile = false;

        float time = Gdx.graphics.getDeltaTime();
        if (time > 1f) time = 1f / 60f;

        boolean leftInput = false, rightInput = false, upInput = false, downInput = false;

        if (takeInput){
            leftInput = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            rightInput = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
            upInput = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
            downInput = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
            if (leftInput || rightInput || upInput || downInput) {
                if (stepTimer > STEP_SOUND_DURATION) {
                    stepSound.play(0.1f, 1.5f, 0f);
                    stepTimer = 0f;
                }
            }
        }
        stepTimer += time;

        // TODO: Any movement requests 1 second old are stale and should be set to -1L

        if (leftInput && pressedButtons.get(Direction.LEFT) < 0) {
            pressedButtons.put(Direction.LEFT, System.currentTimeMillis());
        }
        if (rightInput && pressedButtons.get(Direction.RIGHT) < 0) {
            pressedButtons.put(Direction.RIGHT, System.currentTimeMillis());
        }
        if (upInput && pressedButtons.get(Direction.UP) < 0) {
            pressedButtons.put(Direction.UP, System.currentTimeMillis());
        }
        if (downInput && pressedButtons.get(Direction.DOWN) < 0) {
            pressedButtons.put(Direction.DOWN, System.currentTimeMillis());
        }



        if (movingHorizontal) {
            // Horizontal movement only
            if (leftInput && rightInput) {
                // Choose most recent
                if (pressedButtons.get(Direction.LEFT) > pressedButtons.get(Direction.RIGHT)
                    && dir != Direction.LEFT) {
                    targetTile = game.findTile(tileMap,tileX,tileY,0, -1);
                    dir = Direction.LEFT;
                    flipped = true;
                } else if (pressedButtons.get(Direction.RIGHT) > pressedButtons.get(Direction.LEFT)
                        && dir != Direction.RIGHT) {
                    targetTile = game.findTile(tileMap,tileX,tileY,0, 1);
                    dir = Direction.RIGHT;
                    flipped = false;
                }
            } else if (leftInput && dir != Direction.LEFT) {
                dir = Direction.LEFT;
                targetTile = game.findTile(tileMap,tileX,tileY,0, -1);
                flipped = true;
            } else if (rightInput && dir != Direction.RIGHT) {
                dir = Direction.RIGHT;
                targetTile = game.findTile(tileMap,tileX,tileY,0, 1);
                flipped = false;
            }
        } else if (movingVertical) {
            // Vertical movement only
            if (upInput && downInput) {
                // Choose most recent
                if (pressedButtons.get(Direction.UP) > pressedButtons.get(Direction.DOWN)
                        && dir != Direction.UP) {
                    targetTile = game.findTile(tileMap,tileX,tileY,1, 0);
                    dir = Direction.UP;
                } else if (pressedButtons.get(Direction.DOWN) > pressedButtons.get(Direction.UP)
                        && dir != Direction.DOWN) {
                    targetTile = game.findTile(tileMap,tileX,tileY,-1, 0);
                    dir = Direction.DOWN;
                }
            } else if (upInput && dir != Direction.UP) {
                dir = Direction.UP;
                targetTile = game.findTile(tileMap,tileX,tileY,1, 0);
            } else if (downInput && dir != Direction.DOWN) {
                dir = Direction.DOWN;
                targetTile = game.findTile(tileMap,tileX,tileY,-1, 0);
            }
        } else {
            // Start moving
            long mostRecentTime = -1L;
            dir = null;
            for (Direction d : Direction.values()) {
                if (pressedButtons.get(d) > mostRecentTime) {
                    dir = d;
                    mostRecentTime = pressedButtons.get(d);
                }
            }
            if (dir != null) {
                am.switchAnimState("RUN");
                switch (dir) {
                    case LEFT:
                        targetTile = game.findTile(tileMap,tileX,tileY,0, -1);
                        movingHorizontal = true;
                        flipped = true;
                        break;
                    case RIGHT:
                        targetTile = game.findTile(tileMap,tileX,tileY,0, 1);
                        movingHorizontal = true;
                        flipped = false;
                        break;
                    case UP:
                        targetTile = game.findTile(tileMap,tileX,tileY,1, 0);
                        movingVertical = true;
                        break;
                    case DOWN:
                        targetTile = game.findTile(tileMap,tileX,tileY,-1, 0);
                        movingVertical = true;
                        break;
                }
            } else {
                am.switchAnimState("IDLE");
            }
        }

        if (dir != null) {
            switch (dir) {
                case LEFT:
                    imgX -= imgSpeed * time;
                    if (slowed) imgX += slowedAmount * time;

                    if (imgX < targetTile.getImgX() + PlayScreen.TILE_SCALED_SIZE / 2f) {
                        if (tileX != targetTile.getX()) {
                            onNewTile = true;
                            tileX = targetTile.getX();
                        }
                    }

                    if (imgX < targetTile.getImgX()) {
                        // Moved passed the tile, center on the tile
                        imgX = targetTile.getImgX();


                        targetTile = null;
                        movingHorizontal = false;
                        if (!rightInput) pressedButtons.put(Direction.RIGHT, -1L);
                        if (!leftInput) pressedButtons.put(Direction.LEFT, -1L);
                    }
                    break;
                case RIGHT:
                    imgX += imgSpeed * time;
                    if (slowed) imgX -= slowedAmount * time;

                    if (imgX > targetTile.getImgX() - PlayScreen.TILE_SCALED_SIZE / 2f) {
                        if (tileX != targetTile.getX()) {
                            onNewTile = true;
                            tileX = targetTile.getX();
                        }
                    }

                    if (imgX > targetTile.getImgX()) {
                        // Moved passed the tile, center on the tile
                        imgX = targetTile.getImgX();


                        targetTile = null;
                        movingHorizontal = false;
                        if (!rightInput) pressedButtons.put(Direction.RIGHT, -1L);
                        if (!leftInput) pressedButtons.put(Direction.LEFT, -1L);
                    }
                    break;
                case UP:
                    imgY += imgSpeed * time;
                    if (slowed) imgY -= slowedAmount * time;

                    if (imgY > targetTile.getImgY() - PlayScreen.TILE_SCALED_SIZE / 2f) {
                        if (tileY != targetTile.getY()) {
                            onNewTile = true;
                            tileY = targetTile.getY();
                        }
                    }


                    if (imgY > targetTile.getImgY()) {
                        // Moved passed the tile, center on the tile
                        imgY = targetTile.getImgY();


                        targetTile = null;
                        movingVertical = false;
                        if (!upInput) pressedButtons.put(Direction.UP, -1L);
                        if (!downInput) pressedButtons.put(Direction.DOWN, -1L);
                    }
                    break;
                case DOWN:
                    imgY -= imgSpeed * time;
                    if (slowed) imgY += slowedAmount * time;

                    if (imgY < targetTile.getImgY() + PlayScreen.TILE_SCALED_SIZE / 2f) {
                        // Player middle has crossed into new tile
                        if (tileY != targetTile.getY()) {
                            tileY = targetTile.getY();
                            onNewTile = true;
                        }
                    }

                    if (imgY < targetTile.getImgY()) {
                        // Moved passed the tile, center on the tile
                        imgY = targetTile.getImgY();

                        targetTile = null;
                        movingVertical = false;
                        if (!upInput) pressedButtons.put(Direction.UP, -1L);
                        if (!downInput) pressedButtons.put(Direction.DOWN, -1L);
                    }
                    break;
            }
        }
        return onNewTile;
    }

    public void collision(ArrayList<Enemy> enemies) {
        boolean attacked = false;
        for (Enemy e : enemies) {
            if (e.getTileX() == tileX && e.getTileY() == tileY
                    && stats.canBeAttacked() && e.getStats().canBeAttacked()) {
                // Collision!  Perform attack
                int playerHP = stats.getHp();
                int enemyHP = e.getStats().getHp();

                stats.getAttacked(e.getStats());
                e.getStats().getAttacked(stats);
                e.getStats().registerAttack();

                if (!attacked && !stats.isDead() && playerHP != stats.getHp()) {
                    attacked = true;
                    am.switchAnimState("HURT", "RUN");
                    if (e.getType() == Enemy.Type.CBR) {
                        timeSlowed = System.currentTimeMillis();
                        slowed = true;
                    }
                    long id = hurtSound.play();
                    hurtSound.setVolume(id, 0.1f);
                    hurtSound.setPitch(id, 0.8f);
                } else if (!attacked && stats.isDead()) {
                    attacked = true;
                    long id = deathSound.play();
                    deathSound.setVolume(id, 0.1f);
                    deathSound.setPitch(id, 0.6f);
                }

                if (!e.getStats().isDead() && enemyHP != e.getStats().getHp()) {
                    e.getAm().switchAnimState("HURT", "RUN");
                    e.playHurtSound();
                }
            }
        }
        if (attacked) stats.registerAttack();
    }

    private void updateSpray(Tile[][] tileMap) {
        if (!toSpray && !am.getCurrentAnimState().equals("SPRAY")
                && takeInput && Gdx.input.isKeyPressed(Input.Keys.SPACE)
                && System.currentTimeMillis() > lastTimeSprayed + sprayCooldown
                && !stats.isDead()
                && spraysLeft > 0
        ) {
            toSpray = true;
            lastTimeSprayed = System.currentTimeMillis();
            am.switchAnimState("SPRAY", "RUN");
        }
        if (toSpray && System.currentTimeMillis() > lastTimeSprayed+SPRAY_DELAY) {
            toSpray = false;

            long id = spraySound.play();
            spraySound.setPitch(id, 2f);

            // Show spray instance;
            spraysLeft--;

            // Search for enemies along path
            int dir = flipped ? 1 : -1;
            boolean foundEnemy = false;
            int x = dir;
            if (!tileMap[tileY][tileX].getEnemies().isEmpty()) {
                foundEnemy = false;
                for (Enemy e : tileMap[tileY][tileX].getEnemies()) {
                    if (!e.isPassedOut()) {
                        foundEnemy = true;
                        e.setSprayed(true, nuclearSpray);
                    }
                }
            }
            if (!foundEnemy || nuclearSpray) {
                for (; Math.abs(x) <= sprayLength && game.validMove(tileMap, tileX+x, tileY); x+=dir) {
                    ArrayList<Enemy> enemiesInTile = tileMap[tileY][tileX+x].getEnemies();
                    if (!enemiesInTile.isEmpty()) {
                        foundEnemy = false;
                        for (Enemy e : enemiesInTile) {
                            // Mark enemies as sprayed
                            if (!e.isPassedOut()) {
                                foundEnemy = true;
                                e.setSprayed(true, nuclearSpray);
                            }
                        }
                        if (!nuclearSpray && foundEnemy)
                            break;
                    }
                }
                spray.show(flipped, imgX, imgY, Math.abs(x));
            }

            if (!foundEnemy) {
                x += -dir;
                tileMap[tileY][tileX+x].setStinky(true, sprayDuration, 1000f);


                for (int row = -sprayRadius; row <= sprayRadius; row++) {
                    for (int col = x - sprayRadius; col <= x + sprayRadius; col++) {
                        // Spawn stinky tiles in a circle (roughly)
                        double distance = Math.pow(col-x, 2) + Math.pow(row, 2);
                        double alpha = 1.4; // Smooths out the circle

                        if (game.validMove(tileX+col, tileY+row)
                                && distance <= Math.pow(sprayRadius,2)*alpha) {
                            tileMap[tileY+row][tileX+col].setStinky(true, sprayDuration
                                    , 1000f/(float)(distance+1));
                        }
                    }
                }
            }
        }

        spray.update();
    }

    /**
     * @return Whether the player is on a new tile
     */
    public boolean update(Tile[][] tileMap, ArrayList<Enemy> enemies, PlayScreen.SubState state) {
        boolean onNewTile = false;
        am.update();

        if (slowed && System.currentTimeMillis() > timeSlowed + slowedDuration) {
            slowed = false;
        }

        if (stats.isDead()) {
            am.switchAnimState("DEAD");
            am.setOneShot(true);
            if (am.isFinished() && !finishedDeathAnimation) {
                finishedDeathAnimation = true;
                timeFinishedDeathAnimation = System.currentTimeMillis();
            }
        } else if (!am.getCurrentAnimState().equals("SPRAY") && !am.getCurrentAnimState().equals("HURT")) {
            onNewTile = move(tileMap);
            if (!stats.isDead()) collision(enemies);
        }

        if (state == PlayScreen.SubState.COOLDOWN) {
            lastTimeSprayed = System.currentTimeMillis();
        }
        updateSpray(tileMap);

        return onNewTile;
    }

    public void reset() {
        stats.reset();

        tileX = startTileX;
        tileY = startTileY;
        imgX = tileX * PlayScreen.TILE_SCALED_SIZE;
        imgY = tileY * PlayScreen.TILE_SCALED_SIZE;

        targetTile = null;
        dir = null;
        movingVertical = false;
        movingHorizontal = false;

        pressedButtons.put(Direction.UP, -1L);
        pressedButtons.put(Direction.DOWN, -1L);
        pressedButtons.put(Direction.LEFT, -1L);
        pressedButtons.put(Direction.RIGHT, -1L);

        am.switchAnimState("IDLE");
        finishedDeathAnimation = false;

        maxSprays = startMaxSprays;
        sprayRadius = startSprayRadius;
        sprayCooldown = startSprayCooldown;
        sprayDuration = startSprayDuration;
        sprayLength = startSprayLength;

        nuclearSpray = false;

        spraysLeft = maxSprays;
        lastTimeSprayed = System.currentTimeMillis();
    }

    public void eatBerry() {
        spraysLeft = Math.min(spraysLeft+berrySprayRegen, maxSprays);
        stats.setHp(Math.min(stats.getHp()+berryHPRegen, stats.getMaxHP()));
        slowed = false;
        eatSound.play(0.1f, 0.9f, 0f);
    }

    public void startCooldown() {
        spraysLeft = maxSprays;
        stats.setHp(stats.getMaxHP());
        slowed = false;
    }

    public boolean isFinishedDeathAnimation() {
        return finishedDeathAnimation && System.currentTimeMillis() > timeFinishedDeathAnimation + 1000L;
    }

    public void setTakeInput(boolean takeInput) {
        this.takeInput = takeInput;
    }

    public TextureRegion getImg() {
        return am.getCurrentImage(flipped);
    }

    public AnimationManager getAm() {
        return am;
    }

    public float getImgX() {
        return imgX;
    }

    public float getImgY() {
        return imgY;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public Stats getStats() {
        return stats;
    }

    public int getSprayRadius() {
        return sprayRadius;
    }

    public long getSprayDuration() {
        return sprayDuration;
    }

    public void setSprayLength(int sprayLength) {
        this.sprayLength = Math.max(1, sprayLength);
    }

    public int getSprayLength() {
        return sprayLength;
    }

    public int getMaxSprays() {
        return maxSprays;
    }

    public int getSpraysLeft() {
        return spraysLeft;
    }

    public void setSprayRadius(int sprayRadius) {
        this.sprayRadius = Math.max(1, sprayRadius);
    }

    public void setSprayDuration(long sprayDuration) {
        this.sprayDuration = Math.max(0L, sprayDuration);
    }

    public void setSprayCooldown(long sprayCooldown) {
        this.sprayCooldown = Math.max(100L, sprayCooldown);
    }

    public long getSprayCooldown() {
        return sprayCooldown;
    }

    public void setMaxSprays(int maxSprays) {
        this.maxSprays = Math.max(0, maxSprays);
        this.spraysLeft = maxSprays;
    }

    public void setBerryHPRegen(int berryHPRegen) {
        this.berryHPRegen = berryHPRegen;
    }

    public void setBerrySprayRegen(int berrySprayRegen) {
        this.berrySprayRegen = berrySprayRegen;
    }

    public int getBerryHPRegen() {
        return berryHPRegen;
    }

    public int getBerrySprayRegen() {
        return berrySprayRegen;
    }

    public void setNuclearSpray(boolean nuclearSpray) {
        this.nuclearSpray = nuclearSpray;
    }

    public boolean isNuclearSpray() {
        return nuclearSpray;
    }
}
