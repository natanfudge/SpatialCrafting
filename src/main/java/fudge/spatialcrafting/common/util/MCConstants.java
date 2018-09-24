package fudge.spatialcrafting.common.util;


public final class MCConstants {

    // SetBlockState argument
    public static final int BLOCK_UPDATE = 1;
    public static final int NOTIFY_CLIENT = 2;
    // Block hardness
    public static final Float UNBREAKABLE = -1.0F;
    public static final float BASE_HARDNESS = 1.8F;
    // Block explosion resistance
    public static final Float INDESTRUCTIBLE = 6000000.0F;
    // Required permission for command
    public static final int HIGHEST = 4;
    public static final int LOWEST = 0;
    public static final int NORMAL_ITEMSTACK_LIMIT = 64;
    public static final int TICKS_PER_SECOND = 20;

    private MCConstants() {}


}
