package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.Random;

public class Game extends com.badlogic.gdx.Game {
    // Images
    public static final String RSC_GAMEOVER_IMG = "Images/gameover.png";
    public static final String RSC_PRESSAKEY_IMG = "Images/pressakey.png";
    public static final String RSC_OVERLAY_IMG = "Images/Overlay Image.png";
    public static final String RSC_AOE_EFFECT_IMG = "Images/AOE Effect.png";
    public static final String RSC_GUI_AREA_IMG = "Images/CrimsonFantasyGUI/GUI area.png";

    // Sprite Sheets
    public static final String RSC_SS_GRASS_IMG = "Images/Tileset Grass.png";
    public static final String RSC_SS_PLANTS_IMG = "Images/TX Plant.png";
    public static final String RSC_SS_PLANTS_SHADOW_IMG = "Images/TX Shadow Plant.png";
    public static final String RSC_SS_BERRIES_IMG = "Images/TX Berries.png";
    public static final String RSC_SS_SKUNK_IMG = "Images/Sprite Sheet Skunk.png";
    public static final String RSC_SS_FOX_IMG = "Images/Fox Sprite Sheet.png";
    public static final String RSC_SS_SPRAY_IMG = "Images/Spray Sprite Sheet.png";
    public static final String RSC_SS_SQUIRREL_IMG = "Images/Squirrel Sprite Sheet.png";
    public static final String RSC_SS_COBRA_IMG = "Images/Cobra Sprite Sheet.png";
    public static final String RSC_SS_HEALTH_BAR_IMG = "Images/CrimsonFantasyGUI/AnimationSheets/MinimumDamage-Sheet.png";
    public static final String RSC_SS_SPRAY_BAR_IMG = "Images/CrimsonFantasyGUI/AnimationSheets/ManaFading-Sheet.png";
    public static final String RSC_SS_SHOP_SELECT_IMG = "Images/CrimsonFantasyGUI/Shop select sprite sheet.png";

    // Fonts
    public static final String RSC_DPCOMIC_FONT_FILE = "dpcomic.ttf";
    public static final String RSC_DPCOMIC_FONT = "DPComic.ttf";
    public static final String RSC_DPCOMIC_FONT_GUI = "DPComic_GUI.ttf";
    public static final String RSC_DPCOMIC_FONT_BLACK = "DPComic_Black.ttf";
    public static final String RSC_DPCOMIC_FONT_BIG = "DPComic_Big.ttf";

    // SFX
    public static final String RSC_SQUIRREL_NOISE_SFX = "SFX/squirrel noise 1.mp3";
    public static final String RSC_SQUIRREL_NOISE_2_SFX = "SFX/squirrel noise 2.mp3";
    public static final String RSC_SQUIRREL_NOISE_3_SFX = "SFX/squirrel noise 4.mp3";
    public static final String RSC_SNAKE_NOISE_SFX = "SFX/snake noise 1.mp3";
    public static final String RSC_SNAKE_NOISE_2_SFX = "SFX/snake noise 2.mp3";
    public static final String RSC_SPRAY_SFX = "SFX/spray.mp3";

    public AssetManager am;  // AssetManager provides a single source for loaded resources
    public SpriteBatch batch;

    public Random random = new Random();

    public Music battleMusic;
    public Music menuMusic;
    public Music cooldownMusic;
    public Music victoryMusic;

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

        // Black Font
        FreetypeFontLoader.FreeTypeFontLoaderParameter myFontBlack = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFontBlack.fontFileName = RSC_DPCOMIC_FONT_FILE;
        myFontBlack.fontParameters.size = 20;
        myFontBlack.fontParameters.color = new Color(Color.BLACK);
        am.load(RSC_DPCOMIC_FONT_BLACK, BitmapFont.class, myFontBlack);

