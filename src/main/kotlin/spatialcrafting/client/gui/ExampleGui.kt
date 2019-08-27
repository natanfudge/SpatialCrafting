package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.WLabel.DEFAULT_TEXT_COLOR
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import spatialcrafting.ModId
import spatialcrafting.Packets
import spatialcrafting.compat.rei.getNearestCrafter
import spatialcrafting.sendPacketToServer
import spatialcrafting.util.*
import java.lang.Integer.max
import java.lang.Integer.min

/**
 * Relative size from 0 to 256
 */
inline class Dp(val value: Double)

val Double.dp get() = Dp(this)
val Int.dp get() = Dp(this.d)

data class WidgetPositioning(val widget: WWidget, val x: Dp, val y: Dp)

inline class PanelPositioning(val elements: List<Dp>)


//class Root(private val panel: WPlainPanel) {
////    var x: Int = 0
////    var y: Int = 0
//
//    val height: Int get() = panel.height
//    val width: Int get() = panel.width
//
//    fun add(widget: WWidget, x: Int, y: Int) = panel.add(widget, x, y)
//
//}

class Constraints(val x: Int, val y: Int, val width: Int, val height: Int)


abstract class WidgetContext : WWidget() {
    //    fun add(widget: WidgetContext)
    abstract fun add(widget: WWidget)

    abstract fun positionChildren(parent: WPlainPanel, x: Int, y: Int, width: Int, height: Int)
    abstract val minimumHeight: Int
    abstract val minimumWidth: Int
    val expandHeight: Boolean get() = false
    val expandWidth: Boolean get() = false


//    override fun getHeight(): Int {
//        return
//    }
}

enum class Alignment {
    Top,
    Center
}


private fun WWidget.setWidth(width: Int) = setSize(width, height)
private fun WWidget.setHeight(height: Int) = setSize(width, height)

interface IWidget {
    val minimumHeight: Int
    val minimumWidth: Int
    val expandHeight: Boolean
    val expandWidth: Boolean
//    val maximumHeight: Int
//    val maximumWidth: Int

    fun draw(x: Int, y: Int, width: Int, height: Int)
}

abstract class StatelessWidget : IWidget {
    override val minimumHeight = compose.minimumHeight
    override val minimumWidth = compose.minimumWidth
    override val expandHeight = compose.expandHeight
    override val expandWidth = compose.expandWidth

    override fun draw(x: Int, y: Int, width: Int, height: Int) = compose.draw(x, y, width, height)

    abstract val compose: IWidget

}


class ColumnClass(private val children: List<IWidget>) : IWidget {
    //     = mutableListOf<IWidget>()
    override val minimumHeight: Int
        get() = children.sumBy { it.minimumHeight }
    override val minimumWidth: Int
        get() = children.maxValueBy { it.minimumWidth } ?: 0
    override val expandHeight = true
    override val expandWidth = false
//    override val maximumHeight: Int
//        get() = Int.MAX_VALUE
//    override val maximumWidth: Int
//        get() = children.maxValueBy { it.maximumWidth } ?: 0

    override fun draw(x: Int, y: Int, width: Int, height: Int) {
        drawChildren(x = x,startingPoint = y,width = width, height = height)
    }

    private fun drawChildren(x: Int, startingPoint: Int, width: Int, height: Int) {
        val space = height - children.sumBy { it.minimumHeight }
        val expandingWidgets = children.count { it.expandHeight }
        val extraSpaceForExpandingWidgets = when{
            space <= 0 -> 0
            expandingWidgets == 0 -> 0
            else -> space / expandingWidgets
        }
        var currentY = startingPoint
        for (widget in children) {
            widget.draw(x, currentY, width, height)
            currentY += widget.minimumHeight
            if (widget.expandHeight) {
                 currentY += extraSpaceForExpandingWidgets
            }

b
//            if (widget is WidgetContext) {
//                //TODO: split the space between them
//                val spaceTaken = if (widget.expandHeight) space else max(min(minimumHeight, spaceForEach), 0)
//                widget.positionChildren(parent, x, currentY, width, spaceTaken)
//                currentY += spaceTaken
//            }
//            else {
//                parent.add(widget, x, currentY, widget.width, widget.height)
//                currentY += widget.height
//            }

        }
    }

}

//class WidgetWrapper(val widget: WWidget){
//
//}

