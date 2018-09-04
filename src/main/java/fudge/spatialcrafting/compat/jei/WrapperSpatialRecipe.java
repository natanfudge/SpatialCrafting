package fudge.spatialcrafting.compat.jei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.crafting.SpatialRecipe;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.CrafterUtil;
import fudge.spatialcrafting.common.util.Util;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.server.PacketSetActiveLayer;
import fudge.spatialcrafting.network.server.PacketStartCraftingHelp;
import fudge.spatialcrafting.network.server.PacketStopCraftingHelp;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fudge.spatialcrafting.SpatialCrafting.MODID;
import static fudge.spatialcrafting.common.util.Util.translate;

public class WrapperSpatialRecipe implements IRecipeWrapper {

    private static final int LAYERS_WIDTH = 13;
    private static final int LAYERS_HEIGHT = 11;
    private static final List<Integer> BUTTON_Y_OFFSET = ImmutableList.of(0, 0, 5, 15, 23, 33);
    private static final int DISTANCE_BETWEEN_BUTTONS = 3;
    private static final ResourceLocation LAYERS_OFF_UP_LOCATION = new ResourceLocation(MODID, "textures/gui/button/up_off.png");
    private static final ResourceLocation LAYERS_DOWN_OFF_LOCATION = new ResourceLocation(MODID, "textures/gui/button/down_off.png");
    private static final ResourceLocation LAYERS_UP_LOCATION = new ResourceLocation(MODID, "textures/gui/button/up_on.png");
    private static final ResourceLocation LAYERS_DOWN_LOCATION = new ResourceLocation(MODID, "textures/gui/button/down_on.png");
    private static final ResourceLocation HELP_LOCATION = new ResourceLocation(MODID, "textures/gui/button/plus_on.png");
    private static final ResourceLocation HELP_ACTIVE_LOCATION = new ResourceLocation(MODID, "textures/gui/button/plus_active.png");
    private static final ResourceLocation HELP_OFF_LOCATION = new ResourceLocation(MODID, "textures/gui/button/plus_off.png");

    private static final int LAYERS_X = 1;
    private final SpatialRecipe recipe;
    private final List<JeiButton> buttons;
    private int layer;
    private CategorySpatialRecipe recipeCategory;

    public WrapperSpatialRecipe(SpatialRecipe recipe, CategorySpatialRecipe recipeCategory) {
        this.recipe = recipe;
        this.recipeCategory = recipeCategory;
        layer = 0;

        buttons = addButtons();

    }

    private static List<List<ItemStack>> fromArray(IIngredient[][][] arr, int layer) {
        List<List<ItemStack>> list = new ArrayList<>();
        Util.innerForEach2D(arr[layer], ingredient -> {
            if (ingredient != null) {
                list.add(Lists.newArrayList(CraftTweakerMC.getItemStacks(ingredient.getItems())));
            } else {
                list.add(Collections.singletonList(ItemStack.EMPTY));
            }

        });
        return list;
    }

    private List<JeiButton> addButtons() {
        int upY = BUTTON_Y_OFFSET.get(recipeSize());
        JeiButton upButton = new JeiButton(LAYERS_X, upY, LAYERS_WIDTH, LAYERS_HEIGHT, LAYERS_UP_LOCATION, LAYERS_OFF_UP_LOCATION) {
            @Override
            void onButtonClick(Minecraft minecraft) {
                if (layer < recipeSize() - 1) {
                    layer++;
                    changeLayer(minecraft);
                }
            }

            @Override
            boolean isOn(Minecraft minecraft) {
                return layer < recipeSize() - 1;
            }
        };

        int downY = upY + LAYERS_HEIGHT + DISTANCE_BETWEEN_BUTTONS;
        JeiButton downButton = new JeiButton(LAYERS_X, downY, LAYERS_WIDTH, LAYERS_HEIGHT, LAYERS_DOWN_LOCATION, LAYERS_DOWN_OFF_LOCATION) {
            @Override
            void onButtonClick(Minecraft minecraft) {
                layer--;
                changeLayer(minecraft);
            }

            @Override
            boolean isOn(Minecraft minecraft) {
                return layer > 0;
            }

        };

        final int HELP_WIDTH = 13;
        final int HELP_HEIGHT = 13;
        int totalWidth = CategorySpatialRecipe.TEXTURE_SIZES.get(recipeSize()).getWidth();
        final int HELP_BUTTON_X = recipeSize() <= 3 ? totalWidth + 6 : totalWidth - HELP_HEIGHT;

        final int HELP_Y = CategorySpatialRecipe.TEXTURE_SIZES.get(recipeSize()).getHeight() - HELP_HEIGHT;

        JeiButton helpButton = new JeiButton(HELP_BUTTON_X, HELP_Y, HELP_WIDTH, HELP_HEIGHT, HELP_LOCATION, HELP_OFF_LOCATION) {
            @Override
            boolean isOn(Minecraft minecraft) {
                return craftersExistNearby(minecraft) && nearestCrafterBigEnough(minecraft);
            }

            @Override
            void onButtonClick(Minecraft minecraft) {
                World world = minecraft.world;
                TileCrafter masterCrafter = CrafterUtil.getClosestMasterBlock(world, minecraft.player.getPosition());

                if (masterCrafter != null && masterCrafter.size() >= recipeSize()) {

                    BlockPos masterPos = masterCrafter.getPos();


                    minecraft.player.closeScreen();

                    // Recipe help is active for the nearest crafter for this specific recipe
                    if (crafterMatches(masterCrafter)) {
                        assert masterCrafter.getRecipe() != null;

                        // Client
                        masterCrafter.stopHelp();
                        //Server
                        PacketHandler.getNetwork().sendToServer(new PacketStopCraftingHelp(minecraft.player.getPosition()));
                    } else {

                        // Server craft help
                        PacketHandler.getNetwork().sendToServer(new PacketStartCraftingHelp(masterPos, recipe));
                        // Client craft help
                        Util.<TileCrafter>getTileEntity(world, masterPos).startHelp(recipe);

                    }


                }

            }

            @Override
            public void drawExtra(Minecraft minecraft, int cursorX, int cursorY) {
                if (cursorOnButton(cursorX, cursorY)) {

                    List<String> textLines = new ArrayList<>(2);
                    if (nearestCrafterMatches(minecraft)) {
                        textLines.add(translate("jei.wrapper.helpButton.description_active"));
                    } else {
                        textLines.add(translate("jei.wrapper.helpButton.description"));

                    }

                    int MAX_TEST_WIDTH = 0;
                    if (!craftersExistNearby(minecraft)) {
                        textLines.add(TextFormatting.RED + translate("jei.wrapper.helpButton.error.no_crafters"));
                        MAX_TEST_WIDTH = 150;
                    } else if (!nearestCrafterBigEnough(minecraft)) {
                        textLines.add(TextFormatting.RED + translate("jei.wrapper.helpButton.error.crafter_too_small"));
                        MAX_TEST_WIDTH = 100;

                    }

                    final int HEIGHT = minecraft.displayHeight;
                    final int SCREEN_WIDTH = minecraft.displayWidth;

                    GuiUtils.drawHoveringText(textLines, cursorX, cursorY, SCREEN_WIDTH, HEIGHT, MAX_TEST_WIDTH, minecraft.fontRenderer);
                }
            }


            @Override
            public ResourceLocation getTexture(Minecraft minecraft) {
                return nearestCrafterMatches(minecraft) ? HELP_ACTIVE_LOCATION : HELP_LOCATION;
            }
        };


        return ImmutableList.of(upButton, downButton, helpButton);
    }


