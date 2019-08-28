
package spatialcrafting.client.gui

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import spatialcrafting.util.getPrivateField

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import spatialcrafting.compat.rei.getNearestCrafter
import spatialcrafting.util.getMinecraftClient


//
//inline fun WidgetContext.Column(alignment: Alignment = Alignment.Top,
//                                init: WidgetContext.() -> Unit) =
//        add(ColumnClass(mutableListOf()).apply(init))
//
////class RowWidgetCreator : WidgetContext, WWidget() {
////    private val elements = mutableListOf<WWidget>()
////    private val resizeableElements = mutableMapOf<Int, WidgetContext>()
////    override fun add(widget: WWidget) {
////        elements.add(widget)
////    }
////
////    override val minimumHeight get() = elements.maxBy { it.height }?.height ?: 0
////    override val minimumWidth get() = elements.sumBy { it.width }
////    override val expandHeight: Boolean = false
////    override val expandWidth: Boolean = false
////
////
////    override fun positionChildren(parent: WPlainPanel, x: Int, y: Int, width: Int, height: Int) {
//////        when (alignment) {
//////            Alignment.Top -> {
////        val space = width - elements.sumBy { if (it is WidgetContext) 0 else it.width }
////        val spaceForEach = space / elements.sumBy { if (it is WidgetContext) 1 else 0 }
////        var currentX = x
////        for (widget in elements) {
////            if (widget is WidgetContext) {
////                //TODO: split the space between them
////                val spaceTaken = max(min(minimumWidth, spaceForEach), 0)
////                widget.positionChildren(parent, currentX, y, width, spaceTaken)
////                currentX += spaceTaken
////            }
////            else {
////                parent.add(widget, currentX, y, widget.width, widget.height)
////                currentX += widget.width
////            }
////
////        }
//////
//////            }
//////            Alignment.Center -> {
////////                parent.addWidgets(startingPoint = spatialcrafting.compat.rei.height / 2 - elements.sumBy { it.height } / 2)
////////
////////                var startingPoint = (x + height) / 2 - elements.sumBy { it.height } / 2
////////
////////                for (widget in elements) {
////////                    parent.add(widget, x, startingPoint, widget.width, widget.height)
////////                    startingPoint += widget.height
////////                }
//////            }
//////        }
////    }
////}
////
////inline fun WidgetContext.Row(init: WidgetContext.() -> Unit) = add(RowWidgetCreator().apply(init))

////
////fun WWidget.isHighlighted(x: Int, y: Int, mouseX: Int, mouseY: Int) = mouseX >= x
////        && mouseY >= y
////        && mouseX < x + width
////        && mouseY < y + height
//@Suppress("LeakingThis")
//open class ImageClass(path: String,
//                      width: Int,
//                      height: Int,
//                      private val onClick: ((x: Int, y: Int, button: Int) -> Unit)? = null) : WSprite(Identifier("$ModId:textures/$path")) {
//    override fun onClick(x: Int, y: Int, button: Int) {
//        playButtonClickSoundClient()
//        onClick?.invoke(x, y, button)
//    }
//
//    override fun paintForeground(x: Int, y: Int, mouseX: Int, mouseY: Int) {
//        if (onClick != null && isHighlighted(x, y, mouseX, mouseY)) {
//
////            ScreenDrawing.rect(x, y, getWidth(), getHeight(), 0x40_00_00_FF)
////            ScreenDrawing.rect(x, y, getWidth(), getHeight(), 0x40_00_00_FF)
//        }
//        super.paintForeground(x, y, mouseX, mouseY)
//    }
//
//
//    init {
//        setSize(width, height)
//    }
//}
//
//class GuiException(message: String) : Exception(message)
//
////class ButtonClass(private val onClick: ((x: Int, y: Int, button: Int) -> Unit)) : WWidget(), WidgetContext {
////    private var child: WWidget? = null
////    override fun add(widget: WWidget) {
////        if (child != null) throw GuiException("Button cannot contain more than one child")
////        child = widget
////    }
////
////    override fun positionChildren(parent: WPlainPanel, x: Int, y: Int, width: Int, height: Int) {
////        child?.let {
////            if (it is WidgetContext) it.positionChildren(parent, x, y, width, height)
////            else parent.add(it, x, y, width, height)
////        }
////    }
////
////    override val minimumHeight: Int get() = child?.height ?: 0
////    override val minimumWidth: Int get() = child?.width ?: 0
////
////}
////
////fun WidgetContext.Button(child: () -> WWidget, onClick: ((x: Int, y: Int, button: Int) -> Unit)):
//
////class
////
////fun WidgetContext.Image(path: String,
////                        width: Int,
////                        height: Int,
////                        onClick: ((x: Int, y: Int, button: Int) -> Unit)? = null) {
////    add(ImageClass(path, width, height, onClick))
////}
////
////fun WidgetContext.Text(text: Text, color: Int = DEFAULT_TEXT_COLOR) {
////    val width = widthOf(text.asFormattedString())
////    val widget = object : WLabel(text, color) {
////        override fun getWidth(): Int = width
////    }
////    add(widget)
////}
////
////fun WidgetContext.Padding(amount: Int, init: WidgetContext.() -> Unit) {
////    add(WWidget().apply { setHeight(amount) })
////    init()
////    add(WWidget().apply { setHeight(amount) })
////}
////
////fun WidgetContext.Text(text: String) = Text(LiteralText(text))
//

