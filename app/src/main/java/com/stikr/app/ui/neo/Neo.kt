package com.stikr.app.ui.neo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stikr.app.ui.theme.Anton
import com.stikr.app.ui.theme.LocalNeoPalette
import com.stikr.app.ui.theme.NeoPalette

/** Access to the theme's palette from any composable. */
object Neo {
    val palette: NeoPalette
        @Composable get() = LocalNeoPalette.current
}

/**
 * The one modifier the whole look rests on: an ink border drawn inside the
 * layout bounds plus a hard, un-blurred offset shadow behind it — the
 * sticker-print "neobrutalist" frame.
 */
fun Modifier.neoFrame(
    shape: Shape = RoundedCornerShape(16.dp),
    fill: Color,
    borderColor: Color,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color,
): Modifier = drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    if (shadowOffset > 0.dp) {
        translate(left = shadowOffset.toPx(), top = shadowOffset.toPx()) {
            drawOutline(outline, color = shadowColor)
        }
    }
    drawOutline(outline, color = fill)
    if (borderWidth > 0.dp) {
        drawOutline(outline, color = borderColor, style = Stroke(width = borderWidth.toPx()))
    }
}

/** A framed surface: ink border + hard shadow, with optional click. */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    fill: Color = Neo.palette.card,
    shape: Shape = RoundedCornerShape(16.dp),
    shadow: Dp = 4.dp,
    borderWidth: Dp = 2.dp,
    padding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = Neo.palette
    Column(
        modifier = modifier
            .neoFrame(shape, fill, palette.ink, borderWidth, shadow, palette.shadow)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

/** Primary/secondary action: filled, framed, and it presses *into* its shadow. */
@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Color = Neo.palette.butter,
    contentColor: Color = Color.Unspecified,
    enabled: Boolean = true,
    shadow: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
    leading: (@Composable () -> Unit)? = null,
) {
    val palette = Neo.palette
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink = if (pressed && enabled && shadow > 0.dp) shadow else 0.dp
    val labelColor = when {
        !enabled -> palette.muted
        contentColor != Color.Unspecified -> contentColor
        fill.luminance() > 0.45f -> palette.onAccent
        else -> palette.ink
    }

    Row(
        modifier = modifier
            .offset(x = sink, y = sink)
            .neoFrame(
                shape = shape,
                fill = if (enabled) fill else palette.creamDim,
                borderColor = if (enabled) palette.ink else palette.ink.copy(alpha = 0.3f),
                borderWidth = 2.dp,
                shadowOffset = shadow - sink,
                shadowColor = palette.shadow,
            )
            .clip(shape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        leading?.invoke()
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor,
        )
    }
}

/** Secondary action: a framed card-colored button. */
@Composable
fun NeoOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fill: Color = Neo.palette.card,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
    leading: (@Composable () -> Unit)? = null,
) {
    NeoButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        fill = fill,
        enabled = enabled,
        contentPadding = contentPadding,
        leading = leading,
    )
}

/** A round, framed icon button — back arrows, overflow menus, small actions. */
@Composable
fun NeoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    fill: Color = Neo.palette.card,
    tint: Color = Color.Unspecified,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    shape: Shape = CircleShape,
    shadow: Dp = 3.dp,
    borderWidth: Dp = 2.dp,
    enabled: Boolean = true,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    val palette = Neo.palette
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink = if (pressed && enabled && shadow > 0.dp) shadow else 0.dp
    val iconTint = when {
        tint != Color.Unspecified -> tint
        fill.luminance() > 0.45f -> palette.onAccent
        else -> palette.ink
    }

    Box(
        modifier = modifier
            .offset(x = sink, y = sink)
            .size(size)
            .neoFrame(shape, fill, palette.ink, borderWidth, shadow - sink, palette.shadow)
            .clip(shape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = contentDescription, tint = iconTint, modifier = Modifier.size(iconSize))
        }
        content?.invoke(this)
    }
}

/** A selectable pill. Selected chips fill with butter. */
@Composable
fun NeoChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = Neo.palette
    val shape = CircleShape
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink = if (pressed) 2.dp else 0.dp
    val chipFill = if (selected) palette.butter else palette.card
    val textColor = if (chipFill.luminance() > 0.45f) palette.onAccent else palette.ink

    Box(
        modifier = modifier
            .offset(x = sink, y = sink)
            .neoFrame(
                shape = shape,
                fill = chipFill,
                borderColor = palette.ink,
                borderWidth = 2.dp,
                shadowOffset = (if (selected) 3.dp else 2.dp) - sink,
                shadowColor = palette.shadow,
            )
            .clip(shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

/** A tiny, slightly-rotated sticker tag ("new", "top"). */
@Composable
fun NeoTag(
    text: String,
    modifier: Modifier = Modifier,
    fill: Color = Neo.palette.butter,
    rotation: Float = -6f,
) {
    val palette = Neo.palette
    Box(
        modifier = modifier
            .rotate(rotation)
            .neoFrame(RoundedCornerShape(7.dp), fill, palette.ink, 1.5.dp, 2.dp, palette.shadow)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = palette.onAccent)
    }
}

/** Section title in display type, with an optional count and trailing content. */
@Composable
fun NeoSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    meta: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val palette = Neo.palette
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = palette.ink,
            modifier = Modifier.weight(1f),
        )
        if (meta != null) {
            Text(
                text = meta,
                style = MaterialTheme.typography.labelMedium,
                color = palette.muted,
                modifier = Modifier.padding(end = if (trailing != null) 10.dp else 0.dp),
            )
        }
        trailing?.invoke()
    }
}

