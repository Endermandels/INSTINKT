package wsuv.bounce;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Tile {

    private ArrayList<Object> entities;
    private Texture tile_img;

    public Tile(Game game) {
        entities = new ArrayList<Object>();
    }
}
