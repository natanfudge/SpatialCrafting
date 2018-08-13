package fudge.spatialcrafting.client.tick;

import java.util.function.Consumer;

public class Ticker {
    public Consumer<Integer> action;
    public int delay;
    public int duration;
    public long startTime;
    public boolean isDone;
    public String id;


    public Ticker(Consumer<Integer> action, int delay, int duration, long startTime, boolean isDone, String id) {
        this.action = action;
        this.delay = delay;
        this.duration = duration;
        this.startTime = startTime;
        this.isDone = isDone;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Ticker)) {
            return false;
        }

        return ((Ticker) other).id.equals(this.id);
    }
}
