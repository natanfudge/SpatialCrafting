package spatialcrafting.client.gui.widgets

import spatialcrafting.client.gui.Constraints
import spatialcrafting.client.gui.DevWidget
import spatialcrafting.client.gui.RuntimeWidget
import spatialcrafting.client.gui.widgets.CrossAxisAlignment.Baseline
import spatialcrafting.client.gui.widgets.CrossAxisAlignment.Start
import spatialcrafting.client.gui.widgets.Direction.LeftToRight
import spatialcrafting.client.gui.widgets.Direction.TopToBottom

enum class MainAxisAlignment {
    Start,
    Center,
    End
}

enum class CrossAxisAlignment {
    Start,
    Baseline
}

abstract class Flex(private val mainAxisAlignment: MainAxisAlignment, private val crossAxisAlignment: CrossAxisAlignment) : DevWidget() {
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
                LeftToRight -> child.minimumWidth
                TopToBottom -> child.minimumHeight
            }
            val childExpandsInDirection = when (direction) {
                LeftToRight -> child.expandWidth
                TopToBottom -> child.expandHeight
            }

            if (childExpandsInDirection) {
                childMainAxisSize += extraSpaceForExpandingWidgets
            }

            val childCrossAxisSize = when (direction) {
                LeftToRight -> if (child.expandHeight) constraints.height else child.minimumHeight
                TopToBottom -> if (child.expandWidth) constraints.width else child.minimumWidth
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
            LeftToRight -> Integer.min(constraints.width, devChildren.sumBy { it.minimumWidth })
            TopToBottom -> Integer.min(constraints.height, devChildren.sumBy { it.minimumHeight })
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