        // GUI Font
        FreetypeFontLoader.FreeTypeFontLoaderParameter myFontGUI = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFontGUI.fontFileName = RSC_DPCOMIC_FONT_FILE;
        myFontGUI.fontParameters.size = 64;
        am.load(RSC_DPCOMIC_FONT_GUI, BitmapFont.class, myFontGUI);

        // Load Textures after the font...
        am.load(RSC_GAMEOVER_IMG, Texture.class);
        am.load(RSC_PRESSAKEY_IMG, Texture.class);
        am.load(RSC_OVERLAY_IMG, Texture.class);
        am.load(RSC_AOE_EFFECT_IMG, Texture.class);
        am.load(RSC_GUI_AREA_IMG, Texture.class);
        am.load(RSC_SS_GRASS_IMG, Texture.class);
        am.load(RSC_SS_PLANTS_IMG, Texture.class);
        am.load(RSC_SS_PLANTS_SHADOW_IMG, Texture.class);
        am.load(RSC_SS_BERRIES_IMG, Texture.class);
        am.load(RSC_SS_SKUNK_IMG, Texture.class);
        am.load(RSC_SS_SPRAY_IMG, Texture.class);
        am.load(RSC_SS_FOX_IMG, Texture.class);
        am.load(RSC_SS_SQUIRREL_IMG, Texture.class);
        am.load(RSC_SS_COBRA_IMG, Texture.class);
        am.load(RSC_SS_HEALTH_BAR_IMG, Texture.class);
        am.load(RSC_SS_SPRAY_BAR_IMG, Texture.class);
        am.load(RSC_SS_SHOP_SELECT_IMG, Texture.class);

        // Load SFX
        am.load(RSC_SQUIRREL_NOISE_SFX, Sound.class);
        am.load(RSC_SQUIRREL_NOISE_2_SFX, Sound.class);
        am.load(RSC_SQUIRREL_NOISE_3_SFX, Sound.class);
        am.load(RSC_SNAKE_NOISE_SFX, Sound.class);
        am.load(RSC_SNAKE_NOISE_2_SFX, Sound.class);
        am.load(RSC_SPRAY_SFX, Sound.class);

        batch = new SpriteBatch();
        setScreen(new LoadScreen(this));

        battleMusic = Gdx.audio.newMusic(Gdx.files.internal("SFX/Essence of Battle.mp3"));
        battleMusic.setLooping(true);
        battleMusic.setVolume(0.05f);

        cooldownMusic = Gdx.audio.newMusic(Gdx.files.internal("SFX/Going Somewhere Slow.mp3"));
        cooldownMusic.setLooping(true);
        cooldownMusic.setVolume(0.1f);

        victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("SFX/victory.mp3"));
        victoryMusic.setLooping(true);
        victoryMusic.setVolume(0.1f);

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("SFX/Jizzy Jazz.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.1f);
        menuMusic.play();
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
    }

    // VARIOUS USEFUL FUNCTIONS
    public Tile findTile(Tile[][] tileMap, int tileX, int tileY, int rowOffset, int colOffset) {
        if (!(rowOffset + tileY >= tileMap.length || rowOffset + tileY < 0)) {
            if (!(colOffset + tileX >= tileMap[0].length || colOffset + tileX < 0)) {
                Tile target = tileMap[rowOffset + tileY][colOffset + tileX];
                if (!target.isObstacle()) return target;
            }
        }
        return tileMap[tileY][tileX];
    }



    public boolean validMove(int tileX, int tileY) {
        return tileX >= 0 && tileX < PlayScreen.TILE_COLS && tileY >= 0 && tileY < PlayScreen.TILE_ROWS;
    }

    /**
     * Returns whether the given tile position is a valid move from the current tile position of the enemy
     */
    public boolean validMove(Tile[][] tileMap, int tileX, int tileY) {
        return tileX >= 0 && tileX < PlayScreen.TILE_COLS && tileY >= 0 && tileY < PlayScreen.TILE_ROWS
                && !tileMap[tileY][tileX].isObstacle();
    }
}