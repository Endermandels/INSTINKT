package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Player {

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Game game;
    private AnimationManager am;

    // Track the position of the player's image separately from tile coordinates
    private float imgX;
    private float imgY;
    private float imgSpeed;

    // Tile coordinates
    private int tileX;
    private int tileY;

    private Tile targetTile; // When the player is moving
    private boolean movingHorizontal;
    private boolean movingVertical;
    private boolean[] moveRequests; // Requests in each of the four directions
    private boolean prioritizeHorizontal;

    public Player(Game game, int tileX, int tileY) {
        this.game = game;
        am = new AnimationManager(game.am.get(Game.RSC_SS_SKUNK_IMG)
                , new ArrayList<Integer>(Arrays.asList(6,8,5,4,7))
                , new HashMap<String, Integer>() {{
                    put("IDLE", 0);
                    put("RUN", 1);
                    put("SPRAY", 2);
                    put("HURT", 3);
                    put("DEAD", 4);
                    }}
                , 0.08f, 32, 32
        );

        imgX = 0;
        imgY = 0;
        imgSpeed = 400f;
        movingHorizontal = false;
        movingVertical = false;
        moveRequests = new boolean[4];
        prioritizeHorizontal = false;

        this.tileX = tileX;
        this.tileY = tileY;
        targetTile = null;
    }


    private void move(Tile[][] tileMap) {
        float time = Gdx.graphics.getDeltaTime();
        if (time > 1f) time = 1f / 60f;

        boolean leftInput = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightInput = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean upInput = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downInput = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if (leftInput) moveRequests[Direction.LEFT.ordinal()] = true;
        if (rightInput) moveRequests[Direction.RIGHT.ordinal()] = true;
        if (upInput) moveRequests[Direction.UP.ordinal()] = true;
        if (downInput) moveRequests[Direction.DOWN.ordinal()] = true;

        if (!movingHorizontal && (moveRequests[Direction.UP.ordinal()] || moveRequests[Direction.DOWN.ordinal()])
                && (!(moveRequests[Direction.LEFT.ordinal()] || moveRequests[Direction.RIGHT.ordinal()])
                || !prioritizeHorizontal)) {
            if (moveRequests[Direction.UP.ordinal()]) {
                targetTile = game.findTile(tileMap,tileX,tileY,1, 0);
                if (targetTile.getY() == tileY) targetTile = null;
            }
            if (moveRequests[Direction.DOWN.ordinal()]) {
                targetTile = game.findTile(tileMap,tileX,tileY,-1, 0);
                if (targetTile.getY() == tileY) targetTile = null;
            }
            if (targetTile != null) {
                movingVertical = true;
                prioritizeHorizontal = true;
            }
            moveRequests[Direction.UP.ordinal()] = false;
            moveRequests[Direction.DOWN.ordinal()] = false;
        } else if (!movingVertical) {
            if (moveRequests[Direction.LEFT.ordinal()]) {
                targetTile = game.findTile(tileMap,tileX,tileY,0, -1);
                if (targetTile.getX() == tileX) targetTile = null;
            }
            if (moveRequests[Direction.RIGHT.ordinal()]) {
                targetTile = game.findTile(tileMap,tileX,tileY,0, 1);
                if (targetTile.getX() == tileX) targetTile = null;
            }
            if (targetTile != null) {
                movingHorizontal = true;
                prioritizeHorizontal = false;
            }
            moveRequests[Direction.LEFT.ordinal()] = false;
            moveRequests[Direction.RIGHT.ordinal()] = false;
        }

        if (targetTile != null && imgX > targetTile.getImgX()) {
            // LEFT
            imgX -= imgSpeed * time;

            if (imgX < targetTile.getImgX()) {
                // Moved passed the tile, center on the tile
                imgX = targetTile.getImgX();
                tileX = targetTile.getX();
                targetTile = null;
                movingHorizontal = false;
                moveRequests[Direction.LEFT.ordinal()] = false;
            }
        }
        else if (targetTile != null && imgX < targetTile.getImgX()) {
            // RIGHT
            imgX += imgSpeed * time;

            if (imgX > targetTile.getImgX()) {
                // Moved passed the tile, center on the tile
                imgX = targetTile.getImgX();
                tileX = targetTile.getX();
                targetTile = null;
                movingHorizontal = false;
                moveRequests[Direction.RIGHT.ordinal()] = false;
            }
        }
        else if (targetTile != null && imgY < targetTile.getImgY()) {
            // UP
            imgY += imgSpeed * time;

            if (imgY > targetTile.getImgY()) {
                // Moved passed the tile, center on the tile
                imgY = targetTile.getImgY();
                tileY = targetTile.getY();
                targetTile = null;
                movingVertical = false;
                moveRequests[Direction.UP.ordinal()] = false;
            }
        }
        else if (targetTile != null && imgY > targetTile.getImgY()) {
            // DOWN
            imgY -= imgSpeed * time;

            if (imgY < targetTile.getImgY()) {
                // Moved passed the tile, center on the tile
                imgY = targetTile.getImgY();
                tileY = targetTile.getY();
                targetTile = null;
                movingVertical = false;
                moveRequests[Direction.DOWN.ordinal()] = false;
            }
        }
        else {
            targetTile = null;
        }
    }

    public void update(Tile[][] tileMap) {
        am.update();
        move(tileMap);
    }

    public TextureRegion getImg() {
        return am.getCurrentImage();
    }

    public AnimationManager getAm() {
        return am;
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
