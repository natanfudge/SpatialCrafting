//package spatialcrafting.client.gui;
//
//import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
//import io.github.cottonmc.cotton.gui.widget.WButton;
//import net.minecraft.client.gui.widget.AbstractButtonWidget;
//import net.minecraft.text.Text;
//import net.minecraft.util.Identifier;
//
//public class WIconButton extends WButton {
//    protected Identifier icon;
//    protected Text label;
//
//    public WIconButton(Text label) {
//        this.label = label;
//    }
//
//    public WIconButton(Identifier icon) {
//        this.icon = icon;
//    }
//
//    public WIconButton(Identifier icon, Text label) {
//        this.label = label;
//        this.icon = icon;
//    }
//
//    @Override
//    public void paintForeground(int x, int y, int mouseX, int mouseY) {
//        boolean hovered = (mouseX >= x && mouseY >= y && mouseX < x + getWidth() && mouseY < y + getHeight());
//        int state = 1; //1=regular. 2=hovered. 0=disabled.
//        if (!isEnabled()) state = 0;
//        else if (hovered) state = 2;
//
//        float px = 1 / 256f;
//        float buttonLeft = 0 * px;
//        float buttonTop = (46 + (state * 20)) * px;
//        int halfWidth = getWidth() / 2;
//        if (halfWidth > 198) halfWidth = 198;
//        float buttonWidth = halfWidth * px;
//        float buttonHeight = 20 * px;
//
//        float buttonEndLeft = (200 - (getWidth() / 2)) * px;
//
//        ScreenDrawing.rect(AbstractButtonWidget.WIDGETS_LOCATION, x, y, getWidth() / 2, 20, buttonLeft, buttonTop, buttonLeft + buttonWidth, buttonTop + buttonHeight, 0xFFFFFFFF);
//        ScreenDrawing.rect(AbstractButtonWidget.WIDGETS_LOCATION, x + (getWidth() / 2), y, getWidth() / 2, 20, buttonEndLeft, buttonTop, 200 * px, buttonTop + buttonHeight, 0xFFFFFFFF);
//
//        int textLeft = 0;
//        if (icon != null) {
//            textLeft += 16 + 2;
//            ScreenDrawing.rect(icon, x + 2, y + 1, 16, 16, 0xFFFFFFFF);
//        }
//
//        if (label != null) {
//            int color = 0xE0E0E0;
//            if (!isEnabled()) {
//                color = 0xA0A0A0;
//            } else if (hovered) {
//                color = 0xFFFFA0;
//            }
//            ScreenDrawing.drawString(label.asFormattedString(), x + textLeft, y + ((20 - 8) / 2), color);
//            //ScreenDrawing.drawCenteredWithShadow(label.asFormattedString(), x+(getWidth()/2), y + ((20 - 8) / 2), color); //LibGuiClient.config.darkMode ? darkmodeColor : color);
//        }
//    }
//
//    public void setIcon(Identifier icon) {
//        this.icon = icon;
//    }
//}