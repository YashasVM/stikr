package com.stikr.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stikr.app.render.StickerRenderer
import com.stikr.app.ui.neo.Neo
import com.stikr.app.ui.neo.NeoButton
import com.stikr.app.ui.neo.NeoCard
import com.stikr.app.ui.neo.NeoChip
import com.stikr.app.ui.neo.NeoIconButton
import com.stikr.app.ui.neo.NeoOutlinedButton
import com.stikr.app.ui.neo.NeoTextField
import kotlin.math.roundToInt

@Composable
fun EditorScreen(
    state: EditorState,
    packName: String,
    onPick: (Uri) -> Unit,
    onCutout: (Boolean) -> Unit,
    onStyle: (StickerRenderer.Style) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    val palette = Neo.palette
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onPick)
    }
    var pickerOpened by rememberSaveable { mutableStateOf(false) }
    val pickPhoto = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    LaunchedEffect(Unit) {
        if (state.source == null && !pickerOpened) {
            pickerOpened = true
            pickPhoto()
        }
    }

    Scaffold(
        containerColor = palette.paper,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                NeoButton(
                    text = when {
                        state.saving -> "Saving\u2026"
                        packName.isNotEmpty() -> "Add to \u201C$packName\u201D"
                        else -> "Add to pack"
                    },
                    onClick = onSave,
                    enabled = state.ready,
                    modifier = Modifier.fillMaxWidth(),
                    shadow = 5.dp,
                    contentPadding = PaddingValues(vertical = 15.dp),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            TopBar(packName = packName, onBack = onBack)
            Spacer(Modifier.height(16.dp))

            PreviewCard(state = state, onPickPhoto = pickPhoto)
            Spacer(Modifier.height(18.dp))

            CutoutCard(state = state, onCutout = onCutout)
            Spacer(Modifier.height(14.dp))

            OutlineCard(state = state, onStyle = onStyle)
            Spacer(Modifier.height(14.dp))

            CaptionCard(state = state, onStyle = onStyle)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TopBar(packName: String, onBack: () -> Unit) {
    val palette = Neo.palette
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeoIconButton(
            onClick = onBack,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
        )
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(
                text = "New sticker",
                style = MaterialTheme.typography.titleLarge,
                color = palette.ink,
            )
            Text(
                text = if (packName.isNotEmpty()) "into \u201C$packName\u201D" else "pick a photo to start",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
    }
}

@Composable
private fun PreviewCard(state: EditorState, onPickPhoto: () -> Unit) {
    val palette = Neo.palette
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        shadow = 6.dp,
        padding = PaddingValues(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CheckerboardBackground(Modifier.fillMaxSize())

            state.preview?.let { preview ->
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "Sticker preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            if (state.source == null && !state.loading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No photo yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.ink,
                    )
                    Spacer(Modifier.height(10.dp))
                    NeoOutlinedButton(
                        text = "Pick a photo",
                        onClick = onPickPhoto,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                    )
                }
            }

            if (state.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(palette.ink.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = palette.card, strokeWidth = 3.dp)
                        state.status?.let { status ->
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = status,
                                color = palette.card,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CutoutCard(state: EditorState, onCutout: (Boolean) -> Unit) {
    val palette = Neo.palette
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto cut-out",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.ink,
                )
                Text(
                    text = if (state.cutoutFailed) {
                        "Not available on this phone \u2014 using the full photo"
                    } else {
                        "Removes the background, on device"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = state.cutoutEnabled,
                onCheckedChange = onCutout,
                enabled = state.source != null && !state.loading,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = palette.butter,
                    checkedTrackColor = palette.cherry,
                    checkedBorderColor = palette.ink,
                    uncheckedThumbColor = palette.card,
                    uncheckedTrackColor = palette.creamDim,
                    uncheckedBorderColor = palette.ink,
                ),
            )
        }
    }
}

@Composable
private fun OutlineCard(state: EditorState, onStyle: (StickerRenderer.Style) -> Unit) {
    val palette = Neo.palette
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Outline",
                style = MaterialTheme.typography.titleMedium,
                color = palette.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${state.style.outline}",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp),
                color = palette.ink,
            )
        }
        Slider(
            value = state.style.outline.toFloat(),
            onValueChange = { value -> onStyle(state.style.copy(outline = value.roundToInt())) },
            valueRange = 0f..StickerRenderer.MAX_OUTLINE.toFloat(),
            enabled = state.source != null,
            colors = SliderDefaults.colors(
                thumbColor = palette.cherry,
                activeTrackColor = palette.ink,
                inactiveTrackColor = palette.ink.copy(alpha = 0.18f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
                disabledThumbColor = palette.muted,
                disabledActiveTrackColor = palette.ink.copy(alpha = 0.3f),
                disabledInactiveTrackColor = palette.ink.copy(alpha = 0.1f),
            ),
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Color",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
                modifier = Modifier.weight(1f),
            )
            StickerRenderer.OutlineColor.entries.forEach { outline ->
                ColorDot(
                    fill = Color(outline.color),
                    selected = state.style.outlineColor == outline,
                    onClick = { onStyle(state.style.copy(outlineColor = outline)) },
                )
            }
        }
    }
}

