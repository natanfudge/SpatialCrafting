package fudge.spatialcrafting.compat.crafttweaker;


import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.CraftTweaker;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.tile.util.RecipeInput;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;


// ZenClass means that it literally exists in zenscript
@ZenClass("mods." + SpatialCrafting.MODID)
public final class CraftTweakerIntegration {

    private CraftTweakerIntegration() {}

    public static void init() {
        CraftTweakerAPI.registerClass(CraftTweakerIntegration.class);
    }


    // ZenMethod means you can call this method in zenscript to do wtf you want
    @ZenMethod
    public static void addRecipe(IIngredient[][][] input, IItemStack output) {

        // This is a workaround to have crafttweaker properly compile the zs files
        // Theoretically you can do the fixing loop here and then do:
        //  SpatialRecipe.addRecipe(new SpatialRecipe(fixedInput, fixedOutput)));
        // without implementing IAction.
        // But that wouldn't be caught in zenscript's try/catches and wouldn't print to the log.
        CraftTweaker.LATE_ACTIONS.add(new ActionAdd(input, output));


    }

    public static String itemStackToCTString(ItemStack itemStack) {
        String output = "<" + itemStack.getItem().getRegistryName();

        // Need to add metadeta for things such as different types of wool. Should be removed in 1.13.
        if (itemStack.getMetadata() != 0) {
            output += (":" + itemStack.getMetadata());
        }

        output += ">";

        if (itemStack.getCount() > 1) {
            output += (" * " + itemStack.getCount());
        }

        return output;
    }

    // See above for why this exists
    private static class ActionAdd implements IAction {
        private final RecipeInput input;
        private final IItemStack output;


        ActionAdd(IIngredient[][][] recipeInput, IItemStack recipeOutput) {
            this.input = RecipeInput.Companion.fromArr(recipeInput);
            this.output = recipeOutput;
        }

        @Override
        // This runs at post init together with all other crafttweaker recipes additions.
        public void apply() {

            //TODO IngredientConditions
            //TODO transfomers
            //TODO make it so you can control the crafting time


            SpatialRecipe recipe = new SpatialRecipe(input, CraftTweakerMC.getItemStack(output));
            // Null to indicate to print errors to the console
            if (SpatialRecipe.noRecipeConflict(recipe, null)) {
                SpatialRecipe.addRecipe(recipe);
            }
        }

        @Override
        public String describe() {
            return "Adding Spatial Crafting Recipe for " + CraftTweakerMC.getItemStack(output).getDisplayName();
        }
    }

}










