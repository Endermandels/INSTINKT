package wsuv.instinkt;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.*;

public class PlayScreen extends ScreenAdapter {
    private enum SubState {READY, GAME_OVER, PLAYING}
    private Game game;
    private HUD hud;
    private Player player;
    private SubState state;
    private ArrayList<GameObject> gameObjects;
    private ArrayList<Enemy> enemies;
    private ArrayList<Enemy> enemiesToRemove;

    private ArrayList<Integer[]> enemySpawnLocations;

    private Tile[][] tileMap;
    public static final int TILE_ROWS = 12;
    public static final int TILE_COLS = 18;

    public static int TILE_SIZE = 32;
    public static int TILE_SCALE = 2;
    public static int TILE_SCALED_SIZE = TILE_SIZE * TILE_SCALE;

    // Switching between Game Over and Ready
    private final float TIMER_MAX = 3.0f;
    private float timer;

    private boolean paused;
    private boolean doStep; // Stepping through update cycles while paused

    private boolean showTileLocations;

    public PlayScreen(Game game) {
        this.game = game;
        hud = new HUD(12, 13, 10, 500, game.am.get(Game.RSC_DPCOMIC_FONT_BLACK));
        tileMap = new Tile[TILE_ROWS][TILE_COLS];
        player = new Player(game,6,10);
        gameObjects = new ArrayList<>();
        enemies = new ArrayList<>();
        enemySpawnLocations = new ArrayList<>(Arrays.asList(
                new Integer[]{0,-1},
                new Integer[]{0,TILE_COLS},
                new Integer[]{TILE_ROWS,TILE_COLS-1}
        ));
        enemiesToRemove = new ArrayList<>();

        gameObjects.add(player);
        spawnEnemy(0);

        AssetsSpawner assetsSpawner = new AssetsSpawner(game, tileMap, gameObjects);
        assetsSpawner.spawnAllAssets();

        fillDijkstraFromTile(Tile.DistanceType.PLAYER, player.getTileX(), player.getTileY());

        timer = 0f;
        paused = false;
        doStep = false;

        showTileLocations = false;

        // the HUD will show FPS always, by default.  Here's how
        // to use the HUD interface to silence it (and other HUD Data)
        hud.setDataVisibility(HUDViewCommand.Visibility.WHEN_OPEN);

        // HUD Console Commands

        // Pause - Pause the game
        hud.registerAction("p", new HUDActionCommand() {
            static final String help = "Pause the game";

            @Override
            public String execute(String[] cmd) {
                paused = true;
                return "Game paused";
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        // Unpause - Unpause the game
        hud.registerAction("u", new HUDActionCommand() {
            static final String help = "Unpause the game";

            @Override
            public String execute(String[] cmd) {
                paused = false;
                return "Game resumed";
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        // Step - Step through the next update cycle
        hud.registerAction("s", new HUDActionCommand() {
            static final String help = "Step through the next update cycle while paused";

            @Override
            public String execute(String[] cmd) {
                if (paused) {
                    doStep = true;
                    return "ok!";
                }
                return "Game is not paused";
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        // Tile Locations - Show the tile location of each moving entity
        hud.registerAction("tl", new HUDActionCommand() {
            static final String help = "Show tile location of each moving entity";

            @Override
            public String execute(String[] cmd) {
                showTileLocations = !showTileLocations;
                return "ok!";
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        // we're adding an input processor AFTER the HUD has been created,
        // so we need to be a bit careful here and make sure not to clobber
        // the HUD's input controls. Do that by using an InputMultiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        // let the HUD's input processor handle things first....
        multiplexer.addProcessor(Gdx.input.getInputProcessor());
        Gdx.input.setInputProcessor(multiplexer);

    }

    private ArrayList<Tile> getNeighbors(Tile tile, PriorityQueue<Tile> queue) {
        ArrayList<Tile> neighbors = new ArrayList<>();

        if (game.validMove(tileMap,tile.getX()-1, tile.getY())
                && queue.contains(tileMap[tile.getY()][tile.getX()-1]))
            neighbors.add(tileMap[tile.getY()][tile.getX()-1]);
        if (game.validMove(tileMap,tile.getX()+1, tile.getY())
                && queue.contains(tileMap[tile.getY()][tile.getX()+1]))
            neighbors.add(tileMap[tile.getY()][tile.getX()+1]);
        if (game.validMove(tileMap, tile.getX(), tile.getY()-1)
                && queue.contains(tileMap[tile.getY()-1][tile.getX()]))
            neighbors.add(tileMap[tile.getY()-1][tile.getX()]);
        if (game.validMove(tileMap, tile.getX(), tile.getY()+1)
                && queue.contains(tileMap[tile.getY()+1][tile.getX()]))
            neighbors.add(tileMap[tile.getY()+1][tile.getX()]);

        return neighbors;
    }

    /**
     * Fill each tile in tileMap with values from tileX and tileY location using Dijkstra's Algorithm
     * Ignore obstacle tiles
     */
    private void fillDijkstraFromTile(Tile.DistanceType dt, int tileX, int tileY) {
        Tile source = tileMap[tileY][tileX];
        source.setDistance(dt, 0f);
        Comparator<Tile> comparator = new DistanceComparator(dt);
        PriorityQueue<Tile> queue = new PriorityQueue<>(comparator);

        for (Tile[] tiles : tileMap) {
            for (Tile tile : tiles) {
                if (!tile.equals(source)) tile.setDistance(dt, Tile.INF);
                if (!tile.isObstacle()) queue.add(tile);
            }
        }

        while (!queue.isEmpty()) {
            Tile tile = queue.poll();

            for (Tile neighbor : getNeighbors(tile, queue)) {
                float pathDist = tile.getDistance(dt) + 1f; // Each path has a weight of 1 for now.
                if (pathDist < neighbor.getDistance(dt)) {
                    neighbor.setDistance(dt, pathDist);
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Shows the algorithm at work
//        for (int y = TILE_ROWS-1; y >= 0; y--) {
//            for (int x = 0; x < TILE_COLS; x++) {
//                System.out.print(tileMap[y][x].getDistance(dt));
//                System.out.print(',');
//            }
//            System.out.println();
//        }
    }

    private void spawnEnemy(int spawnLocationIdx) {
        enemies.add(new Enemy(game, enemySpawnLocations.get(spawnLocationIdx)[1]
                ,enemySpawnLocations.get(spawnLocationIdx)[0], enemySpawnLocations));
        gameObjects.add(enemies.get(0));
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
        state = SubState.READY;
    }

    public void update(float delta) {
        if (!paused || doStep) {
            switch (state) {
                case PLAYING:
                    if (player.update(tileMap)) {
                        fillDijkstraFromTile(Tile.DistanceType.PLAYER, player.getTileX(), player.getTileY());
                    }

                    for (Enemy enemy : enemies) {
                        if (enemy.update(tileMap)) enemiesToRemove.add(enemy);
                    }
                    for (Enemy enemy : enemiesToRemove) {
                        enemies.remove(enemy);
                    }
                    enemiesToRemove.clear();

                    if (!hud.isOpen()) {
                        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                            state = SubState.GAME_OVER;
                            timer = 0;
                            game.battleMusic.stop();
                            game.menuMusic.play();
                        }
                        player.setTakeInput(true);
                    } else {
                        player.setTakeInput(false);
                    }
                    break;
                case GAME_OVER:
                    timer += delta;
                    if (timer > TIMER_MAX) state = SubState.READY;
                    break;
                case READY:
                    if (!hud.isOpen()) {
                        if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) && !Gdx.input.isKeyPressed(Input.Keys.GRAVE)) {
                            state = SubState.PLAYING;
                            game.menuMusic.stop();
                            game.battleMusic.play();
                        }
                    }
                default:
                    break;
            }
            doStep = false;
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0, 0, 0, 1);
        game.batch.begin();
        // this logic could also be pushed into a method on SubState enum
        switch (state) {
            case GAME_OVER:
                // Draw Game Over image
                Texture gameover_img = game.am.get(Game.RSC_GAMEOVER_IMG, Texture.class);
                game.batch.draw(gameover_img
                        , Gdx.graphics.getWidth() / 2f - gameover_img.getWidth() / 2f
                        , Gdx.graphics.getHeight() / 2f - gameover_img.getHeight() / 2f +50f);
                break;
            case READY:
                // Draw Press A Key image
                Texture pressakey_img = game.am.get(Game.RSC_PRESSAKEY_IMG, Texture.class);
                game.batch.draw(pressakey_img
                        , Gdx.graphics.getWidth() / 2f - pressakey_img.getWidth() / 2f
                        , Gdx.graphics.getHeight() / 2f - pressakey_img.getHeight() / 2f + 200f);
                break;
            case PLAYING:
                // Draw Tiles
                for (int row = 0; row < TILE_ROWS; row++)
                    for (int col = 0; col < TILE_COLS; col++) {
                        Tile tile = tileMap[row][col];
                        game.batch.draw(tile.getImg(), tile.getImgX(), tile.getImgY()
                                , TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                    }
                // Draw Game Objects
                gameObjects.sort(Comparator.comparingInt(GameObject::getPriority).reversed());
                for (GameObject obs : gameObjects) {
                    TextureRegion img = obs.getImg();
                    if (showTileLocations) {
                        if (obs instanceof Enemy) {
                            Enemy e = (Enemy) obs;
                            game.batch.draw((Texture) game.am.get(Game.RSC_OVERLAY_IMG), e.getTileX()*TILE_SCALED_SIZE, e.getTileY()*TILE_SCALED_SIZE,
                                    TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                        }
                        else if (obs instanceof Player) {
                            Player p = (Player) obs;
                            game.batch.draw((Texture) game.am.get(Game.RSC_OVERLAY_IMG), p.getTileX()*TILE_SCALED_SIZE, p.getTileY()*TILE_SCALED_SIZE,
                                    TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                        }
                    }
                    game.batch.draw(img, obs.getImgX(), obs.getImgY()
                            , img.getRegionWidth()*TILE_SCALE, img.getRegionHeight()*TILE_SCALE);
                }
                break;
        }
        hud.draw(game.batch);
        game.batch.end();
    }
}

class DistanceComparator implements Comparator<Tile> {

    Tile.DistanceType dt;

    public DistanceComparator(Tile.DistanceType dt) {
        this.dt = dt;
    }

    @Override
    public int compare(Tile t1, Tile t2) {
        return Float.compare(t1.getDistance(dt), t2.getDistance(dt));
    }
}

