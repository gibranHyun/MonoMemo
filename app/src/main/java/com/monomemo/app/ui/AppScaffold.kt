package com.monomemo.app.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.unit.sp
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
    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModel.Factory(app.noteRepository, app.settingsDataStore),
    )
    val editorViewModel: EditorViewModel = viewModel(
        factory = EditorViewModel.Factory(app.noteRepository),
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
                    if (success) "파일이 저장되었습니다" else "저장에 실패했습니다",
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
                    Toast.makeText(context, "파일을 가져왔습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "파일을 읽을 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Replace All Undo Snackbar
    LaunchedEffect(undoContent) {
        if (undoContent != null) {
            val result = snackbarHostState.showSnackbar(
                message = "${replaceAllCount}건 변경됨",
                actionLabel = "되돌리기",
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
                                        Text(
                                            text = title.ifEmpty { "MonoMemo" },
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
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
                                                contentDescription = "메뉴 열기",
                                            )
                                        }
                                    },
                                    actions = {
                                        TextButton(
                                            onClick = { editorViewModel.undo() },
                                            enabled = canUndo,
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    RoundedCornerShape(6.dp),
                                                ),
                                        ) {
                                            Text(
                                                "↩",
                                                fontSize = 40.sp,
                                                color = if (canUndo) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.outline,
                                            )
                                        }
                                        TextButton(
                                            onClick = { editorViewModel.redo() },
                                            enabled = canRedo,
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    RoundedCornerShape(6.dp),
                                                ),
                                        ) {
                                            Text(
                                                "↪",
                                                fontSize = 40.sp,
                                                color = if (canRedo) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.outline,
                                            )
                                        }
                                        IconButton(onClick = { editorViewModel.enterSearch() }) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "검색",
                                            )
                                        }
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "더보기",
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
                                                text = { Text("공유") },
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
                                                text = { Text("파일로 저장") },
                                                onClick = {
                                                    showMenu = false
                                                    val fileName = title.ifEmpty { "메모" }
                                                    exportLauncher.launch("$fileName.txt")
                                                },
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = {
                                                    Canvas(modifier = Modifier.size(8.dp)) { drawCircle(AccentLime) }
                                                },
                                                text = { Text("파일 열기") },
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
                                                text = { Text("삭제", color = AccentPink) },
                                                onClick = {
                                                    showMenu = false
                                                    currentNoteId?.let {
                                                        appViewModel.deleteNote(it)
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "휴지통으로 이동되었습니다",
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
                                                text = { Text("정보/라이선스") },
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
