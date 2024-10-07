package wsuv.instinkt;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.*;

public class PlayScreen extends ScreenAdapter {
    private enum SubState {READY, GAME_OVER, PLAYING}
    private Game game;
    private HUD hud;
    private GUI gui;
    private Player player;
    private EnemySpawner enemySpawner;
    private SubState state;
    private BitmapFont debugFont;
    private ArrayList<GameObject> gameObjects;
    private ArrayList<Enemy> enemies;
    private ArrayList<Enemy> enemiesToRemove;
    private ArrayList<GameObject> debugImages;

    private Tile[][] tileMap;
    public static final int TILE_ROWS = 12;
    public static final int TILE_COLS = 18;

    private final int GUI_SPACE = 64;
    public static int TILE_SIZE = 32;
    public static int TILE_SCALE = 2;
    public static int TILE_SCALED_SIZE = TILE_SIZE * TILE_SCALE;

    // Switching between Game Over and Ready
    private final float TIMER_MAX = 3.0f;
    private float timer;

    private boolean paused;
    private boolean doStep; // Stepping through update cycles while paused

    private boolean showTileLocations;
    private boolean showEnemyStats;

    public PlayScreen(Game game) {
        this.game = game;
        hud = new HUD(12, 13, 10, 500, game.am.get(Game.RSC_DPCOMIC_FONT_BLACK));
        debugFont = game.am.get(Game.RSC_DPCOMIC_FONT);
        tileMap = new Tile[TILE_ROWS][TILE_COLS];
        player = new Player(game,6,10);
        gui = new GUI(game, player);
        gameObjects = new ArrayList<>();
        debugImages = new ArrayList<>();
        enemies = new ArrayList<>();
        enemiesToRemove = new ArrayList<>();
        enemySpawner = new EnemySpawner(game, enemies, gameObjects);

        gameObjects.add(player);

        AssetsSpawner assetsSpawner = new AssetsSpawner(game, tileMap, gameObjects);
        ArrayList<Integer[]> importantLocations = assetsSpawner.spawnAllAssets();
        Integer[] berryPile = importantLocations.get(0);

        fillDijkstraFromTile(Tile.DistanceType.PLAYER, player.getTileX(), player.getTileY());
        fillDijkstraFromTile(Tile.DistanceType.BERRIES, berryPile[0], berryPile[1]);

        timer = 0f;
        paused = false;
        doStep = false;

        showTileLocations = false;
        showEnemyStats = false;

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

        // register build-in view commands...
        hud.registerView("HP:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(player.getStats().getHp());
            }
        });

        // Stats - Show debug stat information for player and enemies
        hud.registerAction("stats", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                HUDViewCommand.Visibility v = hud.getHudData().get("HP:").nextVisiblityState();
                showEnemyStats = !showEnemyStats;
                return "states visibility: " + v;
            }

            public String help(String[] cmd) {
                return "toggle player stats visibility always <-> in console";
            }
        });

        // HP - Set player's hp to specified amount (Capped at maximum player HP)
        hud.registerAction("hp", new HUDActionCommand() {
            static final String help = "usage: hp <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    int hp = Integer.parseInt(cmd[1]);
                    player.getStats().setHp(hp);
                    return "Set player's hp to " + player.getStats().getHp();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's HP to specified amount";
            }
        });

        // ATK - Set player's attack to specified amount (Lowest is 0)
        hud.registerAction("atk", new HUDActionCommand() {
            static final String help = "usage: atk <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    int atk = Integer.parseInt(cmd[1]);
                    player.getStats().setAtk(atk);
                    return "Set player's atk to " + player.getStats().getAtk();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's attack to specified amount";
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
                if (!tile.isObstacle() || tile.equals(source)) queue.add(tile);
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
    }

    private void reset() {
        player.reset();
        for (Enemy e : enemies) {
            gameObjects.remove(e);
        }
        enemies.clear();
        enemiesToRemove.clear();

        fillDijkstraFromTile(Tile.DistanceType.PLAYER, player.getTileX(), player.getTileY());
        gameObjects.add(player);
        enemySpawner.setFormation(0);
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
                    if (player.update(tileMap, enemies)) {
                        fillDijkstraFromTile(Tile.DistanceType.PLAYER, player.getTileX(), player.getTileY());
                    }

                    if (player.getStats().isDead() && player.isFinishedDeathAnimation()) {
                        state = SubState.GAME_OVER;
                        timer = 0;
                        game.battleMusic.stop();
                        game.menuMusic.play();
                        break;
                    }

                    for (Enemy enemy : enemies) {
                        if (enemy.update(tileMap)) enemiesToRemove.add(enemy);
                    }
                    for (Enemy enemy : enemiesToRemove) {
                        enemies.remove(enemy);
                        gameObjects.remove(enemy);
                    }
                    enemiesToRemove.clear();

                    enemySpawner.update();

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
                    gui.update();
                    break;
                case GAME_OVER:
                    timer += delta;
                    if (timer > TIMER_MAX) state = SubState.READY;
                    break;
                case READY:
                    if (!hud.isOpen()) {
                        if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) && !Gdx.input.isKeyPressed(Input.Keys.GRAVE)) {
                            reset();
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
                        game.batch.draw(tile.getImg(), tile.getImgX(), tile.getImgY()+GUI_SPACE
                                , TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                        if (showTileLocations) {
                            float dist = tile.getDistance(Tile.DistanceType.PLAYER);
                            float berryDist = tile.getDistance(Tile.DistanceType.BERRIES);
                            String num;
                            String berryNum;
                            if (dist == Tile.INF) {
                                num = "~";
                                berryNum = "~";
                            }
                            else {
                                num = Float.toString(tile.getDistance(Tile.DistanceType.PLAYER));
                                berryNum = Float.toString(berryDist);
                            }

                            float clampedDist = Math.min(Math.max(dist, 0), 12);
                            float redIntensity = clampedDist / 12.0f;

                            debugFont.setColor(redIntensity, 0, 0, 1); // Color more red for higher values
                            debugFont.draw(game.batch, num, tile.getImgX(), tile.getImgY() + TILE_SCALED_SIZE+GUI_SPACE);
                            debugFont.setColor(1, 1, 1, 1);  // Reset to white
                            debugFont.draw(game.batch, berryNum, tile.getImgX(), tile.getImgY() + TILE_SCALED_SIZE/2f+GUI_SPACE);
                        }
                    }
                // Draw Game Objects
                gameObjects.sort(Comparator.comparingInt(GameObject::getPriority).reversed());
                for (GameObject ob : gameObjects) {
                    TextureRegion img = ob.getImg();
                    if (showTileLocations) {
                        if (ob instanceof Enemy) {
                            Enemy e = (Enemy) ob;
                            game.batch.draw((Texture) game.am.get(Game.RSC_OVERLAY_IMG)
                                    , e.getTileX()*TILE_SCALED_SIZE, e.getTileY()*TILE_SCALED_SIZE+GUI_SPACE,
                                    TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                        }
                        else if (ob instanceof Player) {
                            Player p = (Player) ob;
                            game.batch.draw((Texture) game.am.get(Game.RSC_OVERLAY_IMG)
                                    , p.getTileX()*TILE_SCALED_SIZE, p.getTileY()*TILE_SCALED_SIZE+GUI_SPACE,
                                    TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                        }
                    }
                    game.batch.draw(img, ob.getImgX(), ob.getImgY()+GUI_SPACE
                            , img.getRegionWidth()*TILE_SCALE, img.getRegionHeight()*TILE_SCALE);
                    if (showEnemyStats) {
                        if (ob instanceof Enemy) {
                            debugImages.add(ob);
                        }
                    }
                }
                for (GameObject d : debugImages) {
                    if (d instanceof Enemy) {
                        Enemy e = (Enemy) d;
                        debugFont.draw(game.batch, "HP: " + e.getStats().getHp(),
                                e.getImgX(), e.getImgY() + (float) TILE_SCALED_SIZE * 3/2+GUI_SPACE);
                    }
                }
                debugImages.clear();
                gui.draw(game.batch);
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

