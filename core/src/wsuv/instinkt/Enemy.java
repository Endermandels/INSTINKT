package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

public class Enemy extends GameObject {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Game game;
    private AnimationManager am;
    private Stats stats;
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

    public Enemy(Game game, int tileX, int tileY, Direction dir, ArrayList<Integer[]> enemySpawnLocations) {
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

        stats = new Stats(50, 10, 400L);

        imgX = tileX * PlayScreen.TILE_SCALED_SIZE;
        imgY = tileY * PlayScreen.TILE_SCALED_SIZE;

        imgSpeed = 400f;
        movingHorizontal = false;
        movingVertical = false;

        this.dir = dir;
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


    private ArrayList<Tile> getNeighbors(Tile[][] tileMap, Tile tile) {
        ArrayList<Tile> neighbors = new ArrayList<>();

        if (game.validMove(tileMap,tile.getX()-1, tile.getY()))
            neighbors.add(tileMap[tile.getY()][tile.getX()-1]);
        if (game.validMove(tileMap,tile.getX()+1, tile.getY()))
            neighbors.add(tileMap[tile.getY()][tile.getX()+1]);
        if (game.validMove(tileMap, tile.getX(), tile.getY()-1))
            neighbors.add(tileMap[tile.getY()-1][tile.getX()]);
        if (game.validMove(tileMap, tile.getX(), tile.getY()+1))
            neighbors.add(tileMap[tile.getY()+1][tile.getX()]);

        return neighbors;
    }

    /**
     * @return Whether the enemy should be removed
     */
    private boolean move(Tile[][] tileMap) {
        float time = Gdx.graphics.getDeltaTime();
        if (time > 1f) time = 1f / 60f;

        if (tileX >= 0 && tileX < PlayScreen.TILE_COLS && tileY >= 0 && tileY < PlayScreen.TILE_ROWS) {
            Tile currentTile = tileMap[tileY][tileX];
            Tile lowestTile = currentTile;
            for (Tile tile : getNeighbors(tileMap, currentTile)) {
                if (tile.getDistance(Tile.DistanceType.PLAYER) < lowestTile.getDistance(Tile.DistanceType.PLAYER)) {
                    if ((movingHorizontal && tileY == tile.getY())
                            || (movingVertical && tileX == tile.getX())
                            || (!movingHorizontal && !movingVertical)) {
                        lowestTile = tile;
                    }
                }
            }
            if (movingHorizontal && tileY == lowestTile.getY()) {
                if (lowestTile.getX() < tileX) dir = Direction.LEFT;
                else dir = Direction.RIGHT;
            } else if (movingVertical && tileX == lowestTile.getX()) {
                if (lowestTile.getY() < tileY) dir = Direction.DOWN;
                else dir = Direction.UP;
            } else if (!movingHorizontal && !movingVertical) {
                if (lowestTile.getX() < tileX) dir = Direction.LEFT;
                else if (lowestTile.getX() > tileX) dir = Direction.RIGHT;
                else if (lowestTile.getY() < tileY) dir = Direction.DOWN;
                else if (lowestTile.getY() > tileY) dir = Direction.UP;
                else dir = null;
            }
        }

        if (dir != null) {
            am.switchAnimState("RUN");
            switch (dir) {
                case LEFT:
                    if (game.validMove(tileMap, tileX - 1, tileY) || isSpawnTile(tileX-1, tileY)) {
                        targetPos[0] = tileX - 1;
                        movingHorizontal = true;
                        flipped = true;
                    }
                    break;
                case RIGHT:
                    if (game.validMove(tileMap, tileX + 1, tileY) || isSpawnTile(tileX+1, tileY)) {
                        targetPos[0] = tileX + 1;
                        movingHorizontal = true;
                        flipped = false;
                    }
                    break;
                case UP:
                    if (game.validMove(tileMap, tileX, tileY + 1) || isSpawnTile(tileX, tileY+1)) {
                        targetPos[1] = tileY + 1;
                        movingVertical = true;
                    }
                    break;
                case DOWN:
                    if (game.validMove(tileMap, tileX, tileY - 1) || isSpawnTile(tileX, tileY-1)) {
                        targetPos[1] = tileY - 1;
                        movingVertical = true;
                    }
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
        boolean toRemove = false;

        am.update();
        if (move(tileMap)) toRemove = true;
        if (stats.getHp() <= 0) toRemove = true;
        return toRemove;
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

    public Stats getStats() {
        return stats;
    }
}
