package spatialcrafting.client.gui.widgets.core

import spatialcrafting.client.gui.*
import spatialcrafting.client.gui.widgets.core.CrossAxisAlignment.Baseline
import spatialcrafting.client.gui.widgets.core.CrossAxisAlignment.Start
import spatialcrafting.client.gui.widgets.core.Direction.LeftToRight
import spatialcrafting.client.gui.widgets.core.Direction.TopToBottom
import java.lang.Integer.min

enum class MainAxisAlignment {
    Start,
    Center,
    End
}

enum class CrossAxisAlignment {
    Start,
    Baseline
}

enum class FlexSize{
    Expand,
    Wrap
}
abstract class Flex(private val mainAxisAlignment: MainAxisAlignment,
                    private val crossAxisAlignment: CrossAxisAlignment,
                    private val crossAxisSize : FlexSize,
                    overlay: Overlay?) : DevWidget(overlay) {
    fun positionFlexLayout(constraints: Constraints, direction: Direction): MutableList<RuntimeWidget> {
        // "Main Axis Size" means "width/height"
        val constraintsMainAxisStart = startingLocation(direction, constraints)
        val constraintsMainAxisSize = constraintsMainAxisSize(direction, constraints)
        var flexMainAxisSize = flexMainAxisSize(direction, constraints)
        val space = constraintsMainAxisSize - flexMainAxisSize
        val expandingWidgets = expandingWidgetsAmount(direction)
        val constraintsCrossAxisStart = when (direction) {
            LeftToRight -> constraints.y
            TopToBottom -> constraints.x
        }
        val constraintsCrossAxisSize = when (direction) {
            LeftToRight -> constraints.height
            TopToBottom -> constraints.width
        }

        // Split the space evenly between expanding widgets
        val extraSpaceForExpandingWidgets = when {
            space <= 0 -> 0
            expandingWidgets == 0 -> 0
            else -> space / expandingWidgets
        }
        // The expanding widgets makes this widget take all the space
        if (expandingWidgets >= 1) flexMainAxisSize = constraintsMainAxisSize

        var childMainAxisLocation = when (mainAxisAlignment) {
            MainAxisAlignment.Start -> constraintsMainAxisStart
            MainAxisAlignment.Center -> (constraintsMainAxisStart + (constraintsMainAxisSize + constraintsMainAxisStart)) / 2 - flexMainAxisSize / 2
            MainAxisAlignment.End -> constraintsMainAxisStart + constraintsMainAxisSize - flexMainAxisSize
        }

        val children = mutableListOf<RuntimeWidget>()
        for (child in devChildren) {
            var childMainAxisSize = when (direction) {
                LeftToRight -> min(constraints.width, child.minimumWidth)
                TopToBottom -> min(constraints.height, child.minimumHeight)
            }
            val childExpandsInDirection = when (direction) {
                LeftToRight -> child.expandWidth
                TopToBottom -> child.expandHeight
            }

            if (childExpandsInDirection) {
                childMainAxisSize += extraSpaceForExpandingWidgets
            }

            val childCrossAxisSize = when (direction) {
                LeftToRight -> child.heightIn(constraints)
                TopToBottom -> child.widthIn(constraints)
            }
            val childCrossAxisLocation = when (crossAxisAlignment) {
                Start -> constraintsCrossAxisStart
                // It makes sense when you draw it
                Baseline -> (constraintsCrossAxisStart + (+constraintsCrossAxisStart + constraintsCrossAxisSize)) / 2 - childCrossAxisSize / 2
            }


            val childConstraints = when (direction) {
                LeftToRight -> Constraints(
                        x = childMainAxisLocation, y = childCrossAxisLocation,
                        width = childMainAxisSize,
                        height = childCrossAxisSize
                )
                TopToBottom -> Constraints(
                        x = childCrossAxisLocation, y = childMainAxisLocation,
                        width = childCrossAxisSize,
                        height = childMainAxisSize
                )
            }



            children.add(child.layout(childConstraints))

            childMainAxisLocation += childMainAxisSize
        }

        return children
    }

    private fun expandingWidgetsAmount(direction: Direction): Int {
        return devChildren.count {
            when (direction) {
                LeftToRight -> it.expandWidth
                TopToBottom -> it.expandHeight
            }
        }
    }

    private fun flexMainAxisSize(direction: Direction, constraints: Constraints): Int {
        return when (direction) {
            LeftToRight -> min(constraints.width, devChildren.sumBy { it.minimumWidth })
            TopToBottom -> min(constraints.height, devChildren.sumBy { it.minimumHeight })
        }
    }

    private fun constraintsMainAxisSize(direction: Direction, constraints: Constraints): Int {
        return when (direction) {
            LeftToRight -> constraints.width
            TopToBottom -> constraints.height
        }
    }

    private fun startingLocation(direction: Direction, constraints: Constraints): Int {
        return when (direction) {
            LeftToRight -> constraints.x
            TopToBottom -> constraints.y
        }
    }
}

