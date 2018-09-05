package fudge.spatialcrafting.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

public abstract class JeiButton {
    private final int width;
    private final int height;
    private final ResourceLocation textureLocation;
    private final ResourceLocation textureLocationOff;
    private int xOffset;
    private int yOffset;

    public JeiButton(int xOffset, int yOffset, int width, int height, ResourceLocation textureLocation) {
        this(xOffset, yOffset, width, height, textureLocation, textureLocation);
    }

    public JeiButton(int xOffset, int yOffset, int width, int height, ResourceLocation onTextureLocation, ResourceLocation offTextureLocation) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
        this.textureLocation = onTextureLocation;
        this.textureLocationOff = offTextureLocation;
    }

    public ResourceLocation getTexture() {
        return textureLocation;
    }


    public int getxOffset() {
        return xOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void handleClick(Minecraft minecraft, int cursorX, int buttonY) {
        if (isOn(minecraft) && cursorOnButton(cursorX, buttonY)) {
            onButtonClick(minecraft);
            minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    public boolean cursorOnButton(int cursorX, int cursorY) {
        return cursorX > xOffset && cursorY > yOffset && cursorX < xOffset + width && cursorY < yOffset + height;
    }

    abstract boolean isOn(Minecraft minecraft);

    abstract void onButtonClick(Minecraft minecraft);

    public void drawExtra(Minecraft minecraft, int cursorX, int cursorY) {}


    public void draw(Minecraft minecraft, int cursorX, int cursorY) {
        if (isOn(minecraft)) {
            if (cursorOnButton(cursorX, cursorY)) {
                hoverColor();
            } else {
                clearColor();
            }
            // Draw on texture
            minecraft.getTextureManager().bindTexture(getTexture());
            Gui.drawModalRectWithCustomSizedTexture(xOffset, yOffset, 0, 0, width, height, width, height);
        } else {
            clearColor();
            // Draw off texture
            minecraft.getTextureManager().bindTexture(textureLocationOff);
            Gui.drawModalRectWithCustomSizedTexture(xOffset, yOffset, 0, 0, width, height, width, height);
        }

        drawExtra(minecraft, cursorX, cursorY);

    }

    private void hoverColor() {
        GlStateManager.color(0.7f, 0.7f, 1.0f, 1.0f);
    }

    private void clearColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }


}
