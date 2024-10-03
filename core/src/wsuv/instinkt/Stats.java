package wsuv.instinkt;

public class Stats {

    private int hp;
    private int atk;

    private long atkInv; // Attack invincibility
    private long lastAttacked;

    public Stats(int hp, int atk, long atkInv) {
        this.hp = hp;
        this.atk = atk;
        this.atkInv = atkInv;
        lastAttacked = 0;
    }

    public void getAttacked(Stats opponentStats) {
        long curTime = System.currentTimeMillis();
        if (curTime - lastAttacked > atkInv) {
            lastAttacked = curTime;
            hp -= opponentStats.getAtk();
        }
    }

    public int getHp() {
        return hp;
    }

    public int getAtk() {
        return atk;
    }
}
