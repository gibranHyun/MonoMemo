package com.monomemo.app.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DrawerValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.monomemo.app.R
import com.monomemo.app.ui.components.BrandStripe
import com.monomemo.app.ui.theme.AccentBlue
import com.monomemo.app.ui.theme.AccentLime
import com.monomemo.app.ui.theme.AccentOrange
import com.monomemo.app.ui.theme.AccentPink
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monomemo.app.MonoMemoApp
import com.monomemo.app.ui.about.AboutScreen
import com.monomemo.app.ui.editor.EditorMode
import com.monomemo.app.ui.editor.EditorScreen
import com.monomemo.app.ui.editor.EditorViewModel
import com.monomemo.app.ui.editor.SearchTopBar
import com.monomemo.app.ui.settings.SettingsScreen
import com.monomemo.app.ui.theme.MonoMemoTheme
import com.monomemo.app.ui.trash.TrashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val app = LocalContext.current.applicationContext as MonoMemoApp
    val themeMode by app.settingsDataStore.themeMode.collectAsState(initial = "system")
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    MonoMemoTheme(darkTheme = isDark) {
        AppScaffoldContent(app)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffoldContent(app: MonoMemoApp) {
    val newMemoTitle = stringResource(R.string.new_memo)
    val importedMemoTitle = stringResource(R.string.imported_memo)
    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModel.Factory(app.noteRepository, app.settingsDataStore, newMemoTitle, importedMemoTitle),
    )
    val editorViewModel: EditorViewModel = viewModel(
        factory = EditorViewModel.Factory(app.noteRepository, newMemoTitle),
    )
    val currentNoteId by appViewModel.currentNoteId.collectAsState()
    val activeNotes by appViewModel.activeNotes.collectAsState()
    val currentScreen by appViewModel.currentScreen.collectAsState()
    val trashNotes by appViewModel.trashNotes.collectAsState()
    val title by editorViewModel.title.collectAsState()
    val editorMode by editorViewModel.editorMode.collectAsState()
    val findQuery by editorViewModel.findQuery.collectAsState()
    val findOptions by editorViewModel.findOptions.collectAsState()
    val findResult by editorViewModel.findResult.collectAsState()
    val currentMatchIndex by editorViewModel.currentMatchIndex.collectAsState()
    val replaceQuery by editorViewModel.replaceQuery.collectAsState()
    val showReplace by editorViewModel.showReplace.collectAsState()
    val undoContent by editorViewModel.undoContent.collectAsState()
    val replaceAllCount by editorViewModel.replaceAllCount.collectAsState()
    val canUndo by editorViewModel.canUndo.collectAsState()
    val canRedo by editorViewModel.canRedo.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Export file
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                            stream.write(editorViewModel.content.value.text.toByteArray(Charsets.UTF_8))
                        }
                        true
                    } catch (_: Exception) {
                        false
                    }
                }
                Toast.makeText(
                    context,
                    if (success) context.getString(R.string.file_saved) else context.getString(R.string.save_failed),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    // Import file
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader().readText()
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
                if (text != null) {
                    appViewModel.importNote(text)
                    Toast.makeText(context, context.getString(R.string.file_imported), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.cannot_read_file), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Replace All Undo Snackbar
    LaunchedEffect(undoContent) {
        if (undoContent != null) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.changes_made, replaceAllCount),
                actionLabel = context.getString(R.string.undo_action),
                duration = SnackbarDuration.Long,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> editorViewModel.undoReplaceAll()
                SnackbarResult.Dismissed -> editorViewModel.dismissUndo()
            }
        }
    }

    when (currentScreen) {
        AppScreen.Editor -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        DrawerContent(
                            notes = activeNotes,
                            currentNoteId = currentNoteId,
                            onNewNote = { appViewModel.createNewNote() },
                            onNoteSelect = { appViewModel.openNote(it) },
                            onTrashClick = { appViewModel.navigateTo(AppScreen.Trash) },
                            onSettingsClick = { appViewModel.navigateTo(AppScreen.Settings) },
                            onCloseDrawer = {
                                scope.launch { drawerState.close() }
                            },
                        )
                    }
                },
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        when (editorMode) {
                            EditorMode.Normal -> {
                                Column {
                                    BrandStripe()
                                    TopAppBar(
                                    title = {
                                        BasicTextField(
                                            value = title,
                                            onValueChange = { editorViewModel.onTitleChange(it) },
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                            decorationBox = { innerTextField ->
                                                Box {
                                                    if (title.isEmpty()) {
                                                        Text(
                                                            stringResource(R.string.untitled),
                                                            style = MaterialTheme.typography.titleMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            },
                                        )
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch { drawerState.open() }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = stringResource(R.string.open_menu),
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = { editorViewModel.undo() },
                                            enabled = canUndo,
                                        ) {
                                            val undoColor = if (canUndo) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.outline
                                            Canvas(modifier = Modifier.size(24.dp)) {
                                                val w = size.width
                                                val h = size.height
                                                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = 2.5.dp.toPx(),
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                )
                                                // Arc (counter-clockwise arrow body)
                                                drawArc(
                                                    color = undoColor,
                                                    startAngle = -180f,
                                                    sweepAngle = 250f,
                                                    useCenter = false,
                                                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.2f),
                                                    size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.7f),
                                                    style = stroke,
                                                )
                                                // Arrowhead
                                                val path = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(w * 0.15f, h * 0.25f)
                                                    lineTo(w * 0.15f, h * 0.58f)
                                                    lineTo(w * 0.42f, h * 0.45f)
                                                    close()
                                                }
                                                drawPath(path, color = undoColor)
                                            }
                                        }
                                        IconButton(
                                            onClick = { editorViewModel.redo() },
                                            enabled = canRedo,
                                        ) {
                                            val redoColor = if (canRedo) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.outline
                                            Canvas(modifier = Modifier.size(24.dp)) {
                                                val w = size.width
                                                val h = size.height
                                                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = 2.5.dp.toPx(),
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                )
                                                // Arc (clockwise arrow body)
                                                drawArc(
                                                    color = redoColor,
                                                    startAngle = -70f,
                                                    sweepAngle = 250f,
                                                    useCenter = false,
                                                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.2f),
                                                    size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.7f),
                                                    style = stroke,
                                                )
                                                // Arrowhead
                                                val path = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(w * 0.85f, h * 0.25f)
                                                    lineTo(w * 0.85f, h * 0.58f)
                                                    lineTo(w * 0.58f, h * 0.45f)
                                                    close()
                                                }
                                                drawPath(path, color = redoColor)
                                            }
                                        }
                                        IconButton(onClick = { editorViewModel.enterSearch() }) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = stringResource(R.string.search),
                                            )
                                        }
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = stringResource(R.string.more_options),
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                        ) {
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(Icons.Default.Share, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                                                },
                                                text = { Text(stringResource(R.string.share)) },
                                                onClick = {
                                                    showMenu = false
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_SUBJECT, title.ifEmpty { "MonoMemo" })
                                                        putExtra(Intent.EXTRA_TEXT, editorViewModel.content.value.text)
                                                    }
                                                    context.startActivity(
                                                        Intent.createChooser(shareIntent, null),
                                                    )
                                                },
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Canvas(modifier = Modifier.size(8.dp)) { drawCircle(AccentOrange) }
                                                },
                                                text = { Text(stringResource(R.string.save_as_file)) },
                                                onClick = {
                                                    showMenu = false
                                                    val fileName = title.ifEmpty { context.getString(R.string.memo_default_name) }
                                                    exportLauncher.launch("$fileName.txt")
                                                },
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Canvas(modifier = Modifier.size(8.dp)) { drawCircle(AccentLime) }
                                                },
                                                text = { Text(stringResource(R.string.open_file)) },
                                                onClick = {
                                                    showMenu = false
                                                    importLauncher.launch(arrayOf("text/plain", "text/*"))
                                                },
                                            )
                                            HorizontalDivider()
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(Icons.Default.Delete, null, tint = AccentPink, modifier = Modifier.size(18.dp))
                                                },
                                                text = { Text(stringResource(R.string.delete), color = AccentPink) },
                                                onClick = {
                                                    showMenu = false
                                                    currentNoteId?.let {
                                                        appViewModel.deleteNote(it)
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = context.getString(R.string.moved_to_trash),
                                                                duration = SnackbarDuration.Short,
                                                            )
                                                        }
                                                    }
                                                },
                                            )
                                            HorizontalDivider()
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Icon(Icons.Default.Info, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                                                },
                                                text = { Text(stringResource(R.string.about_license)) },
                                                onClick = {
                                                    showMenu = false
                                                    appViewModel.navigateTo(AppScreen.About)
                                                },
                                            )
                                        }
                                    },
                                )
                                }
                            }

                            EditorMode.Search -> {
                                SearchTopBar(
                                    findQuery = findQuery,
                                    onFindQueryChange = { editorViewModel.onFindQueryChange(it) },
                                    replaceQuery = replaceQuery,
                                    onReplaceQueryChange = { editorViewModel.onReplaceQueryChange(it) },
                                    matchCount = findResult.matches.size,
                                    currentIndex = currentMatchIndex,
                                    limitReached = findResult.limitReached,
                                    caseSensitive = findOptions.caseSensitive,
                                    wholeWord = findOptions.wholeWord,
                                    showReplace = showReplace,
                                    onToggleCaseSensitive = { editorViewModel.toggleCaseSensitive() },
                                    onToggleWholeWord = { editorViewModel.toggleWholeWord() },
                                    onToggleShowReplace = { editorViewModel.toggleShowReplace() },
                                    onPrev = { editorViewModel.prevMatch() },
                                    onNext = { editorViewModel.nextMatch() },
                                    onReplaceOne = { editorViewModel.replaceOne() },
                                    onReplaceAll = { editorViewModel.replaceAll() },
                                    onClose = { editorViewModel.exitSearch() },
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    EditorScreen(
                        noteId = currentNoteId,
                        viewModel = editorViewModel,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }

        AppScreen.Trash -> {
            TrashScreen(
                trashNotes = trashNotes,
                onBack = { appViewModel.navigateTo(AppScreen.Editor) },
                onRestore = { appViewModel.restoreNote(it) },
                onDeletePermanently = { appViewModel.deletePermanently(it) },
                onEmptyAll = { appViewModel.emptyTrash() },
            )
        }

        AppScreen.Settings -> {
            SettingsScreen(
                settingsDataStore = app.settingsDataStore,
                onBack = { appViewModel.navigateTo(AppScreen.Editor) },
            )
        }

        AppScreen.About -> {
            AboutScreen(
                onBack = { appViewModel.navigateTo(AppScreen.Editor) },
            )
        }
    }
}
