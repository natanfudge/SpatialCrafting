package fudge.spatialcrafting.client.tick;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class ClientTicker {

    private static List<Ticker> tickers = new LinkedList<>();
    private static long ticks = 0;
    private static boolean inWorld = false;

    private ClientTicker() {}

    /**
     * Add a client-sided ticker to do an action every so often for a certain duration.
     *
     * @param action   The action to be done every so often. It may use the time that has passed in the action.
     * @param delay    The time between each action.
     * @param duration The total time the action will be repeated.
     * @param name     The name of the action. Used for stopTickers.
     */
    public static void addTicker(Consumer<Integer> action, int delay, int duration, String name) {
        tickers.add(new Ticker(action, delay, duration, ticks, false, name));
    }

    /**
     * Stop all tickers of a specific type from ticking.
     *
     * @param name The name of the tickers to be stopped
     */
    public static void stopTickers(String name) {
        tickers.forEach(ticker -> {
            if (ticker.id.equals(name)) {
                ticker.isDone = true;
            }
        });
    }

    /**
     * Called twice every client-sided tick (every second has 20 ticks). Once at the start and once at the end of the tick.
     */
    @SubscribeEvent
    public static void onTickStartAndEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START || !inWorld || tickers.isEmpty()) return;

        tick();
    }

    private static void tick() {
        tickers.removeIf(ticker -> ticker.isDone);

        tickers.forEach(ticker -> {

            // Delay time has passed
            Integer ticksPassed = (int) (ticks - ticker.startTime);
            if ((ticksPassed) % ticker.delay == 0) {
                ticker.action.accept(ticksPassed);
            }

            // Total time has passed
            if (ticks - ticker.startTime >= ticker.duration) {
                ticker.isDone = true;
            }
        });


        ticks++;

    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        inWorld = true;
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Unload event) {
        inWorld = false;
    }


}
