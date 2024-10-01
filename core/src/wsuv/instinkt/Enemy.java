package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Enemy extends GameObject {
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Game game;
    private AnimationManager am;

    private float imgX;
    private float imgY;
    private float imgSpeed;

    private int tileX;
    private int tileY;

    private Tile targetTile;
    private boolean movingHorizontal;
    private boolean movingVertical;
    private boolean flipped;
    private Direction dir;

    public Enemy(Game game, int tileX, int tileY) {
        super(null, 0, 0, 20);
        this.game = game;
        am = new AnimationManager(game.am.get(Game.RSC_SS_FOX_IMG)
                , new ArrayList<Integer>(Arrays.asList(5,14,8,11,5,6,7))
                , new HashMap<String, Integer>() {{
                    put("IDLE", 0);
                    put("LOOK", 1);
                    put("RUN", 2);
                    put("POUNCE", 3);
                    put("HURT", 4);
                    put("SLEEP", 5);
                    put("DEAD", 6);
                }}
                , 0.08f, 32, 32
        );

        imgX = tileX * PlayScreen.TILE_SCALED_SIZE;
        imgY = tileY * PlayScreen.TILE_SCALED_SIZE;

        imgSpeed = 400f;
        movingHorizontal = false;
        movingVertical = false;

        dir = null;
        flipped = false;

        this.tileX = tileX;
        this.tileY = tileY;
        targetTile = null;
    }


    private void move(Tile[][] tileMap) {
        float time = Gdx.graphics.getDeltaTime();
        if (time > 1f) time = 1f / 60f;

    }

    public void update(Tile[][] tileMap) {
        am.update();
//        move(tileMap);
    }

    public TextureRegion getImg() {
        return am.getCurrentImage(flipped);
    }

    public float getImgX() {
        return imgX;
    }

    public float getImgY() {
        return imgY;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }
}
