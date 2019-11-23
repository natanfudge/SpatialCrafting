package spatialcrafting.mixin;

import net.minecraft.block.Block;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

//TODO: export this when separating kotlin extensions from spatialcrafting
@Mixin(RecipeManager.class)
public interface RecipeManagerMixin {
    @Accessor("recipes")
    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();
}
