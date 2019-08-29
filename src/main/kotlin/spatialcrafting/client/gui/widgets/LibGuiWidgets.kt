package spatialcrafting.client.gui.widgets

import io.github.cottonmc.cotton.gui.widget.WSprite
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import net.minecraft.util.Identifier
import spatialcrafting.ModId
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.LibGuiWidget

fun DevWidget.Image(texture : Identifier, width : Int, height : Int) : DevWidget
        = add(LibGuiWidget(WSprite(texture),width,height))
fun DevWidget.Image(path : String, width : Int, height : Int) : DevWidget
        = Image(Identifier("$ModId:textures/$path"),width,height)

fun DevWidget.Switch(enabled : Boolean) : DevWidget
        = add(LibGuiWidget(WToggleButton().apply { toggle = enabled }))
