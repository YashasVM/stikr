package com.stikr.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stikr.app.ui.neo.Neo
import com.stikr.app.ui.neo.NeoSnackbar

private enum class Screen { HOME, PACK, EDITOR }

@Composable
fun StikrApp(viewModel: MainViewModel = viewModel()) {
    val packs by viewModel.packs.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var screen by rememberSaveable { mutableStateOf(Screen.HOME.name) }
    var packId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(message) {
        message?.let { text ->
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    fun openPack(id: String) {
        packId = id
        screen = Screen.PACK.name
    }

    fun openEditor(id: String) {
        packId = id
        viewModel.startEditor(id)
        screen = Screen.EDITOR.name
    }

    fun leaveEditor() {
        viewModel.closeEditor()
        screen = Screen.PACK.name
    }

    fun makeSticker() {
        val first = packs.firstOrNull()
        if (first != null) {
            openEditor(first.id)
        } else {
            viewModel.createPack("My stickers") { openEditor(it.id) }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Neo.palette.paper)) {
        when (screen) {
            Screen.HOME.name -> HomeScreen(
                packs = packs,
                onOpenPack = ::openPack,
                onCreatePack = { name -> viewModel.createPack(name) { openPack(it.id) } },
                onMakeSticker = ::makeSticker,
            )

            Screen.PACK.name -> {
                val pack = packs.firstOrNull { it.id == packId }
                if (pack == null) {
                    LaunchedEffect(packId) { screen = Screen.HOME.name }
                } else {
                    PackScreen(
                        pack = pack,
                        onBack = { screen = Screen.HOME.name },
                        onRename = { viewModel.renamePack(pack.id, it) },
                        onDelete = { viewModel.deletePack(pack.id) { screen = Screen.HOME.name } },
                        onAddSticker = { openEditor(pack.id) },
                        onDeleteSticker = { viewModel.deleteSticker(pack.id, it) },
                        onShareSticker = { viewModel.shareSticker(pack.id, it) },
                        onExport = { viewModel.exportToWhatsApp(pack.id) },
                    )
                }
            }

            Screen.EDITOR.name -> {
                val state = editor
                if (state == null) {
                    LaunchedEffect(Unit) { screen = Screen.PACK.name }
                } else {
                    EditorScreen(
                        state = state,
                        packName = packs.firstOrNull { it.id == state.packId }?.name.orEmpty(),
                        onPick = viewModel::loadImage,
                        onCutout = viewModel::setCutout,
                        onStyle = viewModel::setStyle,
                        onSave = { viewModel.saveSticker { leaveEditor() } },
                        onBack = { leaveEditor() },
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) { data ->
            NeoSnackbar(data)
        }
    }

    BackHandler(enabled = screen != Screen.HOME.name) {
        if (screen == Screen.EDITOR.name) leaveEditor() else screen = Screen.HOME.name
    }
}
