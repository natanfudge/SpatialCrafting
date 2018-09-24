package fudge.spatialcrafting.common.crafting;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.tile.util.CraftingInventory;
import fudge.spatialcrafting.compat.crafttweaker.CraftTweakerIntegration;
import io.netty.buffer.ByteBuf;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static fudge.spatialcrafting.SpatialCrafting.MODID;
import static fudge.spatialcrafting.common.command.CommandAddSRecipe.RECIPES_FILE_NAME;
import static fudge.spatialcrafting.common.util.MCConstants.TICKS_PER_SECOND;

public class SpatialRecipe {

    private static final String EXAMPLE_SCRIPT_NAME = "SpatialRecipeExamples.zs";
    private static final String CT_SCRIPTS_FOLDER_NAME = "scripts";
    private static final List<SpatialRecipe> recipeList = new ArrayList<>();
    private final IRecipeInput requiredInput;
    private final ItemStack output;
    //in TICKS
    private int craftTime;

    public SpatialRecipe(IRecipeInput recipeInput, ItemStack recipeOutput, int craftTime) {
        this.requiredInput = recipeInput;
        this.output = recipeOutput;
        this.craftTime = craftTime;
    }

    public static SpatialRecipe getRecipeFromItemStacks(CraftingInventory itemStackInput, ItemStack output, RecipeAddition recipeAddition, int craftTime, boolean shaped) throws UnsupportedOperationException {
        // Convert ItemStack array to IIngredient array

        IRecipeInput ingredientInput;

        if (shaped) {
            ingredientInput = new ShapedRecipeInput(itemStackInput.getCubeSize(), (i, j, k) -> {
                ItemStack stack = itemStackInput.get(i, j, k);
                if (stack.isEmpty()) return null;

                return getIngredient(recipeAddition, stack);

            });
        } else {
            ArrayList<IIngredient> ingredients = new ArrayList<>();
            for (ItemStack stack : itemStackInput) {
                if (!stack.isEmpty()) {
                    ingredients.add(getIngredient(recipeAddition, stack));
                }
            }

            ingredientInput = new ShapelessRecipeInput(ingredients);
        }

        return new SpatialRecipe(ingredientInput, output, craftTime);

    }

    @Nullable
    public static SpatialRecipe getMatchingRecipe(CraftingInventory inventory) {
        for (SpatialRecipe recipe : getRecipes()) {
            if (recipe.matches(inventory)) {
                return recipe;
            }
        }

        return null;
    }

    @Nullable
    private static IIngredient getIngredient(RecipeAddition recipeAddition, ItemStack requiredInputToAddIS) throws UnsupportedOperationException {
        switch (recipeAddition) {
            case OREDICT:
                List<IOreDictEntry> matchingOreDicts = CraftTweakerMC.getIItemStack(requiredInputToAddIS).getOres();
                // Too many oredicts, can't process this addition.
                if (matchingOreDicts.size() > 1) {
                    throw new UnsupportedOperationException("Cannot process this recipe addition!");
                }
                if (matchingOreDicts.size() == 1) {
                    return matchingOreDicts.get(0);
                }
                // If there are no matching oredicts then it will treat it as "EXACT" meaning only a the very specific item will be accepted.
            case EXACT:
                return CraftTweakerMC.getIItemStack(requiredInputToAddIS);
            // Wildcard = we accept any metadata
            case WILDCARD:
                int count = requiredInputToAddIS.getCount();
                int meta;
                if (requiredInputToAddIS.getHasSubtypes()) {
                    meta = OreDictionary.WILDCARD_VALUE;
                } else {
                    meta = 0;
                }

                // Fix up the itemStack
                ItemStack properMetaStack = new ItemStack(requiredInputToAddIS.getItem(), count, meta);
                properMetaStack.setTagCompound(requiredInputToAddIS.getTagCompound());


                return CraftTweakerMC.getIItemStack(properMetaStack);
        }

        throw new RuntimeException("This should never happen!");
    }

    public static List<SpatialRecipe> getRecipesForSize(int size) {
        List<SpatialRecipe> recipes = new LinkedList<>();
        for (SpatialRecipe recipe : getRecipes()) {
            if (recipe.requiredInput.layerSize() == size) {
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
            if (newRecipe.requiredInput.intersectsWith(existingRecipe.requiredInput)) {

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

    private static boolean itemStackEquals(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem().equals(stack2.getItem()) && stack1.getCount() == stack2.getCount();
    }

    public static SpatialRecipe fromBytes(ByteBuf buf) {
        IRecipeInput input = IRecipeInput.fromNBT(Objects.requireNonNull(ByteBufUtils.readTag(buf)));
        ItemStack output = ByteBufUtils.readItemStack(buf);
        int duration = buf.readInt();
        return new SpatialRecipe(input, output, duration);
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

    //TODO Make it so holograms can store more than 1

    public int getCraftTime() {
        return craftTime;
    }

    public void setCraftTime(int craftTime) {
        this.craftTime = craftTime;
    }

    public String toFormattedString(boolean customTime) {
        String returning = getRequiredInput().toFormattedString() + ",\t" + CraftTweakerIntegration.itemStackToCTString(output);
        if (customTime) returning += ",\t" + getCraftTime() / TICKS_PER_SECOND;
        return returning;
    }

    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, requiredInput.writeToNBT(new NBTTagCompound()));
        ByteBufUtils.writeItemStack(buf, getOutput());
        buf.writeInt(craftTime);
    }

    public IRecipeInput getRequiredInput() {
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

        return this.requiredInput.isEquivalentTo(other.requiredInput) && itemStackEquals(this.output, other.output);

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
        return requiredInput.matches(craftingInventory);
    }

    // Magic function to create code


    public int getID() {
        return getRecipes().indexOf(this);
    }

    public int size() {
        return getRequiredInput().layerSize();
    }

    public String toString() {
        return String.format("SpatialRecipe output = %s size = %d", getOutput(), this.size());
    }


}
