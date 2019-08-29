package spatialcrafting.client.gui

import spatialcrafting.client.gui.widgets.Clickable
import spatialcrafting.client.gui.widgets.TightSingleChildDevWidget
import spatialcrafting.util.logDebug

data class Constraints(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun contains(x: Int, y: Int) = x >= this.x
            && y >= this.y
            && x < this.x + width
            && y < this.y + height
}
//TODO: maybe go back to WidgetContext
//TODO: document this system
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
abstract class DevWidget {
    abstract val minimumHeight: Int
    abstract val minimumWidth: Int
    open val expandHeight: Boolean get() = false
    open val expandWidth: Boolean get() = false

    val devChildren: MutableList<DevWidget> = mutableListOf()
    fun add(widget: DevWidget) = widget.also { devChildren.add(it) }

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
    open val composeDirectChildren: DevWidget.() -> Unit = {}


    fun DevWidget.onClick(callback: RuntimeWidget.() -> Unit): DevWidget {
        this@DevWidget.devChildren.remove(this)
        return this@DevWidget.add(Clickable(callback) { add(this@onClick) })
    }

    fun DevWidget.onHover(callback: RuntimeWidget.() -> Unit): DevWidget {
        this@DevWidget.devChildren.remove(this)
        return this@DevWidget.add(TightSingleChildDevWidget(composeDirectChildren = { add(this@onHover) }, drawer = {
            it.draw()
            if (it.constraints.contains(getClientMouseX(), getClientMouseY())) it.callback()
        }))
    }

    fun recompose(target: DevWidget) = target.apply {
        devChildren.clear()
        walk { it.composeDirectChildren(it) }
        runtimeLayout.runtimeChildren = layout(runtimeLayout.constraints).runtimeChildren

        logDebug {
            "Recomposing:\n ${target.runtimeLayout.infoString()}"
        }
    }

//    fun IDevWidget.onHover(callback: RuntimeWidget.() -> Unit): IDevWidget {
//        devChildren.remove(this)
//        return SingleChildDevWidget(this) {
//            draw()
//            if (constraints.contains(getClientMouseX(), getClientMouseY())) callback()
//        }.also { add(it) }
//    }
}


//abstract class DevWidget : IDevWidget {
//    override val devChildren: MutableList<IDevWidget> = mutableListOf()
//    override lateinit var runtimeLayout: RuntimeWidget
//    protected fun getLayout(constraints: Constraints): RuntimeWidget{
//
//    }
//}


//fun RuntimeWidget.recompose(){
////    // My children recompose
////    for(child in runtimeChildren) child.recompose()
//    // I recompose
//    runtimeChildren = runtimeChildren.map { it.composer.position(it.constraints) }
//
//
//}

/**
 * Runtime Widgets have absolute positions and drawing functions.
 */
interface RuntimeWidget {
    val constraints: Constraints
    var runtimeChildren: List<RuntimeWidget>
        get() = listOf()
        set(value) {
            val x = 2
        }
//        get() = listOf()
//        set(value) {}

//    var parent: RuntimeWidget?
//    set(value) {field = value}

//    val parent : RuntimeWidget

    fun draw()
    val origin: DevWidget

    val debugIdentifier: String get() = "RuntimeWidget"

//    override fun toString() = "RuntimeWidget{ constraints = { $constraints }" +
//            if(runtimeChildren.isNotEmpty()) ", children = [\n" + runtimeChildren.joinToString("\n")  + "\n]" else " "+
//                    "}"

//    fun drawBackground(){}
//    fun drawForeground(){}
}

