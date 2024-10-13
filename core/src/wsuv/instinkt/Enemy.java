package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
    private Player player;
    private AnimationManager am;
    private Stats stats;
    private Type type;
    private Tile.DistanceType targetType;
    private ArrayList<Integer[]> enemySpawnLocations;

    // SFX
    private Sound hurtSound;
    private Sound deathSound;
    private Sound sprayedSound;

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
    private boolean wasDead;

    private boolean sprayed;

    public Enemy(Game game, int tileX, int tileY, Direction dir, Type type,
                 ArrayList<Integer[]> enemySpawnLocations, Player player) {
        super(null, 0, 0, 20);
        this.game = game;
        this.type = type;
        this.player = player;
        this.enemySpawnLocations = enemySpawnLocations;

        hurtSound = null;
        deathSound = null;
        sprayedSound = null;

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

                // Sounds
                hurtSound = game.am.get(Game.RSC_SQUIRREL_NOISE_SFX);
                deathSound = game.am.get(Game.RSC_SQUIRREL_NOISE_2_SFX);
                sprayedSound = game.am.get(Game.RSC_SQUIRREL_NOISE_3_SFX);

                // Stats
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

                // Sounds
                deathSound = game.am.get(Game.RSC_SQUIRREL_NOISE_2_SFX);
                sprayedSound = game.am.get(Game.RSC_SQUIRREL_NOISE_3_SFX);

                // Stats
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

                // Sounds
                hurtSound = game.am.get(Game.RSC_SNAKE_NOISE_SFX);
                deathSound = game.am.get(Game.RSC_SNAKE_NOISE_2_SFX);

                // Stats
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
        wasDead = false;

        sprayed = false;

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
                if ((movingHorizontal && pathY == tile.getY())
                        || (movingVertical && pathX == tile.getX())
                        || (!movingHorizontal && !movingVertical)) {
                    if (targetType != Tile.DistanceType.EXIT) {
                        if (tile.getDistance(targetType) < lowestTile.getDistance(targetType)) {
                            lowestTile = tile;
                        }
                    } else {
                        // Avoid player
                        float exitDist1 = tile.getDistance(targetType);
                        float playerDist1 = tile.getDistance(Tile.DistanceType.PLAYER);
                        float exitDist2 = lowestTile.getDistance(targetType);
                        float playerDist2 = lowestTile.getDistance(Tile.DistanceType.PLAYER);
                        float alpha = 0.6f;
                        if (exitDist1*alpha - playerDist1*(1-alpha) < exitDist2*alpha - playerDist2*(1-alpha)) {
                            lowestTile = tile;
                        }
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

        if (targetType == Tile.DistanceType.EXIT && game.validMove(tileX, tileY)
                && tileMap[tileY][tileX].getDistance(targetType) == 1f) {
            if (isSpawnTile(tileX, tileY+1)) dir = Direction.UP;
            if (isSpawnTile(tileX, tileY-1)) dir = Direction.DOWN;
            if (isSpawnTile(tileX-1, tileY)) dir = Direction.LEFT;
            if (isSpawnTile(tileX+1, tileY)) dir = Direction.RIGHT;
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

        int prevTileX = tileX;
        int prevTileY = tileY;

        tileX = (int) (imgX + PlayScreen.TILE_SCALED_SIZE / 2f) / PlayScreen.TILE_SCALED_SIZE;
        tileY = (int) (imgY + PlayScreen.TILE_SCALED_SIZE / 2f) / PlayScreen.TILE_SCALED_SIZE;

        // Each tile keeps track of which enemies are in it.
        if (prevTileX != tileX || prevTileY != tileY) {
            if (game.validMove(prevTileX, prevTileY))
                tileMap[prevTileY][prevTileX].getEnemies().remove(this);
            if (game.validMove(tileX, tileY))
                tileMap[tileY][tileX].getEnemies().add(this);
        }

        return toRemove;
    }

    public boolean update(Tile[][] tileMap) {
        boolean toRemove = false;

        am.update();

        // Update stinky tiles
        if (sprayed) {
            for (int i = -player.getSprayRadius(); i <= player.getSprayRadius(); i++) {
                for (int j = -player.getSprayRadius(); j <= player.getSprayRadius(); j++) {
                    if (game.validMove(tileX+i, tileY+j)) {
                        tileMap[tileY+j][tileX+i].setStinky(true, player.getSprayDuration());
                    }
                }
            }
        }

        if (type != Type.CBR && game.validMove(tileX, tileY) && tileMap[tileY][tileX].isStinky()) {
            targetType = Tile.DistanceType.EXIT;
        }

        if (stats.isDead()) {
            if (!wasDead) {
                playDeathSound();
                wasDead = true;
            }
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
            if (move(tileMap)) {
                toRemove = true;
            }
        }
        return toRemove;
    }

    public TextureRegion getImg() {
        return am.getCurrentImage(flipped);
    }

    public void playHurtSound() {
        if (hurtSound != null) {
            long id = hurtSound.play();
            switch (type) {
                case FOX:
                    hurtSound.setVolume(id, 0.1f);
                    hurtSound.setPitch(id, 0.6f);
                    break;
                case CBR:
                    hurtSound.setVolume(id, 0.7f);
                    hurtSound.setPitch(id, 4f);
                    break;
            }
        }
    }

    public void playDeathSound() {
        if (deathSound != null) {
            long id = deathSound.play();
            switch (type) {
                case FOX:
                    deathSound.setVolume(id, 0.1f);
                    deathSound.setPitch(id, 0.9f);
                    break;
                case SQL:
                    deathSound.setVolume(id, 0.1f);
                    deathSound.setPitch(id, 1f);
                    break;
                case CBR:
                    deathSound.setVolume(id, 1f);
                    deathSound.setPitch(id, 1f);
                    break;
            }
        }
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

    public void setSprayed(boolean sprayed) {
        this.sprayed = sprayed;
        if (sprayedSound != null && sprayed && !stats.isDead()) {
            long id = sprayedSound.play();
            switch (type) {
                case FOX:
                    sprayedSound.setVolume(id, 2f);
                    sprayedSound.setPitch(id, 2f);
                    break;
                case SQL:
                    sprayedSound.setVolume(id, 2f);
                    sprayedSound.setPitch(id, 3f);
                    break;
            }
        }
    }

    public Enemy clone() {
        return new Enemy(game, tileX, tileY, dir, type, enemySpawnLocations, player);
    }
}
