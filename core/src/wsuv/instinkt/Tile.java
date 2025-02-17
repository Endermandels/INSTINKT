package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class Tile {

    public enum DistanceType {
        PLAYER,
        BERRIES,
        EXIT,
        SPRAYED_EXIT,
    }

    public static final float INF = 999999999f;

    private TextureRegion img;
    private StinkEffect stinkEffect;

    private float imgX;
    private float imgY;

    // Tile coordinates
    private int x;
    private int y;

    private float[] distances; // For Dijkstra's Algorithm

    private boolean containsObstacle;
    private boolean stinky;

    private long timeStinked;
    private long stinkDuration;
    private float stinkPower;

    private ArrayList<Enemy> enemies;

    public Tile(Game game, int x, int y, float imgX, float imgY, int ssRow, int ssCol) {
        this.imgX = imgX;
        this.imgY = imgY;
        this.x = x;
        this.y = y;

        enemies = new ArrayList<>();
        stinkEffect = new StinkEffect(game, x, y);

        containsObstacle = false;
        stinky = false;
        timeStinked = -1L;
        stinkDuration = 1000L;
        stinkPower = 0;
        distances = new float[DistanceType.values().length];
        setDistance(DistanceType.PLAYER, INF);
        setDistance(DistanceType.BERRIES, INF);

        Texture spriteSheetImg = game.am.get(Game.RSC_SS_GRASS_IMG);
        img = new TextureRegion(spriteSheetImg
                , ssCol * PlayScreen.TILE_SIZE
                , ssRow * PlayScreen.TILE_SIZE
                , PlayScreen.TILE_SIZE
                , PlayScreen.TILE_SIZE);
    }

    /**
     * @return Whether the tile changed from stinky to not
     */
    public boolean update() {
        stinkEffect.update();
        if (stinky && System.currentTimeMillis() > timeStinked + stinkDuration) {
            stinky = false;
            stinkPower = 0f;
            return true;
        }

        if (!stinky) stinkPower = 0f;

        return false;
    }

    public String toString() {
        return "Tile @ [" + x + ", " + y + "]";
    }

    public void setContainsObstacle(boolean containsObstacle) {
        this.containsObstacle = containsObstacle;
    }

    public boolean isObstacle() {
        return containsObstacle;
    }

    public void setStinky(boolean stinky, long duration, float stinkPower) {
        this.stinky = stinky;
        if (stinky) {
            stinkEffect.show(duration);
            timeStinked = System.currentTimeMillis();
        } else {
            timeStinked = -1L;
        }
        stinkDuration = duration;
        this.stinkPower = stinkPower;
    }

    public boolean isStinky() {
        return stinky;
    }

    public float getStinkPower() {
        return stinkPower;
    }

    public StinkEffect getStinkEffect() {
        return stinkEffect;
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public TextureRegion getImg() {
        return img;
    }

    public float getImgX() {
        return imgX;
    }

    public float getImgY() {
        return imgY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setDistance(DistanceType dt, float val) {
        distances[dt.ordinal()] = val;
    }

    public float getDistance(DistanceType dt) {
        return distances[dt.ordinal()];
    }
}
