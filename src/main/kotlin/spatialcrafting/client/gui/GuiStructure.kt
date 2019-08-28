package spatialcrafting.client.gui

data class Constraints(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun contains(x: Int, y: Int) = x >= this.x
            && y >= this.y
            && x < this.x + width
            && y < this.y + height
}

interface DevWidget {
    val minimumHeight: Int
    val minimumWidth: Int
    val expandHeight: Boolean get() = false
    val expandWidth: Boolean get() = false

    fun position(constraints: Constraints): RuntimeWidget
    val compose : () -> Unit
}

fun RuntimeWidget.recompose(){
//    // My children recompose
//    for(child in runtimeChildren) child.recompose()
    // I recompose
    runtimeChildren = runtimeChildren.map { it.composer.position(it.constraints) }


}

interface RuntimeWidget {
    val constraints: Constraints
    var runtimeChildren: List<RuntimeWidget>
        get() = listOf()
        set(value){}

//    val parent : RuntimeWidget

    fun draw()
    val composer: DevWidget

    val debugIdentifier : String get() = "RuntimeWidget"

//    override fun toString() = "RuntimeWidget{ constraints = { $constraints }" +
//            if(runtimeChildren.isNotEmpty()) ", children = [\n" + runtimeChildren.joinToString("\n")  + "\n]" else " "+
//                    "}"

//    fun drawBackground(){}
//    fun drawForeground(){}
}

