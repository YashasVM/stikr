package com.stikr.app.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stikr.app.data.StickerPack
import com.stikr.app.ui.neo.NeoWordmark
import com.stikr.app.ui.neo.Neo
import com.stikr.app.ui.neo.NeoButton
import com.stikr.app.ui.neo.NeoCard
import com.stikr.app.ui.neo.NeoDialog
import com.stikr.app.ui.neo.NeoIconButton
import com.stikr.app.ui.neo.NeoOutlinedButton
import com.stikr.app.ui.neo.NeoSectionHeader
import com.stikr.app.ui.neo.NeoTag
import com.stikr.app.ui.neo.NeoTextField
import com.stikr.app.ui.neo.StickerHeadline
import com.stikr.app.ui.neo.neoFrame
import java.io.File

@Composable
fun HomeScreen(
    packs: List<StickerPack>,
    onOpenPack: (String) -> Unit,
    onCreatePack: (String) -> Unit,
    onMakeSticker: () -> Unit,
) {
    val palette = Neo.palette
    var showCreate by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = palette.paper,
        floatingActionButton = {
            if (packs.isNotEmpty()) {
                NeoIconButton(
                    onClick = { showCreate = true },
                    icon = Icons.Default.Add,
                    contentDescription = "New pack",
                    fill = palette.butter,
                    size = 58.dp,
                    iconSize = 26.dp,
                    shape = RoundedCornerShape(17.dp),
                    shadow = 5.dp,
                )
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 10.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                NeoWordmark(modifier = Modifier.padding(bottom = 6.dp))
            }

            if (packs.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyHero(
                        onMakeSticker = onMakeSticker,
                        onCreatePack = { showCreate = true },
                    )
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    NeoSectionHeader(
                        title = "Your packs",
                        meta = if (packs.size == 1) "1 pack" else "${packs.size} packs",
                    )
                }
                itemsIndexed(packs, key = { _, pack -> pack.id }) { index, pack ->
                    PackCard(
                        pack = pack,
                        fill = packFill(index),
                        showNew = index == 0 && packs.size > 1,
                        onClick = { onOpenPack(pack.id) },
                    )
                }
            }
        }
    }

    if (showCreate) {
        NameDialog(
            title = "New pack",
            confirmLabel = "Create",
            initial = "",
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                showCreate = false
                onCreatePack(name)
            },
        )
    }
}

/** Packs cycle through the pastel card colours. */
@Composable
private fun packFill(index: Int): Color {
    val palette = Neo.palette
    val fills = listOf(palette.cyan, palette.green, palette.butter, palette.pink)
    return fills[index % fills.size]
}

@Composable
private fun EmptyHero(onMakeSticker: () -> Unit, onCreatePack: () -> Unit) {
    val palette = Neo.palette
    Column(modifier = Modifier.fillMaxWidth()) {
        StickerHeadline(
            text = "Turn any photo into a sticker",
            accentWord = "sticker",
            fontSize = 36.sp,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Welcome to stikr \u2014 the sticker maker that lives on your phone. " +
                "Cut out the subject, add an outline and a caption, keep the good ones. " +
                "Nothing uploaded, ever.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
        )
        Spacer(Modifier.height(24.dp))
        Spacer(Modifier.height(26.dp))
        NeoButton(
            text = "Make a sticker",
            onClick = onMakeSticker,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        NeoOutlinedButton(
            text = "Create a pack",
            onClick = onCreatePack,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PackCard(
    pack: StickerPack,
    fill: Color,
    showNew: Boolean,
    onClick: () -> Unit,
) {
    val palette = Neo.palette
    val context = LocalContext.current
    val root = remember(context) { File(context.filesDir, "packs") }

    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        fill = fill,
        padding = PaddingValues(12.dp),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pack.name.uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp, lineHeight = 18.sp),
                color = palette.onAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (showNew) {
                NeoTag("new", rotation = 7f)
            }
        }
        Text(
            text = if (pack.stickerCount == 1) "1 sticker" else "${pack.stickerCount} stickers",
            style = MaterialTheme.typography.bodySmall,
            color = palette.onAccent.copy(alpha = 0.62f),
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .neoFrame(fill = palette.card, borderColor = palette.ink, shadowColor = palette.shadow, shadowOffset = 0.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            val first = pack.stickers.firstOrNull()
            if (first != null) {
                StickerImage(
                    file = root.resolve(pack.id).resolve(first),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(text = "\u2728", fontSize = 26.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        NeoButton(
            text = "Open",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shadow = 3.dp,
            contentPadding = PaddingValues(vertical = 9.dp),
        )
    }
}

/** One dialog for both "New pack" and "Rename pack". */
@Composable
fun NameDialog(
    title: String,
    confirmLabel: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial) }
    val palette = Neo.palette

    NeoDialog(
        onDismiss = onDismiss,
        title = title,
        body = {
            NeoTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
            )
        },
        actions = {
            NeoOutlinedButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
            )
            NeoButton(
                text = confirmLabel,
                onClick = { onConfirm(name.trim().ifEmpty { "Untitled pack" }) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
            )
        },
    )
}
