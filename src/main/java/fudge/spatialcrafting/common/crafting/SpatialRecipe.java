package fudge.spatialcrafting.common.crafting;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.tile.util.Arr3D;
import fudge.spatialcrafting.common.tile.util.CraftingInventory;
import fudge.spatialcrafting.common.tile.util.CubeArr;
import fudge.spatialcrafting.common.tile.util.RecipeInput;
import fudge.spatialcrafting.compat.crafttweaker.CraftTweakerIntegration;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static fudge.spatialcrafting.SpatialCrafting.MODID;
import static fudge.spatialcrafting.common.command.CommandAddSRecipe.RECIPES_FILE_NAME;

public class SpatialRecipe {

    //TODO: add unshaped crafting
    private static final String EXAMPLE_SCRIPT_NAME = "SpatialRecipeExamples.zs";
    private static final String CT_SCRIPTS_FOLDER_NAME = "scripts";
    private static List<SpatialRecipe> recipeList = new ArrayList<>();
    private final RecipeInput requiredInput;
    private final ItemStack output;

    public SpatialRecipe(RecipeInput recipeInput, ItemStack recipeOutput) {
        this.requiredInput = recipeInput;
        this.output = recipeOutput;
    }

    public String toFormattedString(){
        return getRequiredInput().toFormattedString() + ",\t" + CraftTweakerIntegration.itemStackToCTString(output);
    }

    @Nullable
    public static SpatialRecipe getRecipeFromItemStacks(CraftingInventory itemStackInput, ItemStack output, RecipeAddition recipeAddition) {
        // Convert ItemStack array to IIngredient array
        RecipeInput ingredientInput = new RecipeInput(itemStackInput.getCubeSize(),
                (i, j, k) -> {
                    ItemStack requiredInputToAddIS = itemStackInput.get(i, j, k);
                    if (requiredInputToAddIS.isEmpty()) return null;
                    IIngredient requiredInputToAdd = null;

                    switch (recipeAddition) {
                        case OREDICT:
                            List<IOreDictEntry> matchingOreDicts = CraftTweakerMC.getIItemStack(requiredInputToAddIS).getOres();
                            // Too many oredicts, can't process this addition.
                            if (matchingOreDicts.size() > 1) {
                                return null;
                            }
                            if (matchingOreDicts.size() == 1) {
                                requiredInputToAdd = matchingOreDicts.get(0);
                                break;
                            }
                            // If there are no matching oredicts then it will treat it as "EXACT" meaning only a the very specific item will be accepted.
                        case EXACT:
                            requiredInputToAdd = CraftTweakerMC.getIItemStack(requiredInputToAddIS);
                            break;
                        // Wildcard = we accept any metadata
                        case WILDCARD:
                            int count = requiredInputToAddIS.getCount();
                            int meta;
                            if (requiredInputToAddIS.getHasSubtypes()) {
                                meta = OreDictionary.WILDCARD_VALUE;
                            } else {
                                meta = 0;
                            }
                            requiredInputToAdd = CraftTweakerMC.getIItemStack(new ItemStack(requiredInputToAddIS.getItem(), count, meta));
                            break;

                    }

                    return requiredInputToAdd;
                });

        return new SpatialRecipe(ingredientInput, output);

    }

    public static List<SpatialRecipe> getRecipesForSize(int size) {
        List<SpatialRecipe> recipes = new LinkedList<>();
        for (SpatialRecipe recipe : getRecipes()) {
            //TODO try having the bigger ones apply for the smaller ones too
            if (recipe.requiredInput.getCubeSize() == size) {
                recipes.add(recipe);
            }
        }

        return recipes;
    }

    public static List<SpatialRecipe> getRecipes() {
        return recipeList;
    }

    public static void addRecipe(SpatialRecipe recipe) {
        recipeList.add(recipe);
    }


