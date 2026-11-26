package com.example.ui.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.UiStrings
import com.example.data.model.MealType
import com.example.ui.auth.onGoogleSignInClicked
import com.example.ui.auth.signOut
import com.example.ui.components.ScanResultSheet
import com.example.ui.components.TextFoodDialog
import com.example.ui.screens.FoodCatalogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.viewmodel.KhabarCalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class AppTab(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", Icons.Filled.Home, Icons.Outlined.Home),
    FOODS("foods", Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu),
    PROGRESS("progress", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    PROFILE("profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun KhabarCalApp(
    viewModel: KhabarCalViewModel,
    userEmail: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val lang by viewModel.preferredLang.collectAsStateWithLifecycle()
    val strings = remember(lang) { UiStrings(lang) }
    val snapshot by viewModel.dashboardState.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val coachLoading by viewModel.coachLoading.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var showTextDialog by remember { mutableStateOf(false) }
    var preselectedMealType by remember { mutableStateOf<MealType?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    if (currentTab != AppTab.HOME) {
        BackHandler {
            currentTab = AppTab.HOME
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val bmp = loadBitmapFromUri(context, uri)
                    if (bmp != null) {
                        viewModel.analyzePhotoMeal(bmp, preselectedMealType)
                    } else {
                        snackbarHostState.showSnackbar("Could not read selected image.")
                    }
                }
            }
        }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            val uri = pendingCameraUri
            if (success && uri != null) {
                scope.launch {
                    val bmp = loadBitmapFromUri(context, uri)
                    if (bmp != null) {
                        viewModel.analyzePhotoMeal(bmp, preselectedMealType)
                    } else {
                        snackbarHostState.showSnackbar("Could not read captured photo.")
                    }
                }
            }
        }
    )

    fun launchCameraCapture() {
        val uri = createTempCameraUri(context)
        pendingCameraUri = uri
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                launchCameraCapture()
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(strings.cameraPermissionDenied)
                }
            }
        }
    )

    fun onCameraRequested() {
        preselectedMealType = null
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            launchCameraCapture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun onGalleryRequested() {
        preselectedMealType = null
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onCameraRequested() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_camera_scan")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = strings.cameraScanBtn
                    )
                }
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tabLabel(tab, strings)
                                    )
                                },
                                label = { Text(tabLabel(tab, strings)) },
                                modifier = Modifier.testTag("nav_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tabLabel(tab, strings)
                                    )
                                },
                                label = { Text(tabLabel(tab, strings)) },
                                modifier = Modifier.testTag("rail_${tab.route}")
                            )
                        }
                    }
                }

                when (currentTab) {
                    AppTab.HOME -> HomeScreen(
                        snapshot = snapshot,
                        strings = strings,
                        coachLoading = coachLoading,
                        onToggleLang = { viewModel.toggleLanguage() },
                        onShiftDate = { delta -> viewModel.shiftSelectedDate(delta) },
                        onSelectToday = { viewModel.selectToday() },
                        onCameraClick = { onCameraRequested() },
                        onGalleryClick = { onGalleryRequested() },
                        onTextClick = { slot ->
                            preselectedMealType = slot
                            showTextDialog = true
                        },
                        onQuickAddFood = { item -> viewModel.openQuickCatalogPreview(item) },
                        onAdjustWater = { delta -> viewModel.adjustWater(delta) },
                        onSetWaterExact = { ml ->
                            val delta = ml - snapshot.selectedDayWaterMl
                            if (delta != 0) viewModel.adjustWater(delta)
                        },
                        onRefreshCoachTip = { viewModel.refreshDailyCoachTip() },
                        onDeleteMeal = { mealId -> viewModel.deleteMeal(mealId) },
                        modifier = Modifier.weight(1f)
                    )

                    AppTab.FOODS -> FoodCatalogScreen(
                        strings = strings,
                        onSelectFood = { item -> viewModel.openQuickCatalogPreview(item) },
                        onAskAiForDish = { query -> viewModel.analyzeTextMeal(query) },
                        modifier = Modifier.weight(1f)
                    )

                    AppTab.PROGRESS -> ProgressScreen(
                        snapshot = snapshot,
                        strings = strings,
                        modifier = Modifier.weight(1f)
                    )

                    AppTab.PROFILE -> ProfileScreen(
                        profile = snapshot.profile,
                        userEmail = userEmail,
                        strings = strings,
                        onToggleLang = { viewModel.toggleLanguage() },
                        onSaveGoals = { w, tw, h, a, mode, cal, p, c, f, water ->
                            viewModel.saveGoals(w, tw, h, a, mode, cal, p, c, f, water)
                        },
                        onSignOut = {
                            signOut(credentialManager = credentialManager, scope = scope)
                        },
                        onEnableCloudSync = {
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {},
                                onAuthError = { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                },
                                scope = scope
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showTextDialog) {
        TextFoodDialog(
            strings = strings,
            onAnalyze = { description ->
                showTextDialog = false
                viewModel.analyzeTextMeal(description, preselectedMealType)
            },
            onDismiss = { showTextDialog = false }
        )
    }

    ScanResultSheet(
        scanState = scanState,
        strings = strings,
        onSelectMealType = { viewModel.updatePreviewMealType(it) },
        onSelectPortion = { viewModel.updatePreviewPortion(it) },
        onSelectOil = { viewModel.updatePreviewOil(it) },
        onToggleItem = { viewModel.togglePreviewItem(it) },
        onSave = { viewModel.savePreviewMeal() },
        onDismiss = { viewModel.dismissScanSheet() }
    )
}

private fun tabLabel(tab: AppTab, strings: UiStrings): String = when (tab) {
    AppTab.HOME -> strings.navHome
    AppTab.FOODS -> strings.navFoods
    AppTab.PROGRESS -> strings.navProgress
    AppTab.PROFILE -> strings.navProfile
}

private fun createTempCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File.createTempFile("meal_", ".jpg", dir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (_: Exception) {
            null
        }
    }
