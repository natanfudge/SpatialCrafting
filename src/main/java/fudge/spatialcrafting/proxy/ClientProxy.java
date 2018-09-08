package fudge.spatialcrafting.proxy;

import fudge.spatialcrafting.client.tile.TESRHologram;
import fudge.spatialcrafting.common.tile.TileHologram;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy implements IProxy {

    @Override
    public void preInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileHologram.class, new TESRHologram());
    }

}