    public static boolean noRecipeConflict(SpatialRecipe newRecipe, @Nullable ICommandSender sender) {

        for (SpatialRecipe existingRecipe : getRecipes()) {

            // If the input is the same it means there is some kind of conflict.
            // "newIng.contains(oldIng) || oldIng.contains(newIng)" insures the recipes intersect
            if(newRecipe.requiredInput.equalsDifSize(existingRecipe.requiredInput,
                    (newIng, oldIng) -> newIng.contains(oldIng) || oldIng.contains(newIng))){

                // Same input same output
                if (itemStackEquals(newRecipe.output, existingRecipe.output)) {
                    error("recipes.spatialcrafting.add_recipe.recipe_exists", sender);
                    return false;
                } else {
                    // Same input different output
                    error("recipes.spatialcrafting.add_recipe.output_conflict", sender);
                    return false;
                }

            }
        }


        return true;
    }

    // Handles the different error method for when you use a command or you do it via @ZenMethod
    private static void error(String errorTranslationKey, @Nullable ICommandSender sender) {
        TextComponentTranslation text = new TextComponentTranslation(errorTranslationKey);
        // Command
        if (sender != null) {
            sender.sendMessage(text);
        } else {
            // @ZenMethod
            CraftTweakerAPI.logError("Can't add Spatial Recipe: " + text.toString());
        }
    }

    //TODO fix the fact that you can't put potions etc in
    private static boolean itemStackEquals(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem().equals(stack2.getItem()) && stack1.getCount() == stack2.getCount();
    }

    /**
     * Copies the recipes in the compiled code to the run directory so they may be come with the mod jar but be used normally by pack makers.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void preInit() {


        // Create scripts folder if it doesn't exist.
        File scriptsDir = new File(CT_SCRIPTS_FOLDER_NAME);
        if (!scriptsDir.isDirectory()) {
            scriptsDir.mkdir();
        }

        final String SCRIPTS_PATH_SOURCE = "/assets/" + MODID + "/scripts/" + EXAMPLE_SCRIPT_NAME;

        File spatialCraftingDir = new File(CT_SCRIPTS_FOLDER_NAME + File.separator + MODID);
        if (!spatialCraftingDir.exists()) {
            spatialCraftingDir.mkdir();
        }

        final String SCRIPTS_PATH_DESTINATION = CT_SCRIPTS_FOLDER_NAME + File.separator + MODID + File.separator + EXAMPLE_SCRIPT_NAME;
        InputStream sourceUrl = SpatialRecipe.class.getResourceAsStream(SCRIPTS_PATH_SOURCE);


        File destFile = new File(SCRIPTS_PATH_DESTINATION);


        copyScriptFile(sourceUrl, destFile);


    }

    private static void copyScriptFile(InputStream sourceUrl, File destFile) {
        try {
            if (!destFile.exists() && !otherScriptExists(new File(CT_SCRIPTS_FOLDER_NAME + "/" + MODID))) {
                FileUtils.copyInputStreamToFile(sourceUrl, destFile);
            }
        } catch (IOException e) {
            SpatialCrafting.LOGGER.error(e);
        }
    }

    private static boolean otherScriptExists(File folder) {
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            if (FilenameUtils.getExtension(file.toString()).equals("zs") && !FilenameUtils.getName(file.toString()).equals(RECIPES_FILE_NAME) && !FilenameUtils.getName(
                    file.toString()).equals(EXAMPLE_SCRIPT_NAME)) {
                return true;
            }
        }

        return false;

    }

    public static SpatialRecipe fromID(int ID) {
        return getRecipes().get(ID);
    }

    public RecipeInput getRequiredInput() {
        return requiredInput;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public boolean equals(Object otherObj) {

        if (!(otherObj instanceof SpatialRecipe)) {
            return false;
        }

        SpatialRecipe other = (SpatialRecipe) otherObj;

        return this.requiredInput.equalsDifSize(other.requiredInput) && itemStackEquals(this.output, other.output);

    }

    public boolean sameIDAs(SpatialRecipe other) {
        return other.getID() == this.getID();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hash(output);
        result = prime * result + requiredInput.hashCode();
        return result;
    }

    public boolean matches(CraftingInventory craftingInventory) {
        return requiredInput.equalsDifSize(craftingInventory.toIItemStackArr(), IIngredient::matches);

    }

    // Magic function to create code


    public int getID() {
        return getRecipes().indexOf(this);
    }

    public int size() {
        return getRequiredInput().getCubeSize();
    }

    public String toString() {
        return String.format("SpatialRecipe output = %s size = %d", getOutput(), this.size());
    }


}
