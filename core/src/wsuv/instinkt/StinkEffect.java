package wsuv.instinkt;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StinkEffect {

    private AnimationManager am;

    private int tileX, tileY;

    private float transparency;
    private long duration;
    private long timeShown;

    public StinkEffect(Game game, int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;

        am = new AnimationManager(game.am.get(Game.RSC_SS_STINK_EFFECT_IMG)
                , new ArrayList<Integer>(List.of(6))
                , new HashMap<String, Integer>() {{
            put("PUFF", 0);
        }}
                , 0.1f, 48, 48
        );

        duration = -1L;
        timeShown = 1L;
        transparency = 0.5f;
    }

    public void update() {
        am.update();
    }

    public void draw(Batch batch) {
        TextureRegion img = am.getCurrentImage(false);
        int width = img.getRegionWidth() * PlayScreen.TILE_SCALE * 2;
        int height = img.getRegionHeight() * PlayScreen.TILE_SCALE * 2;
        int x = (int)(tileX*PlayScreen.TILE_SCALED_SIZE - width / 4f);
        int y = tileY*PlayScreen.TILE_SCALED_SIZE + 2*PlayScreen.TILE_SCALED_SIZE;

        Color c = batch.getColor();

        float elapsedTime = (float) (System.currentTimeMillis() - timeShown);
        float normalizedTime = Math.min(elapsedTime / duration, 1f); // Cap normTime at 1

        // e^(-k * t)
        float k = 1f; // Higher value, faster fade
        transparency = (float) Math.exp(-k * normalizedTime);

        System.out.println(transparency);

        batch.setColor(c.r, c.g, c.b, transparency);

        batch.draw(img,x - 20f,y - 20f, width, height);
        batch.draw(img,x,y - 30f, width, height);
        batch.draw(img, x-20f, y-40f, width, height);
        batch.setColor(c.r, c.g, c.b, 1f);
    }

    public void show(long duration) {
        this.duration = duration;
        timeShown = System.currentTimeMillis();
    }
}
