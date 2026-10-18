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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stikr.app.render.StickerRenderer
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
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
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onPick)
    }
    var pickerOpened by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (state.source == null && !pickerOpened) {
            pickerOpened = true
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New sticker") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = onSave,
                    enabled = state.ready,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        when {
                            state.saving -> "Saving\u2026"
                            packName.isNotEmpty() -> "Add to \"$packName\""
                            else -> "Add to pack"
                        },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp)),
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

                if (state.source == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No photo yet", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        }) {
                            Text("Pick a photo")
                        }
                    }
                }

                if (state.loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x66000000)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            state.status?.let { status ->
                                Text(
                                    text = status,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 12.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto cut-out", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = if (state.cutoutFailed) {
                            "Not available on this phone \u2014 using the full photo"
                        } else {
                            "Removes the background, on device"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.cutoutEnabled,
                    onCheckedChange = onCutout,
                    enabled = state.source != null && !state.loading,
                )
            }

            Spacer(Modifier.height(12.dp))

            Text("Outline", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = state.style.outline.toFloat(),
                    onValueChange = { value -> onStyle(state.style.copy(outline = value.roundToInt())) },
                    valueRange = 0f..StickerRenderer.MAX_OUTLINE.toFloat(),
                    modifier = Modifier.weight(1f),
                    enabled = state.source != null,
                )
                Text(
                    text = "${state.style.outline}",
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.End,
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.style.caption,
                onValueChange = { value -> onStyle(state.style.copy(caption = value)) },
                singleLine = true,
                label = { Text("Caption (optional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.source != null,
            )

            if (state.style.caption.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StickerRenderer.CaptionColor.entries.forEach { color ->
                        CaptionColorDot(
                            color = color,
                            selected = state.style.captionColor == color,
                            onClick = { onStyle(state.style.copy(captionColor = color)) },
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    FilterChip(
                        selected = state.style.captionPosition == StickerRenderer.CaptionPosition.TOP,
                        onClick = { onStyle(state.style.copy(captionPosition = StickerRenderer.CaptionPosition.TOP)) },
                        label = { Text("Top") },
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = state.style.captionPosition == StickerRenderer.CaptionPosition.BOTTOM,
                        onClick = { onStyle(state.style.copy(captionPosition = StickerRenderer.CaptionPosition.BOTTOM)) },
                        label = { Text("Bottom") },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CaptionColorDot(
    color: StickerRenderer.CaptionColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(color.fill))
            .border(
                width = 2.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color(color.stroke).copy(alpha = 0.4f),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color(color.stroke),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** The see-through checkerboard behind the preview, so transparency reads clearly. */
@Composable
fun CheckerboardBackground(modifier: Modifier = Modifier) {
    val even = MaterialTheme.colorScheme.surfaceContainerHigh
    val odd = MaterialTheme.colorScheme.surfaceContainer

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
