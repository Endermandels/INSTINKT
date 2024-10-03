package wsuv.instinkt;

import java.util.ArrayList;
import java.util.Arrays;

public class EnemySpawner {

    private Game game;
    private ArrayList<Enemy> enemies;
    private ArrayList<GameObject> gameObjects;
    private ArrayList<Integer[]> enemySpawnLocations;

    public EnemySpawner(Game game, ArrayList<Enemy> enemies, ArrayList<GameObject> gameObjects) {
        this.game = game;
        this.enemies = enemies;
        this.gameObjects = gameObjects;
        enemySpawnLocations = new ArrayList<>(Arrays.asList(
                new Integer[]{0,-1},
                new Integer[]{0,PlayScreen.TILE_COLS},
                new Integer[]{PlayScreen.TILE_ROWS,PlayScreen.TILE_COLS-1}
        ));
    }

    private void spawnEnemy(int spawnLocationIdx) {
        enemies.add(new Enemy(game, enemySpawnLocations.get(spawnLocationIdx)[1]
                ,enemySpawnLocations.get(spawnLocationIdx)[0], enemySpawnLocations));
        gameObjects.add(enemies.get(0));
    }



}