////private fun heightOf(str: String) = getMinecraftClient().textRenderer.getStringWidth(str)
////        .also { logDebug { "The width of '$str' is $it pixels" } }
//
//
//interface WidgetContext {
//    fun add(widget: IWidget)
//}
//
//
//enum class Alignment {
//    Top,
//    Center
//}
//
//
//private fun WWidget.setWidth(width: Int) = setSize(width, height)
//private fun WWidget.setHeight(height: Int) = setSize(width, height)
//
////TODO make into interface
//abstract class IWidget : WidgetContext {
//    abstract val children: MutableList<IWidget>
//    abstract val minimumHeight: Int
//    abstract val minimumWidth: Int
//    open val expandHeight: Boolean get() = false
//    open val expandWidth: Boolean get() = false
//
//
//
//    abstract fun getDrawer(x: Int, y: Int, width: Int, height: Int)
//
//    override fun add(widget: IWidget) {
//        children.add(widget)
//    }
//
//    abstract fun recompose()
//}
//
//
//class SingleWidget : WidgetContext {
//    var widget: IWidget? = null
//    override fun add(widget: IWidget) {
//        if (this.widget != null) throw GuiException("Only add one top-level widget in compose()")
//        this.widget = widget
//    }
//}
//
//abstract class ComposedWidget : IWidget() {
//    override val children: MutableList<IWidget> = mutableListOf()
//
//    private var child
//        get() = children.firstOrNull()
//        set(value) {
//            if (value != null) children[0] = value
//        }
//
//    //    var widget: IWidget? = null
//    override var minimumHeight: Int by Delegates.notNull()
//    override var minimumWidth: Int by Delegates.notNull()
//    override var expandHeight: Boolean by Delegates.notNull()
//    override var expandWidth: Boolean by Delegates.notNull()
//
//    override fun getDrawer(x: Int, y: Int, width: Int, height: Int)  = child.getDrawer(x,y,width,height)
//
////    override fun draw(x: Int, y: Int, width: Int, height: Int): Unit = child?.draw(x, y, width, height)
////            ?: Unit
//
//    final override fun recompose() {
//        child = SingleWidget().apply { compose() }.widget
//        minimumHeight = child?.minimumHeight ?: 0
//        minimumWidth = child?.minimumWidth ?: 0
//        expandHeight = child?.expandHeight ?: false
//        expandWidth = child?.expandWidth ?: false
//    }
//
//    abstract fun SingleWidget.compose()
//
//}
//
//
//class ColumnClass(override val children: MutableList<IWidget>) : IWidget() {
//    override val minimumHeight: Int
//        get() = children.sumBy { it.minimumHeight }
//    override val minimumWidth: Int
//        get() = children.maxValueBy { it.minimumWidth } ?: 0
//    override val expandHeight = true
//    override val expandWidth = false
//
//    override fun getDrawer(x: Int, y: Int, width: Int, height: Int)  = object
//
//    override fun draw(x: Int, y: Int, width: Int, height: Int) {
//        drawChildren(x = x, startingPoint = y, width = width, height = height)
//    }
//
//    override fun toString(): String = "[\n" + children.joinToString("\n") + "\n]"
//
//    override fun recompose() {
//        //TODO: need to make the actual children amount changeable
//        for (child in children) child.recompose()
//    }
//
//    private fun drawChildren(x: Int, startingPoint: Int, width: Int, height: Int) {
//        val space = height - children.sumBy { it.minimumHeight }
//        val expandingWidgets = children.count { it.expandHeight }
//        val extraSpaceForExpandingWidgets = when {
//            space <= 0 -> 0
//            expandingWidgets == 0 -> 0
//            else -> space / expandingWidgets
//        }
//        var currentY = startingPoint
//        for (widget in children) {
//            widget.draw(x, currentY, width, height)
//            currentY += widget.minimumHeight
//            if (widget.expandHeight) {
//                currentY += extraSpaceForExpandingWidgets
//            }
//
//        }
//    }
//
//}
//
//
////    override fun positionChildren(parent: WPlainPanel, x: Int, y: Int, width: Int, height: Int) {
////        when (alignment) {
////            Alignment.Top -> {
////                placeChildren(parent, x, y, width, height)
////            }
////            Alignment.Center -> {
////                val startingPoint = (y + height) / 2 - elements.sumBy { it.height } / 2
////                placeChildren(parent, x, startingPoint, width, height)
////            }
////        }
////    }
////
////    private fun placeChildren(parent: WPlainPanel, x: Int, startingPoint: Int, width: Int, height: Int) {
////        val space = height - elements.sumBy { if (it is WidgetContext) 0 else it.height }
////        val spaceForEach = elements.sumBy { if (it is WidgetContext) 1 else 0 }.let { if (it == 0) 0 else space / it }
////        var currentY = startingPoint
////        for (widget in elements) {
////            if (widget is WidgetContext) {
////                //TODO: split the space between them
////                val spaceTaken = if (widget.expandHeight) space else max(min(minimumHeight, spaceForEach), 0)
////                widget.positionChildren(parent, x, currentY, width, spaceTaken)
////                currentY += spaceTaken
////            }
////            else {
////                parent.add(widget, x, currentY, widget.width, widget.height)
////                currentY += widget.height
////            }
////
////        }
////    }
//
//
//class ChildrenHolder : WidgetContext {
//    val children = mutableListOf<IWidget>()
//    override fun add(widget: IWidget) {
//        children.add(widget)
//    }
//
//}
//
//private fun IWidget.walk(visitor: (IWidget) -> Unit) {
//    visitor(this)
//    for (child in children) child.walk(visitor)
//}
//
//abstract class WidgetDrawer {
//    val x: Int = 0
//    val y: Int = 0
//    val width: Int = 0
//    val height: Int = 0
//
//    abstract fun draw()
//}
//
//fun LightweightGuiDescription.drawWidgets(width: Int, height: Int, init: WidgetContext.() -> Unit) {
//    val column = ColumnClass(ChildrenHolder().apply(init).children)
//    column.recompose()
//    val root = object : WWidget() {
//        override fun paintBackground(x: Int, y: Int) {
//            column.draw(x, y, width, height)
//        }
//    }
//    rootPanel = object : WPlainPanel() {
//        override fun onClick(x: Int, y: Int, button: Int) {
//            column.walk {
//                if (isHighlighted(it))
//            }
//        }
//    }.apply {
//        add(root, 0, 0, width, height)
//        setSize(width, height)
//    }
//
//    logDebug {
//        "Opening screen with widget tree =\n $column"
//    }
//
//}
//
////TODO: composables can be simplified to just be a function I think
//
////TODO: wrappers can probably be simplified. Maybe even a wrapper for all WWidgets.
//
//
//
////class ButtonWrapper(private val child : IWidget, private val onClick: ((x: Int, y: Int, button: Int) -> Unit)) : IWidget{
////
////}
//
//fun WidgetContext.Text(text: String) = add(TextWrapper(LiteralText(text), DEFAULT_TEXT_COLOR))


