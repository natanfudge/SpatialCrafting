package spatialcrafting.client.gui

import spatialcrafting.client.gui.widgets.Clickable

data class Constraints(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun contains(x: Int, y: Int) = x >= this.x
            && y >= this.y
            && x < this.x + width
            && y < this.y + height
}
//TODO: maybe go back to WidgetContext

abstract class DevWidget {
    abstract val minimumHeight: Int
    abstract val minimumWidth: Int
    open val expandHeight: Boolean get() = false
    open val expandWidth: Boolean get() = false

    val devChildren: MutableList<DevWidget> = mutableListOf()
    fun add(widget: DevWidget) = widget.also { devChildren.add(it) }

    private lateinit var runtimeLayout: RuntimeWidget

    fun layout(constraints: Constraints) = getLayout(constraints).also {
        if(!::runtimeLayout.isInitialized) runtimeLayout = it
    }
    protected abstract fun getLayout(constraints: Constraints): RuntimeWidget


    abstract val compose: DevWidget.() -> Unit


    fun DevWidget.onClick(callback: RuntimeWidget.() -> Unit): DevWidget {
        this@DevWidget.devChildren.remove(this)
        return this@DevWidget.add(Clickable(callback).also { it.add(this) })
    }

    fun recompose() {
        devChildren.clear()
        compose()

//        val newLayout
//        newLayout.parent = runtimeLayout.parent
//
//        runtimeLayout.parent

        runtimeLayout.runtimeChildren = layout(runtimeLayout.constraints).runtimeChildren
        val x = 2

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
    val composer: DevWidget

    val debugIdentifier: String get() = "RuntimeWidget"

//    override fun toString() = "RuntimeWidget{ constraints = { $constraints }" +
//            if(runtimeChildren.isNotEmpty()) ", children = [\n" + runtimeChildren.joinToString("\n")  + "\n]" else " "+
//                    "}"

//    fun drawBackground(){}
//    fun drawForeground(){}
}

