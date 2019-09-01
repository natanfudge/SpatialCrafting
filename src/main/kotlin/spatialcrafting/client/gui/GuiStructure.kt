package spatialcrafting.client.gui

import spatialcrafting.client.gui.widgets.core.Clickable
import spatialcrafting.client.gui.widgets.core.Overlay
import spatialcrafting.client.gui.widgets.core.TightSingleChildDevWidget
import spatialcrafting.client.gui.widgets.getClientMouseX
import spatialcrafting.client.gui.widgets.getClientMouseY
import spatialcrafting.util.logDebug
import java.lang.Integer.min

data class Constraints(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun contains(x: Int, y: Int) = x >= this.x
            && y >= this.y
            && x < this.x + width
            && y < this.y + height
}


fun DevWidget.widthIn(constraints: Constraints) = if (expandWidth) constraints.width else min(constraints.width, minimumWidth)
fun DevWidget.heightIn(constraints: Constraints) = if (expandHeight) constraints.height else min(constraints.height, minimumHeight)

//TODO: steal a bunch of code from compose
/**
 * The process is like this:
 * 1. User writes a dev widget.
 * That dev widgets contains a function that produces the child dev widgets, as specified by the user.
 * 2. Every dev widget's compose(self) function is called, which adds their child dev widgets.
 * Those children's compose(self) function is also called, and so on.
 * 3. layout() is called on the top-level widget, which produces a runtime widget with absolute constraints and drawing functions.
 * the runtime widget also defines a list of children, of each layout() will be called on as well.
 * 4. After obtaining the runtime widgets, their draw() methods are called every frame.
 * 5. At any time a runtime widget may recompose, with a target dev widget. When that happens, the dev widget will
 * call compose(self) as in (2.) once more, and a new runtime widget of that composition will be obtained from layout().
 * Then, the original runtime widget tree is modified to replace a runtime widget's children, which is obtained from the
 * dev widget's runtimeLayout property, with the new layout().
 * - The dev widget are stored in a tree structure.
 * These dev widgets also contain the functions that add the children dev widgets
 * -
 */

/**
 * Dev widget contain what the dev see. Relative ordering of elements
 */
abstract class DevWidget(
        val overlay: Overlay?) {
    abstract val minimumHeight: Int
    abstract val minimumWidth: Int
    open val expandHeight: Boolean get() = false
    open val expandWidth: Boolean get() = false

    val devChildren: MutableList<DevWidget> = mutableListOf()
    fun <T : DevWidget> add(widget: T) = widget.also { devChildren.add(it) }


    /**
     * Used to go back during runtime and change the runtime layout through the dev widget
     */
    private lateinit var runtimeLayout: RuntimeWidget

    /**
     * Convert a dev widget to a runtime widget.
     */
    fun layout(constraints: Constraints) = getLayout(constraints).also {
        if (!::runtimeLayout.isInitialized) runtimeLayout = it
    }

    protected abstract fun getLayout(constraints: Constraints): RuntimeWidget


    /**
     * Function of self
     */
    abstract val composeDirectChildren: DevWidget.() -> Unit /*= {}*/


    fun <T : DevWidget> T.onClick(callback: T.(runtimeWidget: RuntimeWidget) -> Unit): DevWidget {
        this@DevWidget.devChildren.remove(this)
        return this@DevWidget.add(Clickable(overlay, callback) { add(this@onClick) })
    }

    fun DevWidget.onHover(callback: RuntimeWidget.(mouseX: Int, mouseY: Int) -> Unit): DevWidget {
        this@DevWidget.devChildren.remove(this)
        return this@DevWidget.add(TightSingleChildDevWidget(composeDirectChildren = { add(this@onHover) }, drawer = {
            it.draw()
            if (it.constraints.contains(getClientMouseX(), getClientMouseY())) it.callback(getClientMouseX(), getClientMouseY())
        }, overlay = overlay))
    }


    fun recompose(target: DevWidget) = target.apply {
        devChildren.clear()
        walk { it.composeDirectChildren(it) }
        runtimeLayout.runtimeChildren = layout(runtimeLayout.constraints).runtimeChildren

        logDebug {
            "Recomposing:\n ${target.runtimeLayout.infoString()}"
        }
    }


}


/**
 * Runtime Widgets have absolute positions and drawing functions.
 */
interface RuntimeWidget {
    val constraints: Constraints
    var runtimeChildren: List<RuntimeWidget>
        get() = listOf()
        set(_) {}

    fun draw()
    val origin: DevWidget

    val debugIdentifier: String get() = "RuntimeWidget"

}