class ColumnWidgetCreator(
        private val alignment: Alignment = Alignment.Top
) : WidgetContext, WWidget() {
    private val elements = mutableListOf<WWidget>()
    //    private val resizeableElements = mutableMapOf<Int, WidgetContext>()
    override fun add(widget: WWidget) {
        elements.add(widget)
    }

    override val minimumHeight get() = elements.sumBy { it.height }
    override val minimumWidth get() = elements.maxBy { it.width }?.width ?: 0
    override val expandHeight: Boolean = alignment == Alignment.Center
    override val expandWidth: Boolean = false


    override fun positionChildren(parent: WPlainPanel, x: Int, y: Int, width: Int, height: Int) {
        when (alignment) {
            Alignment.Top -> {
                placeChildren(parent, x, y, width, height)
            }
            Alignment.Center -> {
                val startingPoint = (y + height) / 2 - elements.sumBy { it.height } / 2
                placeChildren(parent, x, startingPoint, width, height)
            }
        }
    }

    private fun placeChildren(parent: WPlainPanel, x: Int, startingPoint: Int, width: Int, height: Int) {
        val space = height - elements.sumBy { if (it is WidgetContext) 0 else it.height }
        val spaceForEach = elements.sumBy { if (it is WidgetContext) 1 else 0 }.let { if (it == 0) 0 else space / it }
        var currentY = startingPoint
        for (widget in elements) {
            if (widget is WidgetContext) {
                //TODO: split the space between them
                val spaceTaken = if (widget.expandHeight) space else max(min(minimumHeight, spaceForEach), 0)
                widget.positionChildren(parent, x, currentY, width, spaceTaken)
                currentY += spaceTaken
            }
            else {
                parent.add(widget, x, currentY, widget.width, widget.height)
                currentY += widget.height
            }

        }
    }


    private fun WPlainPanel.addWidgets(startingPoint: Int) {
        var y = startingPoint
        for (widget in elements) {
            add(widget, 0, y, widget.width, widget.height)
            y += widget.height
        }
    }
}

inline fun WidgetContext.Column(alignment: Alignment = Alignment.Top,
                                init: WidgetContext.() -> Unit) =
        add(ColumnWidgetCreator(alignment).apply(init))

class RowWidgetCreator : WidgetContext, WWidget() {
    private val elements = mutableListOf<WWidget>()
    private val resizeableElements = mutableMapOf<Int, WidgetContext>()
    override fun add(widget: WWidget) {
        elements.add(widget)
    }

    override val minimumHeight get() = elements.maxBy { it.height }?.height ?: 0
    override val minimumWidth get() = elements.sumBy { it.width }
    override val expandHeight: Boolean = false
    override val expandWidth: Boolean = false


    override fun positionChildren(parent: WPlainPanel, x: Int, y: Int, width: Int, height: Int) {
//        when (alignment) {
//            Alignment.Top -> {
        val space = width - elements.sumBy { if (it is WidgetContext) 0 else it.width }
        val spaceForEach = space / elements.sumBy { if (it is WidgetContext) 1 else 0 }
        var currentX = x
        for (widget in elements) {
            if (widget is WidgetContext) {
                //TODO: split the space between them
                val spaceTaken = max(min(minimumWidth, spaceForEach), 0)
                widget.positionChildren(parent, currentX, y, width, spaceTaken)
                currentX += spaceTaken
            }
            else {
                parent.add(widget, currentX, y, widget.width, widget.height)
                currentX += widget.width
            }

        }
//
//            }
//            Alignment.Center -> {
////                parent.addWidgets(startingPoint = spatialcrafting.compat.rei.height / 2 - elements.sumBy { it.height } / 2)
////
////                var startingPoint = (x + height) / 2 - elements.sumBy { it.height } / 2
////
////                for (widget in elements) {
////                    parent.add(widget, x, startingPoint, widget.width, widget.height)
////                    startingPoint += widget.height
////                }
//            }
//        }
    }
}

inline fun WidgetContext.Row(init: WidgetContext.() -> Unit) = add(RowWidgetCreator().apply(init))

fun WWidget.isHighlighted(x: Int, y: Int, mouseX: Int, mouseY: Int) = mouseX >= x
        && mouseY >= y
        && mouseX < x + width
        && mouseY < y + height