//class ExampleGui : LightweightGuiDescription() {
//    init {
//        val nearestCrafter = getNearestCrafter(getMinecraftClient().world, getMinecraftClient().player.pos)
//                ?: error("Crafter GUI opened without a crafter multiblock")
//        drawWidgets(width = 64, height = 60) {
//            Text("asdfasdf")
//            Text("asdfasdf")
//            Text("asdfasdf")
//
//        }
//
//
//        val x = 2
//
//    }
//
//
//}

//                Padding(3) {
//                    Image("gui/button/up_on.png", width = 13, height = 11) { _, _, _ ->
//                        sendPacketToServer(
//                                Packets.ChangeActiveLayer(nearestCrafter.arbitraryCrafterPos(), nearestCrafter.recipeCreatorCurrentLayer)
//                        )
//                        nearestCrafter.recipeCreatorCurrentLayer++
//
//                    }
//                }
//                Image("gui/button/down_on.png", width = 13, height = 11) { _, _, _ ->
//                    sendPacketToServer(
//                            Packets.ChangeActiveLayer(nearestCrafter.arbitraryCrafterPos(), nearestCrafter.recipeCreatorCurrentLayer)
//                    )
//                    nearestCrafter.recipeCreatorCurrentLayer--
//                }

//            }

//private val MappingResolver = FabricLoader.getInstance().mappingResolver
//
//private fun WWidget.infoString(): String = if (this is WPlainPanel) {
//    val children = getPrivateField<WPanel, List<WWidget>>("children")
//    "[ " + children.joinToString("\n") { it.infoString() } + " ]"
//}
//else this.javaClass.simpleName + "{x = $x, y = $y, width = $width, height = $height}"
