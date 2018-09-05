package fudge.spatialcrafting.common.util;

import fudge.spatialcrafting.common.block.BlockCrafter;
import fudge.spatialcrafting.common.block.SCBlocks;
import fudge.spatialcrafting.common.data.WorldSavedDataCrafters;
import fudge.spatialcrafting.common.event.CrafterMultiblockCreatedEvent;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.tile.TileHologram;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

import static fudge.spatialcrafting.common.MCConstants.NOTIFY_CLIENT;
import static fudge.spatialcrafting.common.block.BlockCrafter.FORMED;
import static fudge.spatialcrafting.common.block.BlockHologram.ACTIVE;

@UtilityClass
public class CrafterUtil {

    private static void createMultiblock(World world, List<BlockPos> crafterList, int crafterSize) {

        BlockPos masterPos = getMasterPosFromList(crafterList);

        WorldSavedDataCrafters.addData(world, masterPos);


        for (BlockPos crafterPos : crafterList) {

            // Connect crafters to the multiblock
            // Change the crafters' blockstate and tile entity
            world.setBlockState(crafterPos, world.getBlockState(crafterPos).withProperty(FORMED, true), NOTIFY_CLIENT);
            TileCrafter tileCrafter = new TileCrafter(crafterPos, masterPos);
            world.setTileEntity(crafterPos, tileCrafter);


            for (int i = 1; i < crafterSize + 1; i++) {
                // Place hologram
                BlockPos hologramPos = crafterPos.add(0, i, 0);
                world.setBlockState(hologramPos, SCBlocks.HOLOGRAM.getBlockState().getBaseState().withProperty(ACTIVE, true));

                // Connect holograms to the multiblock
                TileHologram hologramTile = Util.getTileEntity(world, hologramPos);
                hologramTile.bindToMasterBlock(masterPos);
            }


        }


        CrafterMultiblockCreatedEvent event = new CrafterMultiblockCreatedEvent(masterPos, world);
        MinecraftForge.EVENT_BUS.post(event);


    }

    // MasterPos will be the one with the lowest x and z coordinates.
    // This way all other poses offset will start from (0,0,0) (masterPos offset) and increase as they are farther away from masterPos.
    private static BlockPos getMasterPosFromList(List<BlockPos> crafterList) {
        BlockPos masterPos = crafterList.get(0);
        for (BlockPos crafterPos : crafterList) {
            if (crafterPos.getZ() + crafterPos.getX() < masterPos.getX() + masterPos.getZ()) {
                masterPos = crafterPos;
            }
        }

        return masterPos;
    }

    /**
     * Can be called from both the client and the server.
     *
     * @param world The world to search for the master block for
     * @param pos   Returns the closest master blockPos to this pos
     * @return The closest BlockPos of the origin point of a crafter multiblock. Null if none exists.
     */
    @Nullable
    public static TileCrafter getClosestMasterBlock(World world, BlockPos pos) {
        List<BlockPos> poses = WorldSavedDataCrafters.getMasterBlocks(world);

        // There are no master blocks
        if (poses.isEmpty()) {
            return null;
        }

        // Find closest block
        BlockPos closestPos = (BlockPos) poses.toArray()[0];
        for (BlockPos currentPos : poses) {
            if (MathUtil.minimalDistanceOf(pos, currentPos) < MathUtil.minimalDistanceOf(pos, closestPos)) {
                closestPos = currentPos;
            }
        }

        return Util.getTileEntity(world, closestPos);
    }

    private static boolean spaceExists(World world, List<BlockPos> startingPoses, int crafterSize) {

        for (BlockPos blockPos : startingPoses) {
            for (int i = 1; i <= crafterSize; i++) {
                if (!world.getBlockState(blockPos.add(0, i, 0)).getBlock().equals(Blocks.AIR)) {
                    return false;
                }
            }
        }


        return true;
    }

    /**
     * Checks if all the requirements are met for making a crafter multiblock. If so, it will create a crafter multiblock.
     *
     * @param world       The world of one of the crafter blocks
     * @param pos         The position of a crafter block to check if it might form a multiblock with other crafter blocks
     * @param placer      Who placed the crafter if the crafter was placed by someone. Can be null if irrelevant.
     * @param crafterSize The size of the multiblock
     */
    public static void attemptMultiblock(World world, BlockPos pos, @Nullable EntityLivingBase placer, int crafterSize) {
        // The master block is the last block placed before the multiblock was formed
        List<BlockPos> crafterBlocks = getPossibleMultiblock(world, pos);
        if (crafterBlocks != null) {
            // Searches for space above it
            if (spaceExists(world, crafterBlocks, crafterSize)) {

                createMultiblock(world, crafterBlocks, crafterSize);


            } else if (world.isRemote && placer instanceof EntityPlayer) {
                ((EntityPlayer) (placer)).sendStatusMessage(new TextComponentTranslation("tile.spatialcrafting.blockcrafter.no_space", 0), true);

            }
        }


    }

    private static List<BlockPos> getPossibleMultiblock(World world, BlockPos originalPos) {
        List<BlockPos> nearbyCrafters = new LinkedList<>();

        int crafterSize = ((BlockCrafter) (world.getBlockState(originalPos).getBlock())).size();
        BlockPos currentPos;

        // Put all nearby crafters in a list
        for (int i = -(crafterSize - 1); i <= crafterSize - 1; i++) {
            for (int j = -(crafterSize - 1); j <= crafterSize - 1; j++) {
                currentPos = originalPos.add(i, 0, j);

                // Adds if the other block is a crafter and of the same size.
                if (world.getBlockState(currentPos).getBlock().equals(world.getBlockState(originalPos).getBlock())) {
                    nearbyCrafters.add(currentPos);
                }

            }
        }

        List<BlockPos> list;

        // Try to form a multiblock from the existing crafters
        for (BlockPos nearbyCrafter1 : nearbyCrafters) {
            list = new LinkedList<>();
            list.add(nearbyCrafter1);
            for (BlockPos nearbyCrafter2 : nearbyCrafters) {

                if (nearbyCrafter2.equals(nearbyCrafter1)) {
                    continue;
                }

                if (validateMultiblockList(list, nearbyCrafter2, crafterSize)) {
                    list.add(nearbyCrafter2);
                }
                if (list.size() == crafterSize * crafterSize) {
                    return list;
                }
            }
        }

        return null;

    }

    private static boolean validateMultiblockList(List<BlockPos> list, BlockPos newValue, int crafterSize) {
        for (BlockPos oldValue : list) {
            if (MathUtil.minimalDistanceOf(oldValue, newValue) > crafterSize - 1) {
                return false;
            }
        }
        return true;
    }


}