@Composable
private fun CaptionCard(state: EditorState, onStyle: (StickerRenderer.Style) -> Unit) {
    val palette = Neo.palette
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
    ) {
        Text(
            text = "Caption",
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink,
        )
        Spacer(Modifier.height(10.dp))
        NeoTextField(
            value = state.style.caption,
            onValueChange = { value -> onStyle(state.style.copy(caption = value)) },
            label = "Caption (optional)",
            enabled = state.source != null,
        )

        if (state.style.caption.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                    modifier = Modifier.weight(1f),
                )
                StickerRenderer.CaptionColor.entries.forEach { color ->
                    ColorDot(
                        fill = Color(color.fill),
                        selected = state.style.captionColor == color,
                        onClick = { onStyle(state.style.copy(captionColor = color)) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Position",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                    modifier = Modifier.weight(1f),
                )
                NeoChip(
                    text = "Top",
                    selected = state.style.captionPosition == StickerRenderer.CaptionPosition.TOP,
                    onClick = {
                        onStyle(state.style.copy(captionPosition = StickerRenderer.CaptionPosition.TOP))
                    },
                )
                Spacer(Modifier.width(10.dp))
                NeoChip(
                    text = "Bottom",
                    selected = state.style.captionPosition == StickerRenderer.CaptionPosition.BOTTOM,
                    onClick = {
                        onStyle(state.style.copy(captionPosition = StickerRenderer.CaptionPosition.BOTTOM))
                    },
                )
            }
        }
    }
}

/** A colour swatch; the selected one is ringed and checked. */
@Composable
private fun ColorDot(
    fill: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = Neo.palette
    Box(
        modifier = Modifier
            .padding(start = 10.dp)
            .size(34.dp)
            .clip(CircleShape)
            .background(fill)
            .border(
                width = if (selected) 3.dp else 2.dp,
                color = if (selected) palette.ink else palette.ink.copy(alpha = 0.3f),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = if (fill.luminance() > 0.5f) palette.ink else Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** The see-through checkerboard behind the preview, so transparency reads clearly. */
@Composable
fun CheckerboardBackground(modifier: Modifier = Modifier) {
    val palette = Neo.palette
    val even = palette.card
    val odd = palette.creamDim

    Canvas(modifier = modifier) {
        val cell = size.minDimension / 12f
        if (cell <= 0f) return@Canvas
        var y = 0f
        var row = 0
        while (y < size.height) {
            var x = 0f
            var column = row
            while (x < size.width) {
                drawRect(
                    color = if (column % 2 == 0) even else odd,
                    topLeft = Offset(x, y),
                    size = Size(cell, cell),
                )
                x += cell
                column++
            }
            y += cell
            row++
        }
    }
}
