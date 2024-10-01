package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Enemy extends GameObject {
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Game game;
    private AnimationManager am;
    private ArrayList<Integer[]> enemySpawnLocations;

    private float imgX;
    private float imgY;
    private float imgSpeed;

    private int tileX;
    private int tileY;

    private int[] targetPos;
    private boolean movingHorizontal;
    private boolean movingVertical;
    private boolean flipped;
    private Direction dir;

    public Enemy(Game game, int tileX, int tileY, ArrayList<Integer[]> enemySpawnLocations) {
        super(null, 0, 0, 20);
        this.game = game;
        this.enemySpawnLocations = enemySpawnLocations;
        am = new AnimationManager(game.am.get(Game.RSC_SS_FOX_IMG)
                , new ArrayList<Integer>(Arrays.asList(5,14,8,11,5,6,7))
                , new HashMap<String, Integer>() {{
                    put("IDLE", 0);
                    put("LOOK", 1);
                    put("RUN", 2);
                    put("POUNCE", 3);
                    put("HURT", 4);
                    put("SLEEP", 5);
                    put("DEAD", 6);
                }}
                , 0.08f, 32, 32
        );

        imgX = tileX * PlayScreen.TILE_SCALED_SIZE;
        imgY = tileY * PlayScreen.TILE_SCALED_SIZE;

        imgSpeed = 400f;
        movingHorizontal = false;
        movingVertical = false;

        dir = Direction.RIGHT;
        flipped = false;

        this.tileX = tileX;
        this.tileY = tileY;
        targetPos = new int[2];
    }

    private boolean isSpawnTile(int tileX, int tileY) {
        for (Integer[] location : enemySpawnLocations) {
            if (location[1] == tileX && location[0] == tileY) return true;
        }
        return false;
    }

    /**
     *
     * @param tileMap
     * @return Whether the enemy should be removed
     */
    private boolean move(Tile[][] tileMap) {
        float time = Gdx.graphics.getDeltaTime();
        if (time > 1f) time = 1f / 60f;

        if (dir != null) {
            am.switchAnimState("RUN");
            switch (dir) {
                case LEFT:
                    if (game.validMove(tileMap, tileX - 1, tileY) || isSpawnTile(tileX-1, tileY))
                        targetPos[0] = tileX - 1;
                    movingHorizontal = true;
                    flipped = true;
                    break;
                case RIGHT:
                    if (game.validMove(tileMap, tileX + 1, tileY) || isSpawnTile(tileX+1, tileY))
                        targetPos[0] = tileX + 1;
                    movingHorizontal = true;
                    flipped = false;
                    break;
                case UP:
                    if (game.validMove(tileMap, tileX, tileY + 1) || isSpawnTile(tileX, tileY+1))
                        targetPos[1] = tileY + 1;
                    movingVertical = true;
                    break;
                case DOWN:
                    if (game.validMove(tileMap, tileX, tileY - 1) || isSpawnTile(tileX, tileY-1))
                        targetPos[1] = tileY - 1;
                    movingVertical = true;
                    break;
            }
        } else {
            am.switchAnimState("IDLE");
        }

        if (dir != null) {
            switch (dir) {
                case LEFT:
                    imgX -= imgSpeed * time;

                    if (imgX < targetPos[0] * PlayScreen.TILE_SCALED_SIZE) {
                        // Moved passed the tile, center on the tile
                        imgX = targetPos[0] * PlayScreen.TILE_SCALED_SIZE;
                        tileX = targetPos[0];

                        movingHorizontal = false;
                    }
                    break;
                case RIGHT:
                    imgX += imgSpeed * time;

                    if (imgX > targetPos[0]*PlayScreen.TILE_SCALED_SIZE) {
                        // Moved passed the tile, center on the tile
                        imgX = targetPos[0]*PlayScreen.TILE_SCALED_SIZE;
                        tileX = targetPos[0];

                        movingHorizontal = false;
                        if (isSpawnTile(tileX, tileY)) return true;
                    }
                    break;
                case UP:
                    imgY += imgSpeed * time;

                    if (imgY > targetPos[1] * PlayScreen.TILE_SCALED_SIZE) {
                        // Moved passed the tile, center on the tile
                        imgY = targetPos[1]*PlayScreen.TILE_SCALED_SIZE;
                        tileY = targetPos[1];

                        movingVertical = false;
                    }
                    break;
                case DOWN:
                    imgY -= imgSpeed * time;

                    if (imgY < targetPos[1] * PlayScreen.TILE_SCALED_SIZE) {
                        // Moved passed the tile, center on the tile
                        imgY = targetPos[1] * PlayScreen.TILE_SCALED_SIZE;
                        tileY = (int) targetPos[1];

                        movingVertical = false;
                    }
                    break;
            }
        }
        return false;
    }

    public boolean update(Tile[][] tileMap) {
        am.update();
        return move(tileMap);
    }

    public TextureRegion getImg() {
        return am.getCurrentImage(flipped);
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
}
