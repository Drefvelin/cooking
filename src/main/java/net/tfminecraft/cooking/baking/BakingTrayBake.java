package net.tfminecraft.cooking.baking;

public final class BakingTrayBake {
    private final int cookSeconds;
    private final int burnSeconds;
    private final boolean sync;

    public BakingTrayBake(int cookSeconds, int burnSeconds, boolean sync) {
        this.cookSeconds = cookSeconds;
        this.burnSeconds = burnSeconds;
        this.sync = sync;
    }

    public int getCookSeconds() {
        return cookSeconds;
    }

    public int getBurnSeconds() {
        return burnSeconds;
    }

    public boolean isSync() {
        return sync;
    }
}
