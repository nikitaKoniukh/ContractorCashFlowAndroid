package com.yetzira.ContractorCashFlowAndroid.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object KablanProShapes {
    val extraSmall = RoundedCornerShape(4.dp)   // Category bar, save badge
    val small = RoundedCornerShape(8.dp)        // Period picker selected item
    val medium = RoundedCornerShape(10.dp)      // StatCardView
    val large = RoundedCornerShape(12.dp)       // Analytics/KPI/period picker cards
    val ipadCard = RoundedCornerShape(14.dp)    // iPad cards, subscribe button
    val extraLarge = RoundedCornerShape(16.dp)  // Detail view cards, header cards, delete button
    val budgetBar = RoundedCornerShape(6.dp)    // Budget/invoice bar container, chart bar
}

val KablanProShapeScheme = Shapes(
    extraSmall = KablanProShapes.extraSmall,
    small = KablanProShapes.small,
    medium = KablanProShapes.medium,
    large = KablanProShapes.large,
    extraLarge = KablanProShapes.extraLarge
)

