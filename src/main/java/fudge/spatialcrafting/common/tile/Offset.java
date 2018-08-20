package fudge.spatialcrafting.common.tile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class Offset extends Vec3i {
    public static final Offset NONE = new Offset(0, 0, 0);
    private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final int Y_SHIFT = 0 + NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    public Offset(int xIn, int yIn, int zIn) {
        super(xIn, yIn, zIn);
    }

    public Offset(BlockPos slavePos, BlockPos masterPos){
        super( slavePos.getX() - masterPos.getX(), slavePos.getY() - masterPos.getY() ,slavePos.getZ() - masterPos.getZ() );
    }


    /**
     * Serialize this Offset into a long value
     */
    public long toLong()
    {
        return ((long)this.getX() & X_MASK) << X_SHIFT | ((long)this.getY() & Y_MASK) << Y_SHIFT | ((long) this.getZ() & Z_MASK);
    }


    /**
     * Create an Offset from a serialized long value (created by toLong)
     */
    public static Offset fromLong(long serialized)
    {
        int x = (int)(serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
        int y = (int)(serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
        int z = (int)(serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
        return new Offset(x, y, z);
    }


    public BlockPos adjustToMaster(Vec3i slavePos){
        return new BlockPos(slavePos.getX() - this.getX(), slavePos.getY() - this.getY(), slavePos.getZ() - this.getZ());
    }

}
