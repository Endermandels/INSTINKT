package wsuv.instinkt;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class PlayScreen extends ScreenAdapter {
    private enum SubState {READY, GAME_OVER, PLAYING}
    private Game game;
    private HUD hud;
    private Player player;
    private SubState state;

    private Tile[][] tileMap;
    private final int TILE_ROWS = 23;
    private final int TILE_COLS = 37;

    // Switching between Game Over and Ready
    private final float TIMER_MAX = 3.0f;
    private float timer;

    private boolean paused;
    private boolean doStep; // Stepping through update cycles while paused

    public PlayScreen(Game game) {
        this.game = game;
        hud = new HUD(16, 13, 10, 500, game.am.get(Game.RSC_DPCOMIC_FONT));
        tileMap = new Tile[TILE_ROWS][TILE_COLS];
        populateTileMap(tileMap);

        player = new Player(game);

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

    private void populateTileMap(Tile[][] tileMap) {
        for (int row = 0; row < TILE_ROWS; row++)
            for(int col = 0; col < TILE_COLS; col++) {
                tileMap[row][col] = new Tile(game, 3,0);
            }
    }

    public void update(float delta) {
        if (!paused || doStep) {
            // Playing
            if (state == SubState.PLAYING) {
                // TODO: Make playing state
            }
            // Game Over
            if (state == SubState.GAME_OVER) {
                timer += delta;
            }
            // Ready
            if (state == SubState.GAME_OVER && timer > TIMER_MAX) {
                state = SubState.READY;
            }
            // ignore key presses when console is open...
            if (!hud.isOpen()) {
                if (state == SubState.READY && Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) && !Gdx.input.isKeyPressed(Input.Keys.GRAVE)) {
                    state = SubState.PLAYING;
                }
                else if (state == SubState.PLAYING && Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    state = SubState.GAME_OVER;
                    timer = 0;
                }
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
                        game.batch.draw(tile.getImg(), col * Tile.SIZE, row * Tile.SIZE);
                    }
                // Draw Player
                game.batch.draw(player.getImg(), 0, 0);
                break;
        }
        hud.draw(game.batch);
        game.batch.end();
    }
}

