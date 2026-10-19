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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.stikr.app.data.PackRepository
import com.stikr.app.data.StickerPack
import com.stikr.app.export.PackExporter
import com.stikr.app.ui.neo.Neo
import com.stikr.app.ui.neo.NeoButton
import com.stikr.app.ui.neo.NeoCard
import com.stikr.app.ui.neo.NeoDialog
import com.stikr.app.ui.neo.NeoIconButton
import com.stikr.app.ui.neo.NeoOutlinedButton
import com.stikr.app.ui.neo.NeoSectionHeader
import com.stikr.app.ui.neo.NeoTag
import java.io.File

@Composable
fun PackScreen(
    pack: StickerPack,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onAddSticker: () -> Unit,
    onDeleteSticker: (String) -> Unit,
    onShareSticker: (String) -> Unit,
    onExport: () -> Unit,
) {
    val palette = Neo.palette
    val context = LocalContext.current
    val root = remember(context) { File(context.filesDir, "packs") }
    var showRename by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    val whatsAppInstalled = remember { PackExporter.isWhatsAppInstalled(context) }
    val missing = (PackRepository.MIN_WHATSAPP_STICKERS - pack.stickerCount).coerceAtLeast(0)

    Scaffold(containerColor = palette.paper) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 24.dp, top = 8.dp, bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TopBar(
                    pack = pack,
                    onBack = onBack,
                    onRename = { showRename = true },
                    onDelete = { showDelete = true },
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ExportCard(
                    whatsAppInstalled = whatsAppInstalled,
                    missing = missing,
                    exportable = whatsAppInstalled && missing == 0,
                    onExport = onExport,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                NeoSectionHeader(
                    title = "Stickers",
                    meta = if (pack.stickerCount == 1) "1" else "${pack.stickerCount}",
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            items(pack.stickers, key = { it }) { fileName ->
                StickerTile(
                    file = root.resolve(pack.id).resolve(fileName),
                    onShare = { onShareSticker(fileName) },
                    onDelete = { onDeleteSticker(fileName) },
                )
            }
            item {
                AddTile(onClick = onAddSticker)
            }
        }
    }

    if (showRename) {
        NameDialog(
            title = "Rename pack",
            confirmLabel = "Save",
            initial = pack.name,
            onDismiss = { showRename = false },
            onConfirm = { name ->
                showRename = false
                onRename(name)
            },
        )
    }

    if (showDelete) {
        NeoDialog(
            onDismiss = { showDelete = false },
            title = "Delete this pack?",
            body = {
                Text(
                    text = "\u201C${pack.name}\u201D and its ${pack.stickerCount} " +
                        "${if (pack.stickerCount == 1) "sticker" else "stickers"} will be removed from stikr. " +
                        "Copies already imported into WhatsApp are not affected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            },
            actions = {
                NeoOutlinedButton(
                    text = "Cancel",
                    onClick = { showDelete = false },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp),
                )
                NeoButton(
                    text = "Delete",
                    onClick = {
                        showDelete = false
                        onDelete()
                    },
                    modifier = Modifier.weight(1f),
                    fill = palette.cherry,
                    contentColor = Color.White,
                    contentPadding = PaddingValues(vertical = 12.dp),
                )
            },
        )
    }
}

@Composable
private fun TopBar(
    pack: StickerPack,
    onBack: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
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
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = pack.name.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (pack.stickerCount == 1) "1 sticker" else "${pack.stickerCount} stickers",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
        NeoIconButton(
            onClick = onRename,
            icon = Icons.Default.Edit,
            contentDescription = "Rename pack",
        )
        Spacer(Modifier.size(10.dp))
        NeoIconButton(
            onClick = onDelete,
            icon = Icons.Default.Delete,
            contentDescription = "Delete pack",
            fill = palette.pink,
        )
    }
}

@Composable
private fun ExportCard(
    whatsAppInstalled: Boolean,
    missing: Int,
    exportable: Boolean,
    onExport: () -> Unit,
) {
    val palette = Neo.palette
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        fill = if (whatsAppInstalled) palette.green else palette.cyan,
        padding = PaddingValues(16.dp),
    ) {
        NeoTag(if (whatsAppInstalled) "whatsapp" else "coming up", rotation = -5f)
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (whatsAppInstalled) "Add this pack to WhatsApp" else "Share a sticker",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp, lineHeight = 23.sp),
            color = palette.onAccent,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = when {
                !whatsAppInstalled -> "WhatsApp isn't installed on this phone. You can still share single stickers from the grid below."
                missing > 0 -> "Add $missing more ${if (missing == 1) "sticker" else "stickers"} \u2014 WhatsApp needs ${PackRepository.MIN_WHATSAPP_STICKERS} per pack."
                else -> "Import this pack into your WhatsApp sticker tray."
            },
            style = MaterialTheme.typography.bodySmall,
            color = palette.onAccent.copy(alpha = 0.68f),
        )
        Spacer(Modifier.height(14.dp))
        NeoButton(
            text = if (whatsAppInstalled) "Add to WhatsApp" else "WhatsApp not found",
            onClick = onExport,
            enabled = exportable,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StickerTile(file: File, onShare: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        NeoCard(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(14.dp),
            shadow = 3.dp,
            padding = PaddingValues(8.dp),
        ) {
            StickerImage(
                file = file,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            NeoIconButton(
                onClick = { menuOpen = true },
                icon = Icons.Default.MoreVert,
                contentDescription = "Sticker options",
                size = 28.dp,
                iconSize = 15.dp,
                shadow = 2.dp,
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Share") },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onShare()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}

@Composable
private fun AddTile(onClick: () -> Unit) {
    val palette = Neo.palette
    NeoCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        fill = palette.butter,
        shape = RoundedCornerShape(14.dp),
        shadow = 3.dp,
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add sticker",
                    tint = palette.onAccent,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.onAccent,
                )
            }
        }
    }
}
