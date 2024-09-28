package wsuv.instinkt;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayScreen extends ScreenAdapter {
    private enum SubState {READY, GAME_OVER, PLAYING}
    private Game game;
    private HUD hud;
    private Player player;
    private SubState state;
    private ArrayList<Obstacle> obstacles;

    private Tile[][] tileMap;
    private final int TILE_ROWS = 12;
    private final int TILE_COLS = 18;

    public static int TILE_SIZE = 32;
    public static int TILE_SCALE = 2;

    // Switching between Game Over and Ready
    private final float TIMER_MAX = 3.0f;
    private float timer;

    private boolean paused;
    private boolean doStep; // Stepping through update cycles while paused

    public PlayScreen(Game game) {
        this.game = game;
        hud = new HUD(12, 13, 10, 500, game.am.get(Game.RSC_DPCOMIC_FONT_BLACK));
        tileMap = new Tile[TILE_ROWS][TILE_COLS];
        populateTileMap();

        player = new Player(game,0,0);

        obstacles = new ArrayList<>();
        addObstaclesToTileMap();

        timer = 0f;
        paused = false;
        doStep = false;

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

        // Animation - Change the animation of the player to specified state
        hud.registerAction("anim", new HUDActionCommand() {
            static final String help = "usage: anim <state>";

            @Override
            public String execute(String[] cmd) {
                try {
                    player.getAm().switchAnimState(cmd[1].toUpperCase());
                    return "ok!";
                } catch (Exception e) {
                    return "available states: IDLE, RUN, SPRAY, HURT, DEAD";
                }
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

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
        state = SubState.READY;
    }

    /**
     * Reads from tile_map.txt to determine which tile sprites are drawn
     */
    private void populateTileMap() {
        try (BufferedReader br = new BufferedReader(new FileReader("tile_map.txt"))) {
            String line;
            char txtRow;
            char txtCol;
            int y = TILE_ROWS-1;
            int ssRow, ssCol;

            while ((line = br.readLine()) != null && y >= 0) {
                for (int x = 0; x < line.length(); x+=3) {
                    txtRow = line.charAt(x);
                    txtCol = line.charAt(x+1);
                    ssRow = Character.getNumericValue(txtRow);
                    ssCol = Character.getNumericValue(txtCol);

                    tileMap[y][x/3] = new Tile(game, x/3, y, (x/3f)*TILE_SIZE*TILE_SCALE, y*TILE_SIZE*TILE_SCALE
                            , ssRow, ssCol);
                }
                y--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void spawnObstacleAt(int row, int col) {
        Obstacle obs = new Obstacle(game, col*TILE_SIZE*TILE_SCALE, row*TILE_SIZE*TILE_SCALE);
        obstacles.add(obs);
        tileMap[row][col].setContainsObstacle(true);
    }

    private void addObstaclesToTileMap() {
        spawnObstacleAt(0,1);
        spawnObstacleAt(0,2);
        spawnObstacleAt(0,3);

        spawnObstacleAt(1,3);
        spawnObstacleAt(2,3);

    }

    public void update(float delta) {
        if (!paused || doStep) {
            switch (state) {
                case PLAYING:
                    player.update(tileMap);
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
                                , TILE_SIZE * TILE_SCALE, TILE_SIZE * TILE_SCALE);
                    }
                // Draw Player
                game.batch.draw(player.getImg(), player.getImgX(), player.getImgY() + 16f
                        , TILE_SIZE * TILE_SCALE, TILE_SIZE * TILE_SCALE);
                // Draw Obstacles
                for (Obstacle obs : obstacles) {
                    TextureRegion img = obs.getImg();
                    game.batch.draw(img, obs.getImgX(), obs.getImgY()
                            , img.getRegionWidth()*TILE_SCALE, img.getRegionHeight()*TILE_SCALE);
                }
                break;
        }
        hud.draw(game.batch);
        game.batch.end();
    }
}

