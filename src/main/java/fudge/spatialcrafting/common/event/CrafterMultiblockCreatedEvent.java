package fudge.spatialcrafting.common.event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called whenever a crafter multiblock is created (and the holograms pop up).
 * Called from the server only.
 */
public class CrafterMultiblockCreatedEvent extends Event {
    private final BlockPos masterPos;
    private final World world;

    public CrafterMultiblockCreatedEvent(BlockPos masterPos, World world) {
        this.masterPos = masterPos;
        this.world = world;
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public World getWorld() {
        return world;
    }
}