    private boolean nearestCrafterMatches(Minecraft minecraft) {
        TileCrafter crafter = CrafterUtil.getClosestMasterBlock(minecraft.world, minecraft.player.getPosition());
        return crafterMatches(crafter);
    }

    private boolean crafterMatches(TileCrafter crafter) {
        if (crafter == null || crafter.getRecipe() == null) return false;

        return crafter.getRecipe().sameIDAs(recipe);
    }

    private boolean nearestCrafterBigEnough(Minecraft minecraft) {
        World world = minecraft.world;
        BlockPos playerPos = minecraft.player.getPosition();

        TileCrafter crafter = CrafterUtil.getClosestMasterBlock(world, playerPos);
        return crafter != null && crafter.size() >= recipeSize();
    }

    private boolean craftersExistNearby(Minecraft minecraft) {
        World world = minecraft.world;
        BlockPos playerPos = minecraft.player.getPosition();

        TileCrafter closestCrafter = CrafterUtil.getClosestMasterBlock(world, playerPos);


        final int MAX_DISTANCE = 64;
        if (closestCrafter != null && Util.euclideanDistanceOf(closestCrafter.getPos(), playerPos) < MAX_DISTANCE) {
            return true;
        } else {
            return false;
        }
    }

    private int recipeSize() {
        return recipe.getRequiredInput().length;
    }

    @Override
    public void getIngredients(@NotNull IIngredients ingredients) {
        ingredients.setOutput(ItemStack.class, recipe.getOutput());
        ingredients.setInputLists(ItemStack.class, fromArray(recipe.getRequiredInput(), layer));
    }


    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        buttons.forEach(button -> button.handleClick(minecraft, mouseX, mouseY));

        return false;
    }

    private void changeLayer(Minecraft minecraft) {
        RefreshCategory();
        PacketHandler.getNetwork().sendToServer(new PacketSetActiveLayer(minecraft.player.getPosition(), layer));
    }

    // Hacky category refreshing
    private void RefreshCategory() {
        try {
            Class ingredients = Class.forName("mezz.jei.ingredients.Ingredients");
            IIngredients iingredients = (IIngredients) ingredients.newInstance();
            getIngredients(iingredients);
            recipeCategory.setRecipe(null, this, iingredients);


        } catch (ClassNotFoundException e) {
            SpatialCrafting.LOGGER.error("The JEI ingredient class has not been found. This is probably because the name changed!");
        } catch (IllegalAccessException | InstantiationException e) {
            SpatialCrafting.LOGGER.error("Error while trying to create a JEI ingredient class");
        }


    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        buttons.forEach(button -> button.draw(minecraft, mouseX, mouseY));

        final int STRING_X_OFFSET = LAYERS_X + 3;
        final int STRING_Y_OFFSET = BUTTON_Y_OFFSET.get(recipeSize()) - 9;

        // Draw layer number string
        String label = Integer.toString(layer + 1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 150);
        final int COLOR = 0x00_00_99_FF;
        minecraft.fontRenderer.drawString(label, STRING_X_OFFSET, STRING_Y_OFFSET, COLOR, true);
        GlStateManager.popMatrix();


    }


}
