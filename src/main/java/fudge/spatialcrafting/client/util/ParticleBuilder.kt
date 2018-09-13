package fudge.spatialcrafting.client.util

import fudge.spatialcrafting.client.particle.ParticleCraft
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ParticleBuilder(val world : World, val endPos : Vec3d, val startTimeDelay : Int, val craftDuration : Int,
                           val craftYEndPos : Double, val stack : ItemStack){
    fun shoot(startPos : Vec3d){
        shootCraftParticle(world,startPos,endPos,startTimeDelay,craftDuration,craftYEndPos, stack)
    }
}

@SideOnly(Side.CLIENT)
private fun shootCraftParticle(world: World, startPos: Vec3d, endPos: Vec3d, startTimeDelay: Int, craftDuration: Int, craftYEndPos: Double, stack: ItemStack) {
    val texture = Minecraft.getMinecraft().renderItem.getItemModelWithOverrides(stack, world, null).particleTexture
    Minecraft.getMinecraft().effectRenderer.addEffect(ParticleCraft(world, startPos, endPos, startTimeDelay, craftDuration, craftYEndPos, texture))
}
