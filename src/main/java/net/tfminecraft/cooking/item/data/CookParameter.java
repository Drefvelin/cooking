package net.tfminecraft.cooking.item.data;

public class CookParameter {
    private int tag;
    private int time;
    private int burntime;

    public CookParameter(int tag, int time, int burntime) {
        this.tag = tag;
        this.time = time;
        this.burntime = burntime;
    }
 
    public int getTag() {
        return tag;
    }
    public int getTime() {
        return time;
    }
    public int getBurnTime() {
        return burntime;
    }
}
