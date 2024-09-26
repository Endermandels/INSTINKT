package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadScreen extends ScreenAdapter {
    private final String TITLE = "inSTINKt";

    Game game;
    int frames;
    BitmapFont font;
    BitmapFont font_big;
    int scrollrate;
    int linesShown;
    String[] credits;

    public LoadScreen(Game game) {
        this.game = game;
        linesShown = 10;
        scrollrate = 5;  // this is currently pixels/frame, not pixels/sec!
        // TODO: Skippable load screen

        // really our app will load quickly, but let's
        // fake a more complicated system... we'll wait
        // until we show all the credits...
        frames = 0;
        FileHandle file = Gdx.files.internal("credits.txt");
        credits = file.readString().split("\n");
    }

    @Override
    public void show() {
        Gdx.app.log("LoadScreen", "show");
    }

    public void render(float delta) {

        int credits_offset = frames < 120 ? 0 : (frames - 120) / scrollrate;
        ScreenUtils.clear(0, 0, 0, 1);
        // let the AssetManager load for 15 milliseconds (~1 frame)
        // this happens in another thread
        game.am.update(10);

        if (font == null
                && game.am.isLoaded(Game.RSC_DPCOMIC_FONT)
                && game.am.isLoaded(Game.RSC_DPCOMIC_FONT_BIG)) {
            font = game.am.get(Game.RSC_DPCOMIC_FONT);
            font_big = game.am.get(Game.RSC_DPCOMIC_FONT_BIG);
        } else if (game.am.isFinished() && (credits_offset >= credits.length || Gdx.input.isKeyPressed(Input.Keys.SPACE)) ) {
            game.setScreen(new PlayScreen(game));
        } else if (font != null) {
            // once the font is loaded, start showing credits.
            // we'll assume a fairly smooth framerate at just start scrolling
            // at a fixed rate per frame until all credits are off screen
            // then we'll switch to the playing state.
            game.batch.begin();
            float y = 300;
            float x = (float) Gdx.graphics.getWidth() / 2 - 200f;
            float lineHeight = font.getLineHeight();
            font_big.draw(game.batch, TITLE, x, y + 2 * lineHeight + 120);
            font.draw(game.batch, "By Elijah Delavar", x, y + 2 * lineHeight + 60);
            font.draw(game.batch, "Thanks to...", x, y + 2 * lineHeight);

            for(int i = 0; i < linesShown && (credits_offset + i) < credits.length; i++) {
                font.draw(game.batch, credits[credits_offset + i], x, y);
                y -= lineHeight;
            }
            game.batch.end();
            frames += 1;
        }
    }
}
