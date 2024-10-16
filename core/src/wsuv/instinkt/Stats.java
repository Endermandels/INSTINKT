package wsuv.instinkt;

public class Stats {

    private int maxHP;
    private int startHP;
    private int startATK;

    private int hp;
    private int atk;

    private long atkInv; // Attack invincibility
    private long lastAttacked;

    public Stats(int hp, int atk, long atkInv) {
        this.maxHP = hp;
        startHP = hp;
        this.hp = hp;
        this.atk = atk;
        startATK = atk;
        this.atkInv = atkInv;
        lastAttacked = 0;
    }

    public void getAttacked(Stats opponentStats) {
        if (opponentStats.isDead() || isDead()) return;
        long curTime = System.currentTimeMillis();
        if (curTime - lastAttacked > atkInv) {
            lastAttacked = curTime;
            hp -= opponentStats.getAtk();
            if (hp < 0) hp = 0;
        }
    }

    public void reset() {
        atk = startATK;
        maxHP = startHP;
        hp = maxHP;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(hp, maxHP));
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = Math.max(0, maxHP);
        hp = maxHP;
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        this.atk = Math.max(0, atk);
    }

    public boolean isDead() { return hp <=0; }

}

