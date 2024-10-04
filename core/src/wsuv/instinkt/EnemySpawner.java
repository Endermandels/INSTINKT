package wsuv.instinkt;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Spawns enemies during each wave.
 * Spawned in specified frequency a specified number of times.
 *
 */
public class EnemySpawner {

    private enum SpawnLocation {BLEFT, BRIGHT, TOP}

    private Game game;
    private ArrayList<Enemy> enemies;
    private ArrayList<GameObject> gameObjects;
    private ArrayList<Integer[]> enemySpawnLocations;
    private EnemyFormation formation;

    private long lastSpawn;

    public EnemySpawner(Game game, ArrayList<Enemy> enemies, ArrayList<GameObject> gameObjects) {
        this.game = game;
        this.enemies = enemies;
        this.gameObjects = gameObjects;
        enemySpawnLocations = new ArrayList<>(Arrays.asList(
                new Integer[]{0,-1},
                new Integer[]{0,PlayScreen.TILE_COLS},
                new Integer[]{PlayScreen.TILE_ROWS,PlayScreen.TILE_COLS-1}
        ));

        formation = new EnemyFormation(1000L, new ArrayList<>(Arrays.asList(
                newEnemyAtLocation(SpawnLocation.BLEFT),
                newEnemyAtLocation(SpawnLocation.BLEFT),
                newEnemyAtLocation(SpawnLocation.BLEFT),
                newEnemyAtLocation(SpawnLocation.BRIGHT)
        )));

        lastSpawn = 0L;
    }

    private Enemy newEnemyAtLocation(SpawnLocation sl) {
        return new Enemy(game, enemySpawnLocations.get(sl.ordinal())[1]
                ,enemySpawnLocations.get(sl.ordinal())[0], enemySpawnLocations);
    }

    private void spawnEnemy(Enemy enemy) {
        enemies.add(enemy);
        gameObjects.add(enemy);
    }

    public void update() {
        long curTime = System.currentTimeMillis();
        if (curTime - lastSpawn > formation.getFrequency()) {
            lastSpawn = curTime;
            Enemy enemy = formation.getNextEnemy();
            if (enemy != null) spawnEnemy(enemy);
        }
    }

}

class EnemyFormation {

    private long frequency;
    private ArrayList<Enemy> enemiesToSpawn;
    private int idx;

    public EnemyFormation(long frequency, ArrayList<Enemy> enemiesToSpawn) {
        this.frequency = frequency;
        this.enemiesToSpawn = enemiesToSpawn;
        this.idx = 0;
    }

    public Enemy getNextEnemy() {
        return idx < enemiesToSpawn.size() ? enemiesToSpawn.get(idx++) : null;
    }

    public long getFrequency() {
        return frequency;
    }
}
