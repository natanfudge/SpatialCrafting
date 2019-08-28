//package spatialcrafting.client.gui.widgets
//
//import spatialcrafting.client.gui.Constraints
//import spatialcrafting.client.gui.DevWidget
//import spatialcrafting.client.gui.RuntimeWidget
//import spatialcrafting.client.gui.runtimeWidget
//
// class SingleChildDevWidget(private val child : DevWidget,private val drawer : RuntimeWidget.() -> Unit) : DevWidget{
//    override val minimumHeight = child.minimumHeight
//    override val minimumWidth  = child.minimumWidth
//    override fun position(constraints: Constraints) = runtimeWidget(constraints,
//            listOf(child.position(constraints))){ drawer(this.runtimeChildren.first()) }
//
//
//}