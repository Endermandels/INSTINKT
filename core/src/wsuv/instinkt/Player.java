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
    private Spray spray;
    private long lastTimeSprayed;
    private long sprayCooldown;

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

        imgX = tileX * PlayScreen.TILE_SCALED_SIZE;
        imgY = tileY * PlayScreen.TILE_SCALED_SIZE;

        imgSpeed = 400f;
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
        spray = new Spray(game);
        gameObjects.add(spray);

        this.startTileX = tileX;
        this.startTileY = tileY;
        this.tileX = tileX;
        this.tileY = tileY;
        targetTile = null;
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
        }

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
                } else if (dir != Direction.RIGHT) {
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
                } else if (dir != Direction.DOWN) {
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
                if (!am.getCurrentAnimState().equals("HURT")) {
                    am.switchAnimState("RUN");
                }
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
                if (!am.getCurrentAnimState().equals("HURT")) {
                    am.switchAnimState("IDLE");
                }
            }
        }

        if (dir != null && !am.getCurrentAnimState().equals("HURT")) {
            switch (dir) {
                case LEFT:
                    imgX -= imgSpeed * time;

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
        for (Enemy e : enemies) {
            if (e.getTileX() == tileX && e.getTileY() == tileY) {
                // Collision!  Perform attack
                int playerHP = stats.getHp();
                int enemyHP = e.getStats().getHp();

                stats.getAttacked(e.getStats());
                e.getStats().getAttacked(stats);

                if (!stats.isDead() && playerHP != stats.getHp()) {
                    am.switchAnimState("HURT", "RUN");
                    long id = hurtSound.play();
                    hurtSound.setVolume(id, 0.1f);
                    hurtSound.setPitch(id, 0.8f);
                } else if (stats.isDead()) {
                    long id = deathSound.play();
                    deathSound.setVolume(id, 0.1f);
                    deathSound.setPitch(id, 0.6f);
                }

                if (!e.getStats().isDead() && enemyHP != e.getStats().getHp()) {
                    e.getAm().switchAnimState("HURT", "RUN");
                    e.playHurtSound();
                }
                break;
            }
        }
    }

    /**
     * @return Whether the player is on a new tile
     */
    public boolean update(Tile[][] tileMap, ArrayList<Enemy> enemies) {
        boolean onNewTile = false;
        am.update();


        // Death
//        if (stats.isDead()) stats.setHp(8); // TODO: Delete

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && System.currentTimeMillis() > lastTimeSprayed + sprayCooldown
            && !stats.isDead()) {
            // Show spray instance
            spray.show(flipped, imgX, imgY);
            lastTimeSprayed = System.currentTimeMillis();
        }

        spray.update();

        if (stats.isDead()) {
            am.switchAnimState("DEAD");
            am.setOneShot(true);
            if (am.isFinished() && !finishedDeathAnimation) {
                finishedDeathAnimation = true;
                timeFinishedDeathAnimation = System.currentTimeMillis();
            }
        } else {
            onNewTile = move(tileMap);
            if (!stats.isDead()) collision(enemies);
        }

        return onNewTile;
    }

    public void drawSpray(Batch batch) {

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
}
