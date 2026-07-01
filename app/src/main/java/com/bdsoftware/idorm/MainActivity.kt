package com.bdsoftware.idorm

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import com.bdsoftware.idorm.core.common.util.LanguageManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bdsoftware.idorm.navigation.rememberAppState
import androidx.compose.material3.ExperimentalMaterial3Api
import com.bdsoftware.idorm.core.common.util.ScreenshotDetector
import com.bdsoftware.idorm.feature.feedback.navigation.navigateToFeedback
import com.bdsoftware.idorm.navigation.AppNavHost
import com.bdsoftware.idorm.navigation.ScreenshotBottomSheet
import com.bdsoftware.idorm.core.designsystem.theme.IDormTheme
import com.bdsoftware.idorm.core.ui.config.MaintenanceDialog
import com.bdsoftware.idorm.core.ui.config.ForceUpdateDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import javax.inject.Inject
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.data.repository.WifiWorkerManager

import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    private val viewModel: MainActivityViewModel by viewModels()
    private var screenshotDetector: ScreenshotDetector? = null

    @Inject
    lateinit var tokenManager: IDormPreferencesDataSource

    @Inject
    lateinit var wifiWorkerManager: WifiWorkerManager

    private var showScreenshotBottomSheet by mutableStateOf(false)
    private var screenshotBitmap by mutableStateOf<Bitmap?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        // Observe state to update both Splash screen and UI
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        // Keep splash screen visible while determining auth state
        splashScreen.setKeepOnScreenCondition {
            uiState is MainActivityUiState.Loading
        }

        setContent {
            val language by tokenManager.appLanguage.collectAsState(initial = "VI")
            val unreadCount by viewModel.unreadCount.collectAsState()
            val unpaidInvoiceCount by viewModel.unpaidInvoiceCount.collectAsState()
            LaunchedEffect(language) {
                val targetCode = if (language.equals("VI", ignoreCase = true)) "vi" else "en"
                LanguageManager.applyLanguage(this@MainActivity, targetCode)
            }
            key(language) {
                IDormTheme {
                    val appState = rememberAppState()

                    when (val state = uiState) {
                        is MainActivityUiState.Loading -> {
                            // Splash screen is still showing, nothing to render
                        }
                        is MainActivityUiState.Maintenance -> {
                            MaintenanceDialog(
                                message = state.message,
                                onRetry = { viewModel.checkAppStatus() }
                            )
                        }
                        is MainActivityUiState.ForceUpdate -> {
                            ForceUpdateDialog(
                                latestVersion = state.latestVersion
                            )
                        }
                        is MainActivityUiState.Success -> {
                            AppNavHost(
                                appState = appState,
                                isLoggedIn = state.isLoggedIn,
                                preferencesDataSource = tokenManager,
                                wifiWorkerManager = wifiWorkerManager,
                                unreadNotificationCount = unreadCount,
                                unpaidInvoiceCount = unpaidInvoiceCount
                            )

                            if (showScreenshotBottomSheet) {
                                Log.d(TAG, "Rendering ScreenshotBottomSheet")
                                ScreenshotBottomSheet(
                                    screenshot = screenshotBitmap,
                                    onDismiss = {
                                        Log.d(TAG, "Dismissing ScreenshotBottomSheet")
                                        showScreenshotBottomSheet = false
                                        screenshotBitmap = null
                                    },
                                    onFeedbackClick = {
                                        Log.d(TAG, "Feedback clicked from bottom sheet")
                                        showScreenshotBottomSheet = false
                                        screenshotBitmap = null
                                        appState.navController.navigateToFeedback()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Initializing ScreenshotDetector")
        screenshotDetector = ScreenshotDetector(this) {
            Log.d(TAG, "onScreenshotTaken callback triggered, capturing window")
            try {
                ScreenshotDetector.captureActivityWindow(this) { bitmap ->
                    Log.d(TAG, "captureActivityWindow callback: bitmap=${if (bitmap != null) "${bitmap.width}x${bitmap.height}" else "null"}")
                    screenshotBitmap = bitmap
                    showScreenshotBottomSheet = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "captureActivityWindow threw exception: ${e.message}", e)
                // Fallback: show bottom sheet without screenshot preview
                screenshotBitmap = null
                showScreenshotBottomSheet = true
            }
        }
        screenshotDetector?.startListening()
    }

    override fun onStop() {
        super.onStop()
        screenshotDetector?.stopListening()
        screenshotDetector = null
    }
}