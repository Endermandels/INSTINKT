package wsuv.instinkt;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spawns enemies during each wave.
 * Spawned in specified frequency a specified number of times.
 *
 */
public class EnemySpawner {

    private enum SpawnLocation {
        BLEFT, BRIGHT, TOP;

        public static SpawnLocation fromString(String location) {
            switch (location.toLowerCase()) {
                case "bleft":
                    return BLEFT;
                case "bright":
                    return BRIGHT;
                case "top":
                    return TOP;
                default:
                    throw new IllegalArgumentException("Unknown spawn location: " + location);
            }
        }
    }

    private Game game;
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<GameObject> gameObjects;
    private ArrayList<Integer[]> enemySpawnLocations;
    private EnemyFormation formation;
    private Map<Integer, EnemyFormation> formationsMap = new HashMap<>();

    private long lastSpawn;

    public EnemySpawner(Game game, ArrayList<Enemy> enemies, ArrayList<GameObject> gameObjects, Player player) {
        this.game = game;
        this.player = player;
        this.enemies = enemies;
        this.gameObjects = gameObjects;
        enemySpawnLocations = new ArrayList<>(Arrays.asList(
                new Integer[]{0,-1},
                new Integer[]{0,PlayScreen.TILE_COLS},
                new Integer[]{PlayScreen.TILE_ROWS,PlayScreen.TILE_COLS-1}
        ));

        readFormationsFile();
        formation = formationsMap.get(0);

        lastSpawn = 0L;
    }

    private void readFormationsFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("Text/enemy_formations.txt"))) {
            String line;
            Integer currentFrequency = null;
            ArrayList<Enemy> currentEnemiesToSpawn = new ArrayList<>();

            int idx = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // If the line contains only digits, it's a frequency
                if (line.matches("\\d+")) {
                    if (currentFrequency != null) {
                        // Store the previous formation before starting a new one
                        formationsMap.put(idx++, new EnemyFormation(currentFrequency, new ArrayList<>(currentEnemiesToSpawn)));
                    }
                    // Start a new formation
                    currentFrequency = Integer.parseInt(line);
                    currentEnemiesToSpawn.clear();
                } else {
                    // Split the line and extract the spawn location (ignoring the enemy type for now)
                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        String enemyType = parts[0];
                        String spawnLocationStr = parts[1];
                        SpawnLocation spawnLocation = SpawnLocation.fromString(spawnLocationStr);
                        Enemy.Type type = Enemy.Type.fromString(enemyType);
                        currentEnemiesToSpawn.add(newEnemyAtLocation(spawnLocation, type));
                    }
                }
            }

            // Store the last formation
            if (currentFrequency != null) {
                formationsMap.put(idx, new EnemyFormation(currentFrequency, currentEnemiesToSpawn));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.exit();
            System.exit(-1);
        }
    }

    private Enemy newEnemyAtLocation(SpawnLocation sl, Enemy.Type type) {
        Enemy.Direction dir = Enemy.Direction.RIGHT;
        switch (sl) {
            case BLEFT:
                dir = Enemy.Direction.RIGHT;
                break;
            case BRIGHT:
                dir = Enemy.Direction.LEFT;
                break;
            case TOP:
                dir = Enemy.Direction.DOWN;
                break;
        }
        return new Enemy(game, enemySpawnLocations.get(sl.ordinal())[1]
                ,enemySpawnLocations.get(sl.ordinal())[0], dir, type, enemySpawnLocations, player);
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

    public void setFormation(int idx) {
        lastSpawn = System.currentTimeMillis();
        formation = formationsMap.get(idx).reset();
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
        return idx < enemiesToSpawn.size() ? enemiesToSpawn.get(idx++).clone() : null;
    }

    public long getFrequency() {
        return frequency;
    }

    public EnemyFormation reset() {
        idx = 0;
        return this;
    }
}
