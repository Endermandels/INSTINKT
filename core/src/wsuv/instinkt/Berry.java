package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Berry {

    private TextureRegion img;
    private float imgX, imgY, targetX, targetY;
    private float velocity;
    private float scale;

    public Berry(Game game, float imgX, float imgY, float targetX, float targetY) {
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
        scale = 2f;
    }

    /**
     * @return Whether the berry has arrived
     */
    public boolean update() {
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

    public void draw(Batch batch) {
        batch.draw(img, imgX-img.getRegionWidth()*(scale-1f)
                , imgY-img.getRegionHeight()*(scale-1f)
                , img.getRegionWidth() * PlayScreen.TILE_SCALE*scale
                , img.getRegionHeight() * PlayScreen.TILE_SCALE*scale);
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }
}
