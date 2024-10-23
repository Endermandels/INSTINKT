package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Berry {

    private TextureRegion img;
    private float imgX, imgY, targetX, targetY;
    private float velocity;
    private float scale;

    private float delay;
    private float delayTimer;

    public Berry(Game game, float imgX, float imgY, float targetX, float targetY) {
        this(game, imgX, imgY, targetX, targetY, 0f);
    }

    public Berry(Game game, float imgX, float imgY, float targetX, float targetY, float delay) {
        if (game.random.nextBoolean())
            img = new TextureRegion((Texture) game.am.get(Game.RSC_SS_BERRIES_IMG),
                0, 32, 32, 32);
        else
            img = new TextureRegion((Texture) game.am.get(Game.RSC_SS_BERRIES_IMG),
                    32, 32, 32, 32);
        this.imgX = imgX;
        this.imgY = imgY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.delay = delay;
        delayTimer = 0f;
        scale = 2f;
    }

    /**
     * @return Whether the berry has arrived
     */
    public boolean update(float delta) {
        if (delayTimer >= delay) {
            float prevX = imgX;
            float prevY = imgY;

            velocity = Math.min(velocity + 0.5f, 20f);
            scale = Math.max(scale-0.05f, 1f);
            if (imgX < targetX) imgX += velocity;
            if (imgX > targetX) imgX -= velocity;
            if (imgY < targetY) imgY += velocity;
            if (imgY > targetY) imgY -= velocity;
            return prevX == imgX && prevY == imgY;
        }
        delayTimer += delta;
        return false;
    }

    public void draw(Batch batch) {
        if (delayTimer >= delay) {
            batch.draw(img, imgX-img.getRegionWidth()*(scale-1f)
                    , imgY-img.getRegionHeight()*(scale-1f)
                    , img.getRegionWidth() * PlayScreen.TILE_SCALE*scale
                    , img.getRegionHeight() * PlayScreen.TILE_SCALE*scale);
        }
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }
}
