package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

public class Enemy extends GameObject {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public enum Type {
        FOX, // Fox
        SQL, // Squirrel
        CBR; // Cobra

        public static Enemy.Type fromString(String type) {
            switch (type.toLowerCase()) {
                case "fox":
                    return FOX;
                case "sql":
                    return SQL;
                case "cbr":
                    return CBR;
                default:
                    throw new IllegalArgumentException("Unknown enemy type: " + type);
            }
        }
    }

    private Game game;
    private AnimationManager am;
    private Stats stats;
    private Type type;
    private Tile.DistanceType targetType;
    private ArrayList<Integer[]> enemySpawnLocations;

    private float imgX;
    private float imgY;
    private float imgSpeed;

    private int tileX;
    private int tileY;
    private int pathX;
    private int pathY;

    private int[] targetPos;
    private boolean movingHorizontal;
    private boolean movingVertical;
    private boolean flipped;
    private Direction dir;

    private boolean finishedDeathAnimation;
    private long timeFinishedDeathAnimation;

    public Enemy(Game game, int tileX, int tileY, Direction dir, Type type,
                 ArrayList<Integer[]> enemySpawnLocations) {
        super(null, 0, 0, 20);
        this.game = game;
        this.type = type;
        this.enemySpawnLocations = enemySpawnLocations;

        switch (type) {
            case FOX:
                am = new AnimationManager(game.am.get(Game.RSC_SS_FOX_IMG)
                        , new ArrayList<>(Arrays.asList(5,14,8,11,5,6,7))
                        , new HashMap<>() {{
                    put("IDLE", 0);
                    put("LOOK", 1);
                    put("RUN", 2);
                    put("ATTACK", 3);
                    put("HURT", 4);
                    put("SLEEP", 5);
                    put("DEAD", 6);
                }}
                        , 0.08f, 32, 32
                );

                stats = new Stats(3, 2, 800L);
                imgSpeed = 400f;
                targetType = Tile.DistanceType.PLAYER;
                break;
            case SQL:
                am = new AnimationManager(game.am.get(Game.RSC_SS_SQUIRREL_IMG)
                        , new ArrayList<>(Arrays.asList(6,6,8,4,2,4,4))
                        , new HashMap<>() {{
                    put("IDLE", 0);
                    put("LOOK", 1);
                    put("RUN", 2);
                    put("ATTACK", 3);
                    put("EAT", 4);
                    put("HURT", 5);
                    put("DEAD", 6);
                }}
                        , 0.08f, 32, 32
                );

                stats = new Stats(1, 0, 0L);
                imgSpeed = 500f;
                targetType = Tile.DistanceType.BERRIES;
                break;
            case CBR:
                am = new AnimationManager(game.am.get(Game.RSC_SS_COBRA_IMG)
                        , new ArrayList<>(Arrays.asList(8,8,6,4,6))
                        , new HashMap<>() {{
                    put("IDLE", 0);
                    put("RUN", 1);
                    put("ATTACK", 2);
                    put("HURT", 3);
                    put("DEAD", 4);
                }}
                        , 0.08f, 32, 32
                );

                stats = new Stats(4, 1, 800L);
                imgSpeed = 100f;
                targetType = Tile.DistanceType.PLAYER;
                break;
        }

        imgX = tileX * PlayScreen.TILE_SCALED_SIZE;
        imgY = tileY * PlayScreen.TILE_SCALED_SIZE;

        movingHorizontal = false;
        movingVertical = false;

        finishedDeathAnimation = false;
        timeFinishedDeathAnimation = -1L;

        this.dir = dir;
        flipped = false;

        this.tileX = tileX;
        this.tileY = tileY;
        pathX = tileX;
        pathY = tileY;
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
        boolean toRemove = false;

        float time = Gdx.graphics.getDeltaTime();
        if (time > 1f) time = 1f / 60f;

        if (pathX >= 0 && pathX < PlayScreen.TILE_COLS && pathY >= 0 && pathY < PlayScreen.TILE_ROWS) {
            Tile currentTile = tileMap[pathY][pathX];
            Tile lowestTile = currentTile;
            for (Tile tile : getNeighbors(tileMap, currentTile)) {
                if (tile.getDistance(targetType) < lowestTile.getDistance(targetType)) {
                    if ((movingHorizontal && pathY == tile.getY())
                            || (movingVertical && pathX == tile.getX())
                            || (!movingHorizontal && !movingVertical)) {
                        lowestTile = tile;
                    }
                }
            }
            if (movingHorizontal && pathY == lowestTile.getY()) {
                if (lowestTile.getX() < pathX) dir = Direction.LEFT;
                else dir = Direction.RIGHT;
            } else if (movingVertical && pathX == lowestTile.getX()) {
                if (lowestTile.getY() < pathY) dir = Direction.DOWN;
                else dir = Direction.UP;
            } else if (!movingHorizontal && !movingVertical) {
                if (lowestTile.getX() < pathX) dir = Direction.LEFT;
                else if (lowestTile.getX() > pathX) dir = Direction.RIGHT;
                else if (lowestTile.getY() < pathY) dir = Direction.DOWN;
                else if (lowestTile.getY() > pathY) dir = Direction.UP;
                else dir = null;
            }
        }

        if (dir != null) {
            if (!am.getCurrentAnimState().equals("HURT")) {
                am.switchAnimState("RUN");
            }
            switch (dir) {
                case LEFT:
                    if (game.validMove(tileMap, pathX - 1, pathY) || isSpawnTile(pathX-1, pathY)) {
                        targetPos[0] = pathX - 1;
                        movingHorizontal = true;
                        flipped = true;
                    }
                    break;
                case RIGHT:
                    if (game.validMove(tileMap, pathX + 1, pathY) || isSpawnTile(pathX+1, pathY)) {
                        targetPos[0] = pathX + 1;
                        movingHorizontal = true;
                        flipped = false;
                    }
                    break;
                case UP:
                    if (game.validMove(tileMap, pathX, pathY + 1) || isSpawnTile(pathX, pathY+1)) {
                        targetPos[1] = pathY + 1;
                        movingVertical = true;
                    }
                    break;
                case DOWN:
                    if (game.validMove(tileMap, pathX, pathY - 1) || isSpawnTile(pathX, pathY-1)) {
                        targetPos[1] = pathY - 1;
                        movingVertical = true;
                    }
                    break;
            }
        } else {
            if (!am.getCurrentAnimState().equals("HURT")) {
                am.switchAnimState("IDLE");
            }
        }

        if (dir != null && !am.getCurrentAnimState().equals("HURT")) {
            switch (dir) {
                case LEFT:
                    imgX -= imgSpeed * time;

                    if (imgX < targetPos[0] * PlayScreen.TILE_SCALED_SIZE) {
                        // Moved passed the tile, center on the tile
                        imgX = targetPos[0] * PlayScreen.TILE_SCALED_SIZE;
                        pathX = targetPos[0];

                        movingHorizontal = false;
                        if (isSpawnTile(pathX, pathY)) toRemove = true;
                    }
                    break;
                case RIGHT:
                    imgX += imgSpeed * time;

                    if (imgX > targetPos[0]*PlayScreen.TILE_SCALED_SIZE) {
                        // Moved passed the tile, center on the tile
                        imgX = targetPos[0]*PlayScreen.TILE_SCALED_SIZE;
                        pathX = targetPos[0];

                        movingHorizontal = false;
                        if (isSpawnTile(pathX, pathY)) toRemove = true;
                    }
                    break;
                case UP:
                    imgY += imgSpeed * time;

                    if (imgY > targetPos[1] * PlayScreen.TILE_SCALED_SIZE) {
                        // Moved passed the tile, center on the tile
                        imgY = targetPos[1]*PlayScreen.TILE_SCALED_SIZE;
                        pathY = targetPos[1];

                        movingVertical = false;
                        if (isSpawnTile(pathX, pathY)) toRemove = true;
                    }
                    break;
                case DOWN:
                    imgY -= imgSpeed * time;

                    if (imgY < targetPos[1] * PlayScreen.TILE_SCALED_SIZE) {
                        // Moved passed the tile, center on the tile
                        imgY = targetPos[1] * PlayScreen.TILE_SCALED_SIZE;
                        pathY = targetPos[1];

                        movingVertical = false;
                        if (isSpawnTile(pathX, pathY)) toRemove = true;
                    }
                    break;
            }
        }

        tileX = (int) (imgX + PlayScreen.TILE_SCALED_SIZE / 2f) / PlayScreen.TILE_SCALED_SIZE;
        tileY = (int) (imgY + PlayScreen.TILE_SCALED_SIZE / 2f) / PlayScreen.TILE_SCALED_SIZE;

        return toRemove;
    }

    public boolean update(Tile[][] tileMap) {
        boolean toRemove = false;

        am.update();
//        if (stats.isDead()) stats.setHp(8); // TODO: Delete

        if (stats.isDead()) {
            am.switchAnimState("DEAD");
            am.setOneShot(true);
            if (am.isFinished()) {
                if(!finishedDeathAnimation) {
                    finishedDeathAnimation = true;
                    timeFinishedDeathAnimation = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() > timeFinishedDeathAnimation + 1000L) toRemove = true;
            }
        } else {
            if (move(tileMap)) toRemove = true;
        }
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

    public AnimationManager getAm() {
        return am;
    }

    public Stats getStats() {
        return stats;
    }

    public Enemy clone() {
        return new Enemy(game, tileX, tileY, dir, type, enemySpawnLocations);
    }
}
