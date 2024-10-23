package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class HowToPlayScreen extends ScreenAdapter {

    private final String TITLE = "How to Play";

    Game game;
    int frames;
    BitmapFont font;
    BitmapFont font_big;
    String[] credits;
    boolean wasSpacePressed;

    public HowToPlayScreen(Game game) {
        this.game = game;

        font = game.am.get(Game.RSC_DPCOMIC_FONT);
        font_big = game.am.get(Game.RSC_DPCOMIC_FONT_BIG);

        wasSpacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        frames = 0;
        FileHandle file = Gdx.files.internal("Text/how_to_play.txt");
        credits = file.readString().split("\n");
    }

    @Override
    public void show() {
        Gdx.app.log("HowToPlayScreen", "show");
    }

    public void render(float delta) {

        ScreenUtils.clear(0, 0, 0, 1);

        if (!wasSpacePressed && Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            game.setScreen(new PlayScreen(game));
        } else {
            // once the font is loaded, start showing credits.
            // we'll assume a fairly smooth framerate at just start scrolling
            // at a fixed rate per frame until all credits are off screen
            // then we'll switch to the playing state.
            game.batch.begin();
            float y = 600f;
            float x = Gdx.graphics.getWidth() / 2f - 420f;
            float lineHeight = font.getLineHeight() + 6f;
            font_big.draw(game.batch, TITLE, x, y + 2f * lineHeight + 80f);

            for(int i = 0; i < credits.length; i++) {
                font.draw(game.batch, credits[i], x, y);
                y -= lineHeight;
            }
            game.batch.end();
        }

        if (wasSpacePressed && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            wasSpacePressed = false;
        }
    }
}
