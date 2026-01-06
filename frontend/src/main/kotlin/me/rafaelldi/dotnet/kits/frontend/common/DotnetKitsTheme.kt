package me.rafaelldi.dotnet.kits.frontend.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jetbrains.jewel.bridge.toComposeColor
import org.jetbrains.jewel.foundation.theme.JewelTheme

/**
 * Centralized design tokens for the .NET Kits tool window.
 *
 * This object provides a consistent design system including:
 * - Spacing scale based on 8dp grid
 * - Typography definitions with semantic text styles
 * - Semantic color naming
 * - Component sizes and shapes
 *
 * All UI components in the toolWindow package should reference
 * these constants instead of hardcoding values.
 *
 * ## Usage Example
 * ```kotlin
 * Text(
 *     text = "Example",
 *     style = DotnetKitsTheme.Typography.artifactVersionStyle(),
 *     modifier = Modifier.padding(DotnetKitsTheme.Spacing.xSmall)
 * )
 * ```
 */
object DotnetKitsTheme {

    /**
     * Spacing scale following the 8dp grid system.
     *
     * The scale provides both absolute values and semantic names for common use cases.
     */
    object Spacing {
        // Base spacing scale
        val xxxSmall = 2.dp
        val xxSmall = 4.dp
        val xSmall = 8.dp
        val small = 12.dp
        val medium = 16.dp
        val large = 24.dp
        val xLarge = 32.dp

        // Semantic spacing for specific components
        val cardPaddingHorizontal = xSmall
        val cardPaddingVertical = xxSmall
        val cardInnerPadding = medium
        val menuItemPadding = xSmall
        val iconSpacing = xSmall
        val itemSpacing = xxxSmall
        val listContentPadding = xSmall
    }

    /**
     * Typography scale with semantic text style functions.
     *
     * Provides both raw values and composable functions that return complete text styles
     * with appropriate font sizes, weights, colors, and line heights.
     */
    object Typography {
        // Font sizes
        val bodyFontSize = 14.sp
        val titleFontSize = 16.sp
        val captionFontSize = 12.sp

        // Line heights
        val lineHeightNormal = 20.sp
        val lineHeightCompact = 16.sp

        /**
         * Text style for artifact version numbers.
         *
         * Bold, normal-sized text in the default text color.
         */
        @Composable
        fun artifactVersionStyle() = JewelTheme.Companion.defaultTextStyle.copy(
            fontSize = bodyFontSize,
            fontWeight = FontWeight.Companion.Bold,
            color = JewelTheme.Companion.globalColors.text.normal,
            lineHeight = lineHeightNormal
        )

        /**
         * Text style for artifact paths.
         *
         * Normal weight, normal-sized text in an info color to de-emphasize.
         */
        @Composable
        fun artifactPathStyle() = JewelTheme.Companion.defaultTextStyle.copy(
            fontSize = bodyFontSize,
            fontWeight = FontWeight.Companion.Normal,
            color = JewelTheme.Companion.globalColors.text.info,
            lineHeight = lineHeightNormal
        )

        /**
         * Text style for empty state messages.
         *
         * Slightly larger text in a disabled color to indicate inactive state.
         */
        @Composable
        fun emptyStateStyle() = JewelTheme.Companion.defaultTextStyle.copy(
            fontSize = titleFontSize,
            color = JewelTheme.Companion.globalColors.text.disabled
        )

        /**
         * Text style for tooltips.
         *
         * Uses platform-specific tooltip foreground color to ensure consistency
         * with native IDE tooltips. The caption font size provides appropriate
         * hierarchy for supplementary tooltip text.
         */
        @Composable
        fun tooltipStyle() = JewelTheme.Companion.defaultTextStyle.copy(
            fontSize = captionFontSize,
            color = JBUI.CurrentTheme.Tooltip.foreground().toComposeColor()
        )
    }

    /**
     * Component sizes for icons, borders, and other UI elements.
     */
    object Sizes {
        val iconSmall = 16.dp
        val iconMedium = 24.dp
        val borderWidthDefault = 1.dp
        val menuMinWidth = 100.dp
    }

    /**
     * Shapes for cards and other components.
     */
    object Shapes {
        val cardCornerRadius = 8.dp
        val cardShape = RoundedCornerShape(cardCornerRadius)
    }

    /**
     * Semantic color definitions for components.
     */
    object Colors {
        /**
         * Background color for artifact cards.
         *
         * Uses standard panel background color, which is semantically appropriate
         * for container components like cards. This ensures consistent appearance
         * with other panels in the IDE and avoids the semantic mismatch of using
         * banner colors (designed for notifications) for list item containers.
         *
         * This color automatically adapts to the current IDE theme (light/dark/high contrast).
         */
        @Composable
        fun cardBackground() = UIUtil.getPanelBackground().toComposeColor()

        /**
         * Border color for artifact cards.
         *
         * Uses standard border color, which provides subtle visual boundaries
         * consistent with other bordered components in the IDE. This is semantically
         * more appropriate than banner border colors for list item cards.
         *
         * This color automatically adapts to the current IDE theme and ensures
         * proper contrast and visibility across all themes.
         */
        @Composable
        fun cardBorder() = JBColor.border().toComposeColor()

        /**
         * Background color for artifact cards when hovered.
         *
         * Uses the standard list hover background color from the IntelliJ Platform,
         * ensuring consistency with other list-based components in the IDE.
         * The focused=true parameter provides vibrant hover feedback appropriate
         * for tool window lists.
         *
         * This color automatically adapts to the current IDE theme.
         */
        @Composable
        fun cardHoverBackground() = JBUI.CurrentTheme.List.Hover.background(true).toComposeColor()
    }
}