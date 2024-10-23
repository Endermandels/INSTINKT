package wsuv.instinkt;

import java.util.ArrayList;

public class EnemyFormation {

    private ArrayList<EnemyFrequencyTuple> enemiesToSpawn;
    private int idx;

    public EnemyFormation(ArrayList<EnemyFrequencyTuple> enemiesToSpawn) {
        this.enemiesToSpawn = enemiesToSpawn;
        this.idx = 0;
    }

    public Enemy getNextEnemy() {
        return idx < enemiesToSpawn.size() ? enemiesToSpawn.get(idx++).enemy.clone() : null;
    }

    public long getFrequency() {
        return idx < enemiesToSpawn.size() ? enemiesToSpawn.get(idx).frequency : -1;
    }

    public EnemyFormation reset() {
        idx = 0;
        return this;
    }
}
