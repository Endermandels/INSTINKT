package wsuv.instinkt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;

public class Player {

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
    private int dirX;
    private int dirY;

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
        imgSpeed = 200f;

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
        if (upInput) {
            targetTile = game.findTile(tileMap,tileX,tileY,1, 0);
        }
        if (downInput) {
            targetTile = game.findTile(tileMap,tileX,tileY,-1, 0);
        }
        if (leftInput) {
            targetTile = game.findTile(tileMap,tileX,tileY,0, -1);
        }
        if (rightInput) {
            targetTile = game.findTile(tileMap,tileX,tileY,0, 1);
        }

        if (targetTile != null && imgX > targetTile.getImgX()) {
            imgX -= imgSpeed * time;

            if (imgX < targetTile.getImgX()) {
                // Moved passed the tile, center on the tile
                imgX = targetTile.getImgX();
                tileX = targetTile.getX();
                targetTile = null;
            }
        }
        else if (targetTile != null && imgX < targetTile.getImgX()) {
            imgX += imgSpeed * time;

            if (imgX > targetTile.getImgX()) {
                // Moved passed the tile, center on the tile
                imgX = targetTile.getImgX();
                tileX = targetTile.getX();
                targetTile = null;
            }
        }
        else if (targetTile != null && imgY < targetTile.getImgY()) {
            imgY += imgSpeed * time;

            if (imgY > targetTile.getImgY()) {
                // Moved passed the tile, center on the tile
                imgY = targetTile.getImgY();
                tileY = targetTile.getY();
                targetTile = null;
            }
        }
        else if (targetTile != null && imgY > targetTile.getImgY()) {
            imgY -= imgSpeed * time;

            if (imgY < targetTile.getImgY()) {
                // Moved passed the tile, center on the tile
                imgY = targetTile.getImgY();
                tileY = targetTile.getY();
                targetTile = null;
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
