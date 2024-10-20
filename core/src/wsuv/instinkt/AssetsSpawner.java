package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Spawns assets such as tiles and gameObjects using text files.
 */
public class AssetsSpawner {
    Game game;
    Tile[][] tileMap;
    ArrayList<GameObject> gameObjects;
    HashMap<String, HashMap<Integer, ArrayList<Integer[]>>> ssMaps;
    ArrayList<Integer[]> importantLocations;

    public AssetsSpawner(Game game, Tile[][] tileMap, ArrayList<GameObject> gameObjects) {
        this.game = game;
        this.tileMap = tileMap;
        this.gameObjects = gameObjects;
        ssMaps = new HashMap<>();
        importantLocations = new ArrayList<>();
    }


    /**
     * Reads from text file to determine which tile sprites are drawn.
     * Text file is formated where every tile is a pair of integers
     *      and the tiles are separated by commas.
     */
    private void populateTileMap() {
        FileHandle tileMapFile = Gdx.files.internal("Text/tile_map.txt");

        try (BufferedReader br = tileMapFile.reader(100000)) {
            String line;
            char txtRow;
            char txtCol;
            int y = PlayScreen.TILE_ROWS-1;
            int ssRow, ssCol;

            while ((line = br.readLine()) != null && y >= 0) {
                for (int x = 0; x < line.length(); x+=3) {
                    txtRow = line.charAt(x);
                    txtCol = line.charAt(x+1);
                    ssRow = Character.getNumericValue(txtRow);
                    ssCol = Character.getNumericValue(txtCol);

                    tileMap[y][x/3] = new Tile(game, x/3, y
                            , (x/3f)*PlayScreen.TILE_SCALED_SIZE, y*PlayScreen.TILE_SCALED_SIZE
                            , ssRow, ssCol);
                }
                y--;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.exit();
            System.exit(-1);
        }
    }

    /**
     * Maps each sprite in TX Plant.png to the top left and bottom right tiles that it occupies.
     * If an array list has an extra integer array, that indicates how much to shift down the sprite image.
     */
    private void initializeSSPlantsMap() {
        HashMap<Integer, ArrayList<Integer[]>> ssMap = new HashMap<>();
        ssMap.put(0, new ArrayList<>(Arrays.asList(
                new Integer[]{0,0},
                new Integer[]{4,4}
        ))); // Tree 1
        ssMap.put(1, new ArrayList<>(Arrays.asList(
                new Integer[]{0,5},
                new Integer[]{4,7}
        ))); // Tree 2
        ssMap.put(2, new ArrayList<>(Arrays.asList(
                new Integer[]{0,9},
                new Integer[]{4,11}
        ))); // Tree 3
        ssMap.put(3, new ArrayList<>(Arrays.asList(
                new Integer[]{6,1},
                new Integer[]{6,1}
        ))); // Bush small 1
        ssMap.put(4, new ArrayList<>(Arrays.asList(
                new Integer[]{6,3},
                new Integer[]{6,3}
        ))); // Bush small 2
        ssMap.put(5, new ArrayList<>(Arrays.asList(
                new Integer[]{5,4},
                new Integer[]{6,6}
        ))); // Bush medium
        ssMap.put(6, new ArrayList<>(Arrays.asList(
                new Integer[]{5,7},
                new Integer[]{7,9},
                new Integer[]{1}
        ))); // Bush large 1
        ssMap.put(7, new ArrayList<>(Arrays.asList(
                new Integer[]{5,10},
                new Integer[]{7,12},
                new Integer[]{1}
        ))); // Bush large 2
        ssMap.put(8, new ArrayList<>(Arrays.asList(
                new Integer[]{5,13},
                new Integer[]{7,15},
                new Integer[]{1}
        ))); // Bush large 3
        ssMaps.put(Game.RSC_SS_PLANTS_IMG, ssMap);
    }

    private void initializeSSBerriesMap() {
        HashMap<Integer, ArrayList<Integer[]>> ssMap = new HashMap<>();
        ssMap.put(0, new ArrayList<>(Arrays.asList(
                new Integer[]{0,0},
                new Integer[]{0,0}
        ))); // Farmland
        ssMap.put(1, new ArrayList<>(Arrays.asList(
                new Integer[]{0,1},
                new Integer[]{0,1}
        ))); // Seedling Farmland
        ssMap.put(2, new ArrayList<>(Arrays.asList(
                new Integer[]{0,2},
                new Integer[]{0,2}
        ))); // Bush blue
        ssMap.put(3, new ArrayList<>(Arrays.asList(
                new Integer[]{0,3},
                new Integer[]{0,3}
        ))); // Bush pink
        ssMap.put(4, new ArrayList<>(Arrays.asList(
                new Integer[]{1,2},
                new Integer[]{1,4}
        ))); // Berry Pile
        ssMaps.put(Game.RSC_SS_BERRIES_IMG, ssMap);
    }

    private void spawnObstacleAt(int row, int col, int obsType, String ssID) {
        GameObject obs = new GameObject(game, row, col, ssMaps.get(ssID).get(obsType), ssID, 1);
        gameObjects.add(obs);
        tileMap[row][col].setContainsObstacle(true);
    }

    private void plantObstacles() {
        initializeSSPlantsMap();

        FileHandle tileMapFile = Gdx.files.internal("Text/plants_map.txt");

        try (BufferedReader br = tileMapFile.reader(100000)) {
            String line;
            String substr;
            int y = PlayScreen.TILE_ROWS-1;
            int obsType;

            while ((line = br.readLine()) != null && y >= 0) {
                for (int x = 0; x < line.length(); x+=3) {
                    substr = line.substring(x,x+2);
                    obsType = Integer.parseInt(substr);

                    if (obsType > 0) {
                        GameObject shadow = new GameObject(game, y, x/3
                                , ssMaps.get(Game.RSC_SS_PLANTS_IMG).get(obsType-1)
                                , Game.RSC_SS_PLANTS_SHADOW_IMG, 1000);
                        gameObjects.add(shadow);
                        spawnObstacleAt(y,x/3, obsType-1, Game.RSC_SS_PLANTS_IMG);
                    }
                }
                y--;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.exit();
            System.exit(-1);
        }
    }

    private void berryObstacles() {
        initializeSSBerriesMap();
        FileHandle berriesFile = Gdx.files.internal("Text/berries_map.txt");

        try (BufferedReader br = berriesFile.reader(100000)) {
            String line;
            String substr;
            int y = PlayScreen.TILE_ROWS-1;
            int obsType;

            while ((line = br.readLine()) != null && y >= 0) {
                for (int x = 0; x < line.length(); x+=3) {
                    substr = line.substring(x,x+2);
                    obsType = Integer.parseInt(substr);

                    // This is the berry pile
                    if (obsType == 5) {
                        importantLocations.add(new Integer[]{x/3, y});
                    }

                    if (obsType > 0) spawnObstacleAt(y,x/3, obsType-1, Game.RSC_SS_BERRIES_IMG);
                }
                y--;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.exit();
            System.exit(-1);
        }
    }

    public ArrayList<Integer[]> spawnAllAssets() {
        populateTileMap();
        berryObstacles();
        plantObstacles();
        return importantLocations;
    }
}
