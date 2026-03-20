package com.yetzira.ContractorCashFlowAndroid.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object KablanProShapes {
    val extraSmall = RoundedCornerShape(4.dp)
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(10.dp)
    val large = RoundedCornerShape(12.dp)
    val extraLarge = RoundedCornerShape(16.dp)
}

val KablanProShapeScheme = Shapes(
    extraSmall = KablanProShapes.extraSmall,
    small = KablanProShapes.small,
    medium = KablanProShapes.medium,
    large = KablanProShapes.large,
    extraLarge = KablanProShapes.extraLarge
)

