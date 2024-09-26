package wsuv.bounce;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.Random;

public class Game extends com.badlogic.gdx.Game {
    public static final String RSC_GAMEOVER_IMG = "gameover.png";
    public static final String RSC_PRESSAKEY_IMG = "pressakey.png";
    public static final String RSC_DPCOMIC_FONT_FILE = "dpcomic.ttf";
    public static final String RSC_DPCOMIC_FONT = "DPComic.ttf";
    public static final String RSC_DPCOMIC_FONT_BIG = "DPComic_Big.ttf";

    AssetManager am;  // AssetManager provides a single source for loaded resources
    SpriteBatch batch;

    Random random = new Random();

    Music music;
    @Override
    public void create() {
        am = new AssetManager();

		/* True Type Fonts are a bit of a pain. We need to tell the AssetManager
           a bit more than simply the file name in order to get them into an
           easily usable (BitMap) form...
		 */
        FileHandleResolver resolver = new InternalFileHandleResolver();
        am.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        am.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        // Regular Font
        FreetypeFontLoader.FreeTypeFontLoaderParameter myFont = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFont.fontFileName = RSC_DPCOMIC_FONT_FILE;
        myFont.fontParameters.size = 32;
        am.load(RSC_DPCOMIC_FONT, BitmapFont.class, myFont);

        // Big Font
        FreetypeFontLoader.FreeTypeFontLoaderParameter myFontBig = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFontBig.fontFileName = RSC_DPCOMIC_FONT_FILE;
        myFontBig.fontParameters.size = 128;
        am.load(RSC_DPCOMIC_FONT_BIG, BitmapFont.class, myFontBig);

        // Load Textures after the font...
        am.load(RSC_GAMEOVER_IMG, Texture.class);
        am.load(RSC_PRESSAKEY_IMG, Texture.class);

        batch = new SpriteBatch();
        setScreen(new LoadScreen(this));

    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
    }
}