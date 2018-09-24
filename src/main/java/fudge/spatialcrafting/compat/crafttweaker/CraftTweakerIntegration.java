package fudge.spatialcrafting.compat.crafttweaker;


import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.CraftTweaker;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.crafting.ShapedRecipeInput;
import fudge.spatialcrafting.common.crafting.ShapelessRecipeInput;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.util.MCConstants;
import fudge.spatialcrafting.common.util.SCConstants;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;


// ZenClass means that it literally exists in zenscript
@ZenClass("mods." + SpatialCrafting.MODID)
public final class CraftTweakerIntegration {

    //TODO do some magic that will allow for transformations in shapeless crafting

    private CraftTweakerIntegration() {}

    public static void preInit() {
        CraftTweakerAPI.registerClass(CraftTweakerIntegration.class);
    }


    // ZenMethod means you can call this method in zenscript to do wtf you want
    @ZenMethod
    public static void addRecipe(IIngredient[][][] input, IItemStack output, @Optional float craftTime) {

        // This is a workaround to have crafttweaker properly compile the zs files
        // Theoretically you can do the fixing loop here and then do:
        //  SpatialRecipe.addRecipe(new SpatialRecipe(fixedInput, fixedOutput)));
        // without implementing IAction.
        // But that wouldn't be caught in zenscript's try/catches and wouldn't print to the log.
        CraftTweaker.LATE_ACTIONS.add(new AddShaped(input, output, craftTime));

    }

    @ZenMethod
    public static void addShapeless(IIngredient[] input, IItemStack output, @Optional float craftTime) {
        CraftTweaker.LATE_ACTIONS.add(new AddShapeless(input, output, craftTime));
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

    static abstract class ActionAdd implements IAction {
        protected final IItemStack output;
        protected final float craftTime;


        ActionAdd(IItemStack recipeOutput, float craftTime) {
            this.output = recipeOutput;
            this.craftTime = craftTime;
        }

        // This runs at post init together with all other crafttweaker recipes additions.
        public void apply(SpatialRecipe recipe) {
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

    // See above for why this exists
    static class AddShaped extends ActionAdd {
        private final IIngredient[][][] input;


        AddShaped(IIngredient[][][] recipeInput, IItemStack recipeOutput, float craftTime) {
            super(recipeOutput, craftTime);
            this.input = recipeInput;
        }

        @Override
        public void apply() {
            if (input.length == input[0].length && input[0].length == input[0][0].length) {

                int time = craftTime == 0 ? input.length * SCConstants.DEFAULT_CRAFT_TIME_MULTIPLIER : Math.round(craftTime * MCConstants.TICKS_PER_SECOND);
                SpatialRecipe recipe = new SpatialRecipe(ShapedRecipeInput.Companion.fromArr(input), CraftTweakerMC.getItemStack(output), time);
                super.apply(recipe);
            } else {
                CraftTweakerAPI.logError("Can't add recipe for " + output.toString() + " because the input is not a cube array");
            }
        }
    }

    // See above for why this exists
    static class AddShapeless extends ActionAdd {
        private final IIngredient[] input;


        AddShapeless(IIngredient[] recipeInput, IItemStack recipeOutput, float craftTime) {
            super(recipeOutput, craftTime);
            this.input = recipeInput;
        }

        @Override
        public void apply() {
            double sizeD = Math.cbrt(input.length);
            int size = sizeD == ((int) sizeD) ? (int) sizeD : ((int) sizeD) + 1;
            int time = craftTime == 0 ? size * SCConstants.DEFAULT_CRAFT_TIME_MULTIPLIER : Math.round(craftTime * MCConstants.TICKS_PER_SECOND);
            SpatialRecipe recipe = new SpatialRecipe(new ShapelessRecipeInput(input), CraftTweakerMC.getItemStack(output), time);
            super.apply(recipe);

        }
    }


}










