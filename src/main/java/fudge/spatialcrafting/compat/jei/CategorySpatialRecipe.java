package fudge.spatialcrafting.compat.jei;

import com.google.common.collect.ImmutableList;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.util.Rectangle;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CategorySpatialRecipe implements IRecipeCategory<WrapperSpatialRecipe> {

    public static final List<Rectangle<Integer, Integer>> TEXTURE_SIZES = ImmutableList.of(new Rectangle<>(0, 0),
            new Rectangle<>(0, 0),
            new Rectangle<>(116, 36),
            new Rectangle<>(134, 54),
            new Rectangle<>(158, 72),
            new Rectangle<>(176, 90));
    private static final String GUI_TEXTURE_LOCATION = SpatialCrafting.MODID + ":textures/gui/crafter/x";
    private final String UID;
    private final IDrawable background;

    private int recipeSize;
    private IRecipeLayout cachedRecipeLayout;

    public CategorySpatialRecipe(IGuiHelper guiHelper, int size) {
        recipeSize = size;
        UID = SpatialCrafting.MODID + recipeSize;

        ResourceLocation location = new ResourceLocation(GUI_TEXTURE_LOCATION + size + ".png");

        background = guiHelper.drawableBuilder(location, 0, 0, TEXTURE_SIZES.get(size).getWidth(), TEXTURE_SIZES.get(size).getHeight()).build();
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle()  {
        return new TextComponentTranslation("jei.category.spatial_crafting").getUnformattedComponentText();
    }

    @Override
    public String getModName() {
        return SpatialCrafting.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(@Nullable IRecipeLayout recipeLayout, @Nonnull WrapperSpatialRecipe recipeWrapper, @Nonnull IIngredients ingredients) {

        if (recipeLayout != null) {
            cachedRecipeLayout = recipeLayout;
        }


        IGuiItemStackGroup guiItemStacks = cachedRecipeLayout.getItemStacks();

        // Display the inputs
        final int DISTANCE_BETWEEN_ITEMS = 18;

        for (int i = 0; i < recipeSize; ++i) {
            for (int j = 0; j < recipeSize; ++j) {
                int index = j + (i * recipeSize);

                final int ITEMS_X_OFFSET = 18;
                guiItemStacks.init(index, true, j * DISTANCE_BETWEEN_ITEMS + ITEMS_X_OFFSET, i * DISTANCE_BETWEEN_ITEMS);
            }
        }

        final List<Integer> X_OFFSETS = ImmutableList.of(0, 0, 94, 112, 136, 154);
        final int yPosition = 18 * (recipeSize - 1) / 2;
        //Display the output
        guiItemStacks.init(recipeSize * recipeSize + 1, false, X_OFFSETS.get(recipeSize), yPosition);


        guiItemStacks.set(ingredients);


    }

}
