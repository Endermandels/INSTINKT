package wsuv.instinkt;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.*;

public class PlayScreen extends ScreenAdapter {
    public enum SubState {READY, GAME_OVER, ENEMY_WAVE, COOLDOWN}
    private Game game;
    private HUD hud;
    private GUI gui;
    private Player player;
    private EnemySpawner enemySpawner;
    private BerryManager berryManager;
    private SubState state;
    private BitmapFont debugFont;
    private ArrayList<GameObject> gameObjects;
    private ArrayList<Enemy> enemies;
    private ArrayList<Enemy> enemiesToRemove;
    private ArrayList<GameObject> debugImages;
    private Set<Tile> aoeEffectTiles;
    private Texture aoeEffectImg;

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

    private final int WAVES_GOAL = 10;
    private int wave;

    private boolean paused;
    private boolean doStep; // Stepping through update cycles while paused
    private boolean escPressed;
    private boolean interactPressed; // For planting berry bushes

    private boolean showTileLocations;
    private boolean showEnemyStats;

    private boolean skipToCooldownPhase;

    public PlayScreen(Game game) {
        this.game = game;

        gameObjects = new ArrayList<>();
        debugImages = new ArrayList<>();
        enemies = new ArrayList<>();
        enemiesToRemove = new ArrayList<>();
        aoeEffectTiles = new HashSet<>();

        hud = new HUD(20, 13, 10, 500, game.am.get(Game.RSC_DPCOMIC_FONT_BLACK));
        debugFont = game.am.get(Game.RSC_DPCOMIC_FONT);
        tileMap = new Tile[TILE_ROWS][TILE_COLS];
        berryManager = new BerryManager(game, gameObjects);
        player = new Player(game,6,10, gameObjects);
        gui = new GUI(game, player, berryManager, this);
        enemySpawner = new EnemySpawner(game, enemies, gameObjects, player);

        gameObjects.add(player);
        aoeEffectImg = game.am.get(Game.RSC_AOE_EFFECT_IMG);


        AssetsSpawner assetsSpawner = new AssetsSpawner(game, tileMap, gameObjects);
        ArrayList<Integer[]> importantLocations = assetsSpawner.spawnAllAssets();
        Integer[] berryPile = importantLocations.get(0);

        fillDijkstraFromTile(Tile.DistanceType.PLAYER, player.getTileX(), player.getTileY());
        fillDijkstraFromTile(Tile.DistanceType.BERRIES, berryPile[0], berryPile[1]);

        ArrayList<Integer[]> enemySpawnLocations = enemySpawner.getEnemySpawnLocations();
        boolean first = true;
        for (Integer[] location : enemySpawnLocations) {
            fillDijkstraFromTile(Tile.DistanceType.EXIT
                    , location[1]
                    , location[0]
                    , first);
            first = false;
        }

        timer = 0f;

        wave = 1;

        paused = false;
        doStep = false;
        escPressed = false;
        interactPressed = false;

        showTileLocations = false;
        showEnemyStats = false;

        skipToCooldownPhase = false;

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

        // Next - Move to next cooldown phase (skip current enemy wave)
        hud.registerAction("next", new HUDActionCommand() {
            static final String help = "Skip the current enemy wave and go to next cooldown phase";

            @Override
            public String execute(String[] cmd) {
                skipToCooldownPhase = true;
                enemySpawner.setNoMoreEnemiesToSpawn(true);
                return "ok!";
            }

            public String help(String[] cmd) {
                return help;
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

        // Berries - Set berries to specified amount
        hud.registerAction("berries", new HUDActionCommand() {
            static final String help = "usage: berries <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    int berries = Integer.parseInt(cmd[1]);
                    berryManager.setBerriesCollected(berries);
                    return "Set player's berry count to " + berryManager.getBerriesCollected();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's berry count to specified amount";
            }
        });

        PlayerHUDCommands hudSetup = new PlayerHUDCommands(hud, player);
        hudSetup.initHUDCommands();

        hud.registerView("Berries:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(berryManager.getBerriesCollected());
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

    private Tile getEnemyStartTile(int tileX, int tileY) {

        if (game.validMove(tileMap,tileX-1, tileY))
            return tileMap[tileY][tileX-1];
        if (game.validMove(tileMap,tileX+1, tileY))
            return tileMap[tileY][tileX+1];
        if (game.validMove(tileMap, tileX, tileY-1))
            return tileMap[tileY-1][tileX];
        if (game.validMove(tileMap, tileX, tileY+1))
            return tileMap[tileY+1][tileX];

        return null;
    }

    private void fillDijkstraFromTile(Tile.DistanceType dt, int tileX, int tileY) {
        fillDijkstraFromTile(dt, tileX, tileY, true);
    }

    /**
     * Fill each tile in tileMap with values from tileX and tileY location using Dijkstra's Algorithm
     * Ignore obstacle tiles
     */
    private void fillDijkstraFromTile(Tile.DistanceType dt, int tileX, int tileY, boolean newFill) {
        Tile source;
        Comparator<Tile> comparator = new DistanceComparator(dt);
        PriorityQueue<Tile> queue = new PriorityQueue<>(comparator);

        if (dt != Tile.DistanceType.EXIT)  {
            source = tileMap[tileY][tileX];
            source.setDistance(dt, 0f);
        }
        else  {
            source = getEnemyStartTile(tileX, tileY);
            source.setDistance(dt, 1f);
        }

        for (Tile[] tiles : tileMap) {
            for (Tile tile : tiles) {
                if (!tile.equals(source) && newFill) tile.setDistance(dt, Tile.INF);
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
        berryManager.reset();
        for (Enemy e : enemies) {
            gameObjects.remove(e);
        }
        for (Tile[] tiles : tileMap) {
            for (Tile tile : tiles) {
                tile.getEnemies().clear();
            }
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
            if (state == SubState.ENEMY_WAVE) {
                if (player.update(tileMap, enemies, state)) {
                    fillDijkstraFromTile(Tile.DistanceType.PLAYER, player.getTileX(), player.getTileY());
                }

                if (player.getStats().isDead() && player.isFinishedDeathAnimation()) {
                    state = SubState.GAME_OVER;
                    timer = 0;
                    game.battleMusic.stop();
                    game.menuMusic.play();
                }

                for (Enemy enemy : enemies) {
                    if (enemy.update(tileMap) || skipToCooldownPhase) enemiesToRemove.add(enemy);
                }
                for (Enemy enemy : enemiesToRemove) {
                    if (game.validMove(enemy.getTileX(), enemy.getTileY())) {
                        tileMap[enemy.getTileY()][enemy.getTileX()].getEnemies().remove(enemy);
                    }
                    enemies.remove(enemy);
                    gameObjects.remove(enemy);
                }
                enemiesToRemove.clear();

                enemySpawner.update();

                if (enemySpawner.areNoMoreEnemiesToSpawn() && enemies.isEmpty()) {
                    state = SubState.COOLDOWN;
                    wave++;
                    berryManager.startOfCooldown();
                    player.startCooldown();
                    timer = 0;
                    game.battleMusic.stop();
                    game.cooldownMusic.play();

                    for (Tile[] tiles : tileMap) {
                        for (Tile tile : tiles) {
                            tile.setStinky(false, 0L);
                            aoeEffectTiles.remove(tile);
                        }
                    }
                }

                for (Tile[] tiles : tileMap) {
                    for (Tile tile : tiles) {
                        if (tile.update()) aoeEffectTiles.remove(tile);
                    }
                }

                if (escPressed && !Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    escPressed = false;
                } else if (!escPressed && !hud.isOpen()) {
                    if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                        paused = true;
                        escPressed = true;
                    } else if (Gdx.input.isKeyPressed(Input.Keys.E) && !interactPressed) {
                        interactPressed = true;
                        int amount = berryManager.getBerriesCollected();
                        if (amount > 0 && player.getSpraysLeft() < player.getMaxSprays()) {
                            berryManager.setBerriesCollected(amount-1);
                            player.eatBerry();
                        }
                    } else if (!Gdx.input.isKeyPressed(Input.Keys.E)) {
                        interactPressed = false;
                    }
                    player.setTakeInput(true);
                } else if (!escPressed) {
                    player.setTakeInput(false);
                }
                gui.update();
            } else if (state == SubState.COOLDOWN) {
                player.update(tileMap, enemies, state);

                for (Tile[] tiles : tileMap) {
                    for (Tile tile : tiles) {
                        if (tile.update()) aoeEffectTiles.remove(tile);
                    }
                }

                if (!hud.isOpen()) {
                    if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                        state = SubState.ENEMY_WAVE;
                        skipToCooldownPhase = false;
                        fillDijkstraFromTile(Tile.DistanceType.PLAYER, player.getTileX(), player.getTileY());
                        enemySpawner.setFormation(0);
                        game.cooldownMusic.stop();
                        game.battleMusic.play();
                    } else if (Gdx.input.isKeyPressed(Input.Keys.E) && !interactPressed) {
                        interactPressed = true;
                        berryManager.plantNewBerryBush();
                    } else if (!Gdx.input.isKeyPressed(Input.Keys.E)) {
                        interactPressed = false;
                    }
                    player.setTakeInput(true);
                } else {
                    player.setTakeInput(false);
                }
                gui.update();
            } else if (state == SubState.GAME_OVER) {
                timer += delta;
                if (timer > TIMER_MAX) state = SubState.READY;
            } else if (state == SubState.READY) {
                if (!hud.isOpen()) {
                    if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) && !Gdx.input.isKeyPressed(Input.Keys.GRAVE)) {
                        reset();
                        state = SubState.ENEMY_WAVE;
                        game.menuMusic.stop();
                        game.battleMusic.play();
                    }
                }
            }
            doStep = false;
        } else {
            if (escPressed && !Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                escPressed = false;
            } else if (!escPressed && !hud.isOpen()) {
                if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    paused = false;
                    escPressed = true;
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0, 0, 0, 1);
        game.batch.begin();
        // this logic could also be pushed into a method on SubState enum
        if (state == SubState.GAME_OVER) {
            // Draw Game Over image
            Texture gameover_img = game.am.get(Game.RSC_GAMEOVER_IMG, Texture.class);
            game.batch.draw(gameover_img
                    , Gdx.graphics.getWidth() / 2f - gameover_img.getWidth() / 2f
                    , Gdx.graphics.getHeight() / 2f - gameover_img.getHeight() / 2f +50f);
        } else if (state == SubState.READY) {
            // Draw Press A Key image
            Texture pressakey_img = game.am.get(Game.RSC_PRESSAKEY_IMG, Texture.class);
            game.batch.draw(pressakey_img
                    , Gdx.graphics.getWidth() / 2f - pressakey_img.getWidth() / 2f
                    , Gdx.graphics.getHeight() / 2f - pressakey_img.getHeight() / 2f + 200f);
        } else if (state == SubState.ENEMY_WAVE || state == SubState.COOLDOWN) {
            // Draw Tiles
            for (int row = 0; row < TILE_ROWS; row++)
                for (int col = 0; col < TILE_COLS; col++) {
                    Tile tile = tileMap[row][col];
                    if (tile.isStinky()) aoeEffectTiles.add(tile);
                    game.batch.draw(tile.getImg(), tile.getImgX(), tile.getImgY()+GUI_SPACE
                            , TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                    if (showTileLocations) {
                        float dist = tile.getDistance(Tile.DistanceType.PLAYER);
                        float berryDist = tile.getDistance(Tile.DistanceType.BERRIES);
                        float exitDist = tile.getDistance(Tile.DistanceType.EXIT);
                        String num;
                        String berryNum;
                        String exitNum;
                        if (dist == Tile.INF) {
                            num = "~";
                            berryNum = "~";
                            exitNum = "~";
                        }
                        else {
                            num = Integer.toString((int) tile.getDistance(Tile.DistanceType.PLAYER));
                            berryNum = Integer.toString((int) berryDist);
                            exitNum = Integer.toString((int) exitDist);
                        }

                        float clampedDist = Math.min(Math.max(dist, 0), 12);
                        float redIntensity = clampedDist / 12.0f;

                        debugFont.setColor(redIntensity, 0, 0, 1); // Color more red for higher values
                        debugFont.draw(game.batch, num, tile.getImgX(),
                                tile.getImgY() + TILE_SCALED_SIZE+GUI_SPACE);
                        debugFont.setColor(0, 0, 0, 1);  // Reset to white
                        debugFont.draw(game.batch, berryNum, tile.getImgX(),
                                tile.getImgY() + TILE_SCALED_SIZE/2f+GUI_SPACE);
                        debugFont.setColor(1, 1, 1, 1);  // Reset to white
                        debugFont.draw(game.batch, exitNum, tile.getImgX() + TILE_SCALED_SIZE/2f,
                                tile.getImgY() + TILE_SCALED_SIZE/2f+GUI_SPACE);

                        for (Enemy e : tile.getEnemies()) {
                            game.batch.draw((Texture) game.am.get(Game.RSC_OVERLAY_IMG)
                                    , e.getTileX()*TILE_SCALED_SIZE, e.getTileY()*TILE_SCALED_SIZE+GUI_SPACE,
                                    TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                        }
                    }
                }
            // Draw Game Objects
            gameObjects.sort(Comparator.comparingInt(GameObject::getPriority).reversed());
            for (GameObject ob : gameObjects) {
                if (showTileLocations) {
                    if (ob instanceof Player) {
                        Player p = (Player) ob;
                        game.batch.draw((Texture) game.am.get(Game.RSC_OVERLAY_IMG)
                                , p.getTileX()*TILE_SCALED_SIZE, p.getTileY()*TILE_SCALED_SIZE+GUI_SPACE,
                                TILE_SCALED_SIZE, TILE_SCALED_SIZE);
                    }
                }

                if (ob instanceof Spray) {
                    Spray spray = (Spray) ob;
                    TextureRegion img = spray.getImg();
                    if (img != null) {
                        int dir = spray.isFlipped() ? 1 : -1;
                        for (int i = 0; i < spray.getLength(); i++) {
                            game.batch.draw(img, ob.getImgX() + dir * i * PlayScreen.TILE_SCALED_SIZE
                                    , ob.getImgY() + GUI_SPACE
                                    , img.getRegionWidth() * TILE_SCALE, img.getRegionHeight() * TILE_SCALE);
                        }
                    }
                } else {
                    TextureRegion img = ob.getImg();
                    if (img != null) {
                        game.batch.draw(img, ob.getImgX(), ob.getImgY()+GUI_SPACE
                            , img.getRegionWidth()*TILE_SCALE, img.getRegionHeight()*TILE_SCALE);
                    }
                }

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

            for (Tile tile : aoeEffectTiles) {
                game.batch.draw(aoeEffectImg, tile.getImgX(), tile.getImgY() + GUI_SPACE, TILE_SCALED_SIZE, TILE_SCALED_SIZE);
            }

            debugImages.clear();
            gui.draw(game.batch);
        }
        hud.draw(game.batch);
        game.batch.end();
    }

    public int getWave() {
        return wave;
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

class PlayerHUDCommands {
    private HUD hud;
    private Player player;

    public PlayerHUDCommands(HUD hud, Player player) {
        this.hud = hud;
        this.player = player;
    }

    public void initHUDCommands() {
        // register build-in view commands...
        hud.registerView("HP:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(player.getStats().getHp());
            }
        });


        // HP - Set player's max hp to specified amount
        hud.registerAction("hp", new HUDActionCommand() {
            static final String help = "usage: hp <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    int hp = Integer.parseInt(cmd[1]);
                    player.getStats().setMaxHP(hp);
                    return "Set player's max hp to " + player.getStats().getMaxHP();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's max HP to specified amount";
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

        // Spray Length - Set player's spray length to specified amount
        hud.registerAction("slen", new HUDActionCommand() {
            static final String help = "usage: slen <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    int slen = Integer.parseInt(cmd[1]);
                    player.setSprayLength(slen);
                    return "Set player's spray length to " + player.getSprayLength();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's spray length to specified amount";
            }
        });

        // Spray Radius - Set player's spray radius to specified amount
        hud.registerAction("srad", new HUDActionCommand() {
            static final String help = "usage: srad <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    int srad = Integer.parseInt(cmd[1]);
                    player.setSprayRadius(srad);
                    return "Set player's spray radius to " + player.getSprayRadius();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's spray radius to specified amount";
            }
        });

        // Spray Duration - Set player's spray duration to specified amount (in seconds)
        hud.registerAction("sdur", new HUDActionCommand() {
            static final String help = "usage: sdur <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    float sdur = Float.parseFloat(cmd[1]);
                    player.setSprayDuration((long)(sdur*1000f));
                    return "Set player's spray duration to " + player.getSprayDuration();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's spray duration to specified amount (in seconds)";
            }
        });

        // Spray Cooldown - Set player's spray cooldown to specified amount (in seconds)
        hud.registerAction("scld", new HUDActionCommand() {
            static final String help = "usage: scld <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    float scld = Float.parseFloat(cmd[1]);
                    player.setSprayCooldown((long)(scld*1000f));
                    return "Set player's spray cooldown to " + player.getSprayCooldown();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's spray cooldown to specified amount (in seconds)";
            }
        });

        // Spray Max - Set player's max spray count to specified amount
        hud.registerAction("smax", new HUDActionCommand() {
            static final String help = "usage: smax <amount>";

            @Override
            public String execute(String[] cmd) {
                try {
                    int smax = Integer.parseInt(cmd[1]);
                    player.setMaxSprays(smax);
                    return "Set player's max spray count to " + player.getMaxSprays();
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return "set the player's max spray count to specified amount";
            }
        });
    }
}