/**
 * The slogan as a sticker: every word is white fill + thick ink outline +
 * a hard ink shadow. [accentWord] gets painted in cherry red instead.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StickerHeadline(
    text: String,
    modifier: Modifier = Modifier,
    accentWord: String? = null,
    fontSize: TextUnit = 34.sp,
    wordGap: Dp = 10.dp,
    lineGap: Dp = 4.dp,
) {
    val palette = Neo.palette
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { fontSize.toPx() * 0.13f }
    val shadowOffsetPx = with(density) { fontSize.toPx() * 0.05f }
    val words = text.split(" ").filter { it.isNotBlank() }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(wordGap),
        verticalArrangement = Arrangement.spacedBy(lineGap),
    ) {
        words.forEach { word ->
            val accent = accentWord != null &&
                word.trim(',', '.', '!', '?').equals(accentWord, ignoreCase = true)
            StickerWord(
                word = word,
                fontSize = fontSize,
                fill = if (accent) palette.cherry else palette.card,
                outline = palette.ink,
                shadow = palette.shadow,
                strokeWidthPx = strokeWidthPx,
                shadowOffsetPx = shadowOffsetPx,
            )
        }
    }
}

@Composable
private fun StickerWord(
    word: String,
    fontSize: TextUnit,
    fill: Color,
    outline: Color,
    shadow: Color,
    strokeWidthPx: Float,
    shadowOffsetPx: Float,
) {
    val density = LocalDensity.current
    val shadowDp = with(density) { shadowOffsetPx.toDp() }
    val base = TextStyle(
        fontFamily = Anton,
        fontSize = fontSize,
        lineHeight = fontSize * 1.04f,
        letterSpacing = 0.5.sp,
    )

    Box(modifier = Modifier.padding(end = shadowDp, bottom = shadowDp)) {
        // hard drop shadow
        Text(
            text = word.uppercase(),
            style = base.copy(color = shadow),
            modifier = Modifier.offset(x = shadowDp, y = shadowDp),
        )
        // ink outline
        Text(
            text = word.uppercase(),
            style = base.copy(
                color = outline,
                drawStyle = Stroke(width = strokeWidthPx, join = StrokeJoin.Round, cap = StrokeCap.Round),
            ),
        )
        // face
        Text(text = word.uppercase(), style = base.copy(color = fill))
    }
}

/** The wordmark: STIKR flanked by red sparks. */
@Composable
fun LogoRow(modifier: Modifier = Modifier) {
    val palette = Neo.palette
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("\u2726", color = palette.cherry, fontSize = 17.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = "STIKR",
            style = TextStyle(fontFamily = Anton, fontSize = 21.sp, letterSpacing = 4.sp),
            color = palette.ink,
        )
        Spacer(Modifier.width(12.dp))
        Text("\u2726", color = palette.cherry, fontSize = 17.sp)
    }
}

/** An emoji sitting in a framed, slightly-tilted badge. */
@Composable
fun EmojiBadge(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    fill: Color = Neo.palette.cyan,
    rotation: Float = -5f,
) {
    val palette = Neo.palette
    Box(
        modifier = modifier.rotate(rotation),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .neoFrame(CircleShape, fill, palette.ink, 2.dp, 4.dp, palette.shadow),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = (size.value * 0.42f).sp)
        }
    }
}

/** A dialog dressed like everything else: framed card, display title. */
@Composable
fun NeoDialog(
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    body: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    val palette = Neo.palette
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .neoFrame(RoundedCornerShape(18.dp), palette.card, palette.ink, 2.dp, 6.dp, palette.shadow)
                    .padding(20.dp),
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.ink,
                )
                if (body != null) {
                    Spacer(Modifier.height(14.dp))
                    body()
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    content = actions,
                )
            }
        }
    }
}

/** Text input dressed as a framed card. */
@Composable
fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    val palette = Neo.palette
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, color = palette.muted) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = palette.ink,
            unfocusedBorderColor = palette.ink.copy(alpha = 0.5f),
            disabledBorderColor = palette.ink.copy(alpha = 0.25f),
            focusedContainerColor = palette.card,
            unfocusedContainerColor = palette.card,
            disabledContainerColor = palette.creamDim,
            cursorColor = palette.cherry,
            focusedTextColor = palette.ink,
            unfocusedTextColor = palette.ink,
            disabledTextColor = palette.muted,
            focusedLabelColor = palette.muted,
            unfocusedLabelColor = palette.muted,
        ),
    )
}

/** Snackbar as an inverted framed card. */
@Composable
fun NeoSnackbar(data: SnackbarData, modifier: Modifier = Modifier) {
    val palette = Neo.palette
    Box(modifier = modifier.padding(horizontal = 8.dp)) {
        Row(
            modifier = Modifier
                .neoFrame(RoundedCornerShape(14.dp), palette.ink, palette.ink, 2.dp, 4.dp, palette.shadow)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = palette.paper,
            )
        }
    }
}
