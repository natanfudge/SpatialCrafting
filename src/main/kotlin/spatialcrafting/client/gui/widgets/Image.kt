//package spatialcrafting.client.gui.widgets
//
//import io.github.cottonmc.cotton.gui.widget.WSprite
//import net.minecraft.util.Identifier
//import spatialcrafting.ModId
//import spatialcrafting.client.gui.DevWidget
//import spatialcrafting.client.gui.LibGuiWidget
//import spatialcrafting.client.gui.WidgetContext
//
//fun WidgetContext.Image(texture : Identifier, width : Int, height : Int) : DevWidget
//        = add(LibGuiWidget(WSprite(texture),width,height))
//fun WidgetContext.Image(path : String, width : Int, height : Int) : DevWidget
//        = Image(Identifier("$ModId:textures/$path"),width,height)