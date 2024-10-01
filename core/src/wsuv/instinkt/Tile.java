package wsuv.instinkt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Tile {

    public enum DistanceType {PLAYER}

    public static final float INF = 10000f;

    private TextureRegion img;

    private float imgX;
    private float imgY;

    // Tile coordinates
    private int x;
    private int y;

    private float[] distances; // For Dijkstra's Algorithm

    private boolean containsObstacle;

    public Tile(Game game, int x, int y, float imgX, float imgY, int ssRow, int ssCol) {
        this.imgX = imgX;
        this.imgY = imgY;
        this.x = x;
        this.y = y;

        containsObstacle = false;
        distances = new float[1];
        setDistance(DistanceType.PLAYER, INF);

        Texture spriteSheetImg = game.am.get(Game.RSC_SS_GRASS_IMG);
        img = new TextureRegion(spriteSheetImg
                , ssCol * PlayScreen.TILE_SIZE
                , ssRow * PlayScreen.TILE_SIZE
                , PlayScreen.TILE_SIZE
                , PlayScreen.TILE_SIZE);
    }

    public void setContainsObstacle(boolean containsObstacle) {
        this.containsObstacle = containsObstacle;
    }

    public boolean isObstacle() {
        return containsObstacle;
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
