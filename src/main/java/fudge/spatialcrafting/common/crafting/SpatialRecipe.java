package fudge.spatialcrafting.common.crafting;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.util.Util;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static fudge.spatialcrafting.SpatialCrafting.MODID;
import static fudge.spatialcrafting.common.command.CommandAddSRecipe.RECIPES_FILE_NAME;

public class SpatialRecipe {

    //TODO: add unshaped crafting
    private static final String EXAMPLE_SCRIPT_NAME = "SpatialRecipeExamples.zs";
    private static List<SpatialRecipe> recipeList = new ArrayList<>();
    private final IIngredient[][][] requiredInput;
    private final ItemStack output;

    public SpatialRecipe(IIngredient[][][] recipeInput, ItemStack recipeOutput) {
        this.requiredInput = recipeInput;
        this.output = recipeOutput;
    }


    @Nullable
    public static SpatialRecipe getRecipeFromItemStacks(ItemStack[][][] itemStackInput, ItemStack output, RecipeAddition recipeAddition) {
        // Convert ItemStack array to IIngredient array
        IIngredient[][][] ingredientInput = new IIngredient[itemStackInput.length][itemStackInput[0].length][itemStackInput[0][0].length];
        for (int i = 0; i < itemStackInput.length; i++) {
            for (int j = 0; j < itemStackInput[i].length; j++) {
                for (int k = 0; k < itemStackInput[i][j].length; k++) {

                    ItemStack requiredInputToAddIS = itemStackInput[i][j][k];
                    if (requiredInputToAddIS.isEmpty()) continue;
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

                    ingredientInput[i][j][k] = requiredInputToAdd;

                }
            }
        }

        return new SpatialRecipe(ingredientInput, output);

    }

    public static List<SpatialRecipe> getRecipes() {
        return recipeList;
    }

    public static void addRecipe(SpatialRecipe recipe) {
        recipeList.add(recipe);
    }

    public static void removeRecipe(SpatialRecipe recipe) {
        recipeList.remove(recipe);
    }

    public static boolean noRecipeConflict(SpatialRecipe newRecipe, ICommandSender sender) {

        for (SpatialRecipe existingRecipe : getRecipes()) {

            // If the input is the same it means there is some kind of conflict.
            // "newIng.contains(oldIng) || oldIng.contains(newIng)" insures the recipes intersect
            if (Util.innerEqualsDifferentSizes(newRecipe.requiredInput,
                    existingRecipe.requiredInput,
                    (newIng, oldIng) -> newIng.contains(oldIng) || oldIng.contains(newIng))) {

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
    private static void error(String errorTranslationKey, ICommandSender sender) {
        TextComponentTranslation text = new TextComponentTranslation(errorTranslationKey);
        // Command
        if (sender != null) {
            sender.sendMessage(text);
        } else {
            // @ZenMethod
            CraftTweakerAPI.logError("Can't add Spatial Recipe: " + text.toString());
        }
    }

    private static boolean itemStackEquals(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem().equals(stack2.getItem()) && stack1.getCount() == stack2.getCount();
    }

    public IIngredient[][][] getRequiredInput() {
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

        return Util.innerEqualsDifferentSizes(this.requiredInput, other.requiredInput, Object::equals) && itemStackEquals(this.output,
                other.output);

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hash(output);
        result = prime * result + Arrays.hashCode(requiredInput);
        return result;
    }

    public boolean matches(ItemStack[][][] craftingInventory) {
        return Util.innerEqualsDifferentSizes(requiredInput, craftingInventory, (required, actualInput) -> {
            return required.matches(CraftTweakerMC.getIItemStack(actualInput));
        }, ItemStack.EMPTY);

    }

    // Magic function to create code
    public String toFormattedString() {
        StringBuilder outputBuilder = new StringBuilder("[");


        for (IIngredient[][] arr2D : requiredInput) {
            outputBuilder.append("\n\t[");

            for (IIngredient[] arr1D : arr2D) {
                outputBuilder.append("\n\t\t[");
                for (IIngredient ingredient : arr1D) {

                    if (ingredient != null) {
                        outputBuilder.append(ingredient.toCommandString());
                    } else {
                        outputBuilder.append("      null      ");
                    }

                    outputBuilder.append(", ");

                }

                // Delete the last ", "
                outputBuilder.delete(outputBuilder.length() - 2, outputBuilder.length());

                outputBuilder.append("],");

            }
            // Delete the last ","
            outputBuilder.deleteCharAt(outputBuilder.length() - 1);

            outputBuilder.append("\n\t],");
        }
        outputBuilder.append("\n]");

        return outputBuilder.toString();
    }

    /**
     * Copies the recipes in the compiled code to the run directory so they may be come with the mod jar but be used normally by pack makers.
     */
    public static void preInit() {

        final String CT_SCRIPTS_FOLDER_NAME = "scripts";

        // Create scripts folder if it doesn't exist.
        File scriptsDir = new File(CT_SCRIPTS_FOLDER_NAME);
        if (!scriptsDir.isDirectory()) {
            scriptsDir.mkdir();
        }

        final String SCRIPTS_PATH_SOURCE = "/assets/" + MODID + "/scripts/" + EXAMPLE_SCRIPT_NAME;

        File spatialCraftingDir = new File(CT_SCRIPTS_FOLDER_NAME + "/" + MODID);
        if (!spatialCraftingDir.exists()) {
            spatialCraftingDir.mkdir();
        }

        final String SCRIPTS_PATH_DESTINATION = CT_SCRIPTS_FOLDER_NAME + "/" + MODID + "/" + EXAMPLE_SCRIPT_NAME;
        InputStream sourceUrl = SpatialRecipe.class.getResourceAsStream(SCRIPTS_PATH_SOURCE);


        File destFile = new File(SCRIPTS_PATH_DESTINATION);


        copyScriptFile(CT_SCRIPTS_FOLDER_NAME, sourceUrl, destFile);


        //TODO write the script files into the scripts directory.
    }

    private static void copyScriptFile(String CT_SCRIPTS_FOLDER_NAME, InputStream sourceUrl, File destFile) {
        try {
            if (!destFile.exists() && !otherScriptExists(new File(CT_SCRIPTS_FOLDER_NAME + "/" + MODID))) {
                FileUtils.copyInputStreamToFile(sourceUrl, destFile);
            }
        } catch (IOException e) {
            SpatialCrafting.LOGGER.error(e);
        }
    }

    private static boolean otherScriptExists(File folder) {
        for (File file : folder.listFiles()) {
            if (FilenameUtils.getExtension(file.toString()).equals("zs") && !FilenameUtils.getName(file.toString()).equals(RECIPES_FILE_NAME) && !FilenameUtils.getName(
                    file.toString()).equals(EXAMPLE_SCRIPT_NAME)) {
                return true;
            }
        }

        return false;

    }


}
