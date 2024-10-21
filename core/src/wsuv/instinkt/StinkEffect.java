package wsuv.instinkt;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StinkEffect {

    private AnimationManager am;

    private int tileX, tileY;

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

        batch.draw(img,x - 20f,y - 20f, width, height);
        batch.draw(img,x,y - 30f, width, height);
        batch.draw(img, x-20f, y-40f, width, height);
    }
}
