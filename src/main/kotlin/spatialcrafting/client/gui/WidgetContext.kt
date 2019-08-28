package spatialcrafting.client.gui


////TODO: remove
//interface WidgetContext {
//    fun add(widget: IDevWidget): IDevWidget
//    fun remove(widget: IDevWidget)
//
////    fun IDevWidget.onClick(callback: RuntimeWidget.() -> Unit): IDevWidget {
////        remove(this)
////        return Clickable(this, callback).also { add(it) }
////    }
////
////    fun IDevWidget.onHover(callback: RuntimeWidget.() -> Unit): IDevWidget {
////        remove(this)
////        return SingleChildDevWidget(this) {
////            draw()
////            if (constraints.contains(getClientMouseX(), getClientMouseY())) callback()
////        }.also { add(it) }
////    }
//}
//
//class ChildrenContext : WidgetContext {
//
//    val children: MutableList<IDevWidget> = mutableListOf()
//    override fun add(widget: IDevWidget): IDevWidget {
//        children.add(widget)
//        return widget
//    }
//
//    override fun remove(widget: IDevWidget) {
//        children.remove(widget)
//    }
//}