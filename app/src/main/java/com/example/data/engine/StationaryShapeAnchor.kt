package com.example.data.engine

enum class ShapeType {
    RECTANGLE_CARD,
    BUTTON_CIRCLE,
    TOOLBAR_LEDGE,
    TEXT_INPUT_BOX,
    SYSTEM_BAR,
    SIDE_DOCK
}

data class StationaryShapeAnchor(
    val id: String,
    val label: String,
    val shapeType: ShapeType,
    val boundsLeftPx: Float,
    val boundsTopPx: Float,
    val boundsRightPx: Float,
    val boundsBottomPx: Float,
    val perchX: Float,
    val perchY: Float,
    val surfaceColorHex: String = "#381A66",
    val isOccupiedByPet: Boolean = false
) {
    val widthPx: Float get() = (boundsRightPx - boundsLeftPx).coerceAtLeast(1f)
    val heightPx: Float get() = (boundsBottomPx - boundsTopPx).coerceAtLeast(1f)
}