@Suppress("LeakingThis")
open class ImageClass(path: String,
                      width: Int,
                      height: Int,
                      private val onClick: ((x: Int, y: Int, button: Int) -> Unit)? = null) : WSprite(Identifier("$ModId:textures/$path")) {
    override fun onClick(x: Int, y: Int, button: Int) {
        playButtonClickSoundClient()
        onClick?.invoke(x, y, button)
    }

    override fun paintForeground(x: Int, y: Int, mouseX: Int, mouseY: Int) {
        if (onClick != null && isHighlighted(x, y, mouseX, mouseY)) {

//            ScreenDrawing.rect(x, y, getWidth(), getHeight(), 0x40_00_00_FF)
//            ScreenDrawing.rect(x, y, getWidth(), getHeight(), 0x40_00_00_FF)
        }
        super.paintForeground(x, y, mouseX, mouseY)
    }


    init {
        setSize(width, height)
    }
}

class GuiException(message: String) : Exception(message)

class ButtonClass(private val onClick: ((x: Int, y: Int, button: Int) -> Unit)) : WWidget(), WidgetContext {
    private var child: WWidget? = null
    override fun add(widget: WWidget) {
        if (child != null) throw GuiException("Button cannot contain more than one child")
        child = widget
    }

    override fun positionChildren(parent: WPlainPanel, x: Int, y: Int, width: Int, height: Int) {
        child?.let {
            if (it is WidgetContext) it.positionChildren(parent, x, y, width, height)
            else parent.add(it, x, y, width, height)
        }
    }

    override val minimumHeight: Int get() = child?.height ?: 0
    override val minimumWidth: Int get() = child?.width ?: 0

}

fun WidgetContext.Button(child: () -> WWidget, onClick: ((x: Int, y: Int, button: Int) -> Unit)):

//class

fun WidgetContext.Image(path: String,
                        width: Int,
                        height: Int,
                        onClick: ((x: Int, y: Int, button: Int) -> Unit)? = null) {
    add(ImageClass(path, width, height, onClick))
}

fun WidgetContext.Text(text: Text, color: Int = DEFAULT_TEXT_COLOR) {
    val width = widthOf(text.asFormattedString())
    val widget = object : WLabel(text, color) {
        override fun getWidth(): Int = width
    }
    add(widget)
}

fun WidgetContext.Padding(amount: Int, init: WidgetContext.() -> Unit) {
    add(WWidget().apply { setHeight(amount) })
    init()
    add(WWidget().apply { setHeight(amount) })
}

fun WidgetContext.Text(text: String) = Text(LiteralText(text))

private fun widthOf(str: String) = getMinecraftClient().textRenderer.getStringWidth(str)
        .also { logDebug { "The width of '$str' is $it pixels" } }


fun LightweightGuiDescription.drawWidgets(width: Int, height: Int, init: WidgetContext.() -> Unit) {
    val root = WPlainPanel()
    rootPanel = root
    root.setSize(width, height)
    ColumnWidgetCreator().apply(init).positionChildren(root, 0, 0, width, height)
}


class ExampleGui : LightweightGuiDescription() {
    init {
        val nearestCrafter = getNearestCrafter(getMinecraftClient().world, getMinecraftClient().player.pos)
                ?: error("Crafter GUI opened without a crafter multiblock")
        drawWidgets(width = 64, height = 60) {
            Column(Alignment.Center) {
                Padding(3) {
                    Image("gui/button/up_on.png", width = 13, height = 11) { _, _, _ ->
                        sendPacketToServer(
                                Packets.ChangeActiveLayer(nearestCrafter.arbitraryCrafterPos(), nearestCrafter.recipeCreatorCurrentLayer)
                        )
                        nearestCrafter.recipeCreatorCurrentLayer++

                    }
                }
                Image("gui/button/down_on.png", width = 13, height = 11) { _, _, _ ->
                    sendPacketToServer(
                            Packets.ChangeActiveLayer(nearestCrafter.arbitraryCrafterPos(), nearestCrafter.recipeCreatorCurrentLayer)
                    )
                    nearestCrafter.recipeCreatorCurrentLayer--
                }

            }
        }


        logDebug {
            "Opening screen with widget tree =\n ${rootPanel.infoString()}"
        }

        val x = 2

    }


}

private val MappingResolver = FabricLoader.getInstance().mappingResolver

private fun WWidget.infoString(): String = if (this is WPlainPanel) {
    val children = getPrivateField<WPanel, List<WWidget>>("children")
    "[ " + children.joinToString("\n") { it.infoString() } + " ]"
}
else this.javaClass.simpleName + "{x = $x, y = $y, width = $width, height = $height}"
