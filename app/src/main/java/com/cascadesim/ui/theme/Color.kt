package com.cascadesim.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors
val CascadePrimary = Color(0xFF1976D2)
val CascadePrimaryVariant = Color(0xFF115293)
val CascadeOnPrimary = Color(0xFFFFFFFF)

// Secondary Colors
val CascadeSecondary = Color(0xFF03DAC6)
val CascadeSecondaryVariant = Color(0xFF018A7A)
val CascadeOnSecondary = Color(0xFF000000)

// Background Colors
val CascadeBackground = Color(0xFFFAFAFA)
val CascadeSurface = Color(0xFFFFFFFF)
val CascadeOnBackground = Color(0xFF000000)
val CascadeOnSurface = Color(0xFF000000)

// Status Colors - Crisis Red
val CrisisRed = Color(0xFFD32F2F)
val CrisisRedLight = Color(0xFFFF6659)
val CrisisRedDark = Color(0xFF9A0007)

// Status Colors - Stability Green
val StabilityGreen = Color(0xFF388E3C)
val StabilityGreenLight = Color(0xFF6ABF6E)
val StabilityGreenDark = Color(0xFF00600F)

// Warning Colors
val WarningOrange = Color(0xFFFFA000)
val WarningOrangeLight = Color(0xFFFFD149)
val WarningOrangeDark = Color(0xFFC57100)

// Cascade Level Colors
val CascadeLevelStable = StabilityGreen
val CascadeLevelUnstable = WarningOrange
val CascadeLevelCritical = CrisisRed
val CascadeLevelCascade = Color(0xFF7B1FA2)

// Event Severity Colors
val EventSeverityLow = StabilityGreen
val EventSeverityMedium = WarningOrange
val EventSeverityHigh = CrisisRed
val EventSeverityCritical = Color(0xFF8E0000)
val EventSeverityCatastrophic = Color(0xFF4A0000)

// Error Colors
val ErrorRed = Color(0xFFB00020)
val ErrorRedLight = Color(0xFFCF6679)
