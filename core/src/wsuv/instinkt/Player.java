package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

public class Player extends GameObject {

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Game game;
    private AnimationManager am;
    private Stats stats;

    // Track the position of the player's image separately from tile coordinates
    private float imgX;
    private float imgY;
    private float imgSpeed;

    // Tile coordinates
    private int tileX;
    private int tileY;

    private Tile targetTile; // When the player is moving
    private boolean movingHorizontal;
    private boolean movingVertical;
    private boolean flipped;
    private Direction dir;
    private Map<Direction, Long> pressedButtons; // Direction key, timestamp value (-1 if not pressed)

    private boolean takeInput;

    public Player(Game game, int tileX, int tileY) {
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

        stats = new Stats(100, 20, 400L);

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
                if (pressedButtons.get(Direction.LEFT) > pressedButtons.get(Direction.RIGHT)) {
                    targetTile = game.findTile(tileMap,tileX,tileY,0, -1);
                    dir = Direction.LEFT;
                    flipped = true;
                } else {
                    targetTile = game.findTile(tileMap,tileX,tileY,0, 1);
                    dir = Direction.RIGHT;
                    flipped = false;
                }
            } else if (leftInput) {
                dir = Direction.LEFT;
                targetTile = game.findTile(tileMap,tileX,tileY,0, -1);
                flipped = true;
            } else if (rightInput) {
                dir = Direction.RIGHT;
                targetTile = game.findTile(tileMap,tileX,tileY,0, 1);
                flipped = false;
            }
        } else if (movingVertical) {
            // Vertical movement only
            if (upInput && downInput) {
                // Choose most recent
                if (pressedButtons.get(Direction.UP) > pressedButtons.get(Direction.DOWN)) {
                    targetTile = game.findTile(tileMap,tileX,tileY,1, 0);
                    dir = Direction.UP;
                } else {
                    targetTile = game.findTile(tileMap,tileX,tileY,-1, 0);
                    dir = Direction.DOWN;
                }
            } else if (upInput) {
                dir = Direction.UP;
                targetTile = game.findTile(tileMap,tileX,tileY,1, 0);
            } else if (downInput) {
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

                    if (imgX < targetTile.getImgX()) {
                        // Moved passed the tile, center on the tile
                        imgX = targetTile.getImgX();
                        if (tileX != targetTile.getX()) {
                            onNewTile = true;
                            tileX = targetTile.getX();
                        }

                        targetTile = null;
                        movingHorizontal = false;
                        if (!rightInput) pressedButtons.put(Direction.RIGHT, -1L);
                        if (!leftInput) pressedButtons.put(Direction.LEFT, -1L);
                    }
                    break;
                case RIGHT:
                    imgX += imgSpeed * time;

                    if (imgX > targetTile.getImgX()) {
                        // Moved passed the tile, center on the tile
                        imgX = targetTile.getImgX();
                        if (tileX != targetTile.getX()) {
                            onNewTile = true;
                            tileX = targetTile.getX();
                        }

                        targetTile = null;
                        movingHorizontal = false;
                        if (!rightInput) pressedButtons.put(Direction.RIGHT, -1L);
                        if (!leftInput) pressedButtons.put(Direction.LEFT, -1L);
                    }
                    break;
                case UP:
                    imgY += imgSpeed * time;

                    if (imgY > targetTile.getImgY()) {
                        // Moved passed the tile, center on the tile
                        imgY = targetTile.getImgY();
                        if (tileY != targetTile.getY()) {
                            onNewTile = true;
                            tileY = targetTile.getY();
                        }

                        targetTile = null;
                        movingVertical = false;
                        if (!upInput) pressedButtons.put(Direction.UP, -1L);
                        if (!downInput) pressedButtons.put(Direction.DOWN, -1L);
                    }
                    break;
                case DOWN:
                    imgY -= imgSpeed * time;

                    if (imgY < targetTile.getImgY()) {
                        // Moved passed the tile, center on the tile
                        imgY = targetTile.getImgY();
                        if (tileY != targetTile.getY()) {
                            onNewTile = true;
                            tileY = targetTile.getY();
                        }

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
                stats.getAttacked(e.getStats());
                e.getStats().getAttacked(stats);
                break;
            }
        }
    }

    /**
     * @return Whether the player is on a new tile
     */
    public boolean update(Tile[][] tileMap, ArrayList<Enemy> enemies) {
        boolean onNewTile;
        am.update();
        onNewTile = move(tileMap);
        collision(enemies);
        return onNewTile;
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
