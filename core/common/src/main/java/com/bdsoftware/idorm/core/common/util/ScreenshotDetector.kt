package com.bdsoftware.idorm.core.common.util

import android.app.Activity
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.Window
import android.view.View
import android.graphics.Canvas
import android.util.Log
import android.content.Context

class ScreenshotDetector(
    private val activity: Activity,
    private val onScreenshotTaken: () -> Unit
) {
    private val TAG = "ScreenshotDetector"
    private var lastTriggerTime = 0L

    // For Android 14+
    private var screenCaptureCallback: Any? = null

    // For Android 10-13
    private var contentObserver: ContentObserver? = null

    private fun triggerCallback() {
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime > 2000L) {
            lastTriggerTime = now
            Log.d(TAG, "Screenshot detected, triggering callback!")
            onScreenshotTaken()
        }
    }

    fun startListening() {
        Log.d(TAG, "startListening: SDK_INT = ${Build.VERSION.SDK_INT}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val callback = Activity.ScreenCaptureCallback {
                Log.d(TAG, "ScreenCaptureCallback triggered")
                triggerCallback()
            }
            screenCaptureCallback = callback
            activity.registerScreenCaptureCallback(activity.mainExecutor, callback)
        } else {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    Log.d(TAG, "ContentObserver onChange: selfChange = $selfChange, uri = $uri")
                    if (uri != null) {
                        val uriString = uri.toString()
                        if (uriString.contains("/images/media") || 
                            uriString.contains("screenshots") || 
                            uriString.contains(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
                        ) {
                            if (isScreenshotUri(activity, uri)) {
                                triggerCallback()
                            }
                        }
                    } else {
                        if (isLatestImageScreenshot(activity)) {
                            triggerCallback()
                        }
                    }
                }
            }
            contentObserver = observer
            activity.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
        }
    }

    fun stopListening() {
        Log.d(TAG, "stopListening")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            screenCaptureCallback?.let {
                activity.unregisterScreenCaptureCallback(it as Activity.ScreenCaptureCallback)
            }
            screenCaptureCallback = null
        } else {
            contentObserver?.let {
                activity.contentResolver.unregisterContentObserver(it)
            }
            contentObserver = null
        }
    }

    private fun isScreenshotUri(context: Context, uri: Uri): Boolean {
        try {
            val projection = arrayOf(
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.ImageColumns.RELATIVE_PATH
                } else {
                    MediaStore.Images.ImageColumns.DATA
                }
            )
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor == null) {
                Log.d(TAG, "isScreenshotUri: query returned null (probably missing permission). Falling back to true.")
                return true
            }
            cursor.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                    val pathIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        c.getColumnIndex(MediaStore.Images.ImageColumns.RELATIVE_PATH)
                    } else {
                        c.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    }
                    
                    val name = if (nameIndex != -1) c.getString(nameIndex) else ""
                    val path = if (pathIndex != -1) c.getString(pathIndex) else ""
                    
                    Log.d(TAG, "isScreenshotUri: name = $name, path = $path")
                    val lowerName = name.lowercase()
                    val lowerPath = path.lowercase()
                    
                    if (lowerName.contains("screenshot") || lowerPath.contains("screenshot") ||
                        lowerName.contains("screencast") || lowerPath.contains("screencast")
                    ) {
                        return true
                    }
                } else {
                    Log.d(TAG, "isScreenshotUri: cursor is empty (saving incomplete). Falling back to true.")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if URI is screenshot: ${e.message}")
            return true // Fallback to true if we cannot query due to permissions
        }
        return false
    }

    private fun isLatestImageScreenshot(context: Context): Boolean {
        try {
            val projection = arrayOf(
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_ADDED,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.ImageColumns.RELATIVE_PATH
                } else {
                    MediaStore.Images.ImageColumns.DATA
                }
            )
            val sortOrder = "${MediaStore.Images.ImageColumns.DATE_ADDED} DESC"
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                    val pathIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cursor.getColumnIndex(MediaStore.Images.ImageColumns.RELATIVE_PATH)
                    } else {
                        cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    }
                    val dateIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED)
                    
                    val name = if (nameIndex != -1) cursor.getString(nameIndex) else ""
                    val path = if (pathIndex != -1) cursor.getString(pathIndex) else ""
                    val dateAdded = if (dateIndex != -1) cursor.getLong(dateIndex) else 0L
                    
                    val nowSec = System.currentTimeMillis() / 1000L
                    Log.d(TAG, "isLatestImageScreenshot: name = $name, path = $path, dateAdded = $dateAdded, nowSec = $nowSec")
                    if (Math.abs(nowSec - dateAdded) < 15L) {
                        val lowerName = name.lowercase()
                        val lowerPath = path.lowercase()
                        if (lowerName.contains("screenshot") || lowerPath.contains("screenshot") ||
                            lowerName.contains("screencast") || lowerPath.contains("screencast")
                        ) {
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking latest image: ${e.message}")
            return true // Fallback to true if we cannot query due to permissions
        }
        return false
    }


    companion object {
        fun captureActivityWindow(activity: Activity, onBitmapCaptured: (Bitmap?) -> Unit) {
            val window = activity.window
            val mainDecorView = window.decorView
            if (mainDecorView.width <= 0 || mainDecorView.height <= 0) {
                Log.e("ScreenshotDetector", "captureActivityWindow: mainDecorView width/height <= 0")
                onBitmapCaptured(null)
                return
            }

            try {
                val bitmap = Bitmap.createBitmap(mainDecorView.width, mainDecorView.height, Bitmap.Config.ARGB_8888)
                
                // Use PixelCopy as the primary method to copy the hardware-accelerated Window buffer
                val locationOfViewInWindow = IntArray(2)
                mainDecorView.getLocationInWindow(locationOfViewInWindow)
                val rect = Rect(
                    locationOfViewInWindow[0],
                    locationOfViewInWindow[1],
                    locationOfViewInWindow[0] + mainDecorView.width,
                    locationOfViewInWindow[1] + mainDecorView.height
                )

                PixelCopy.request(
                    window,
                    rect,
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            Log.d("ScreenshotDetector", "captureActivityWindow: captured successfully via PixelCopy")
                            onBitmapCaptured(bitmap)
                        } else {
                            Log.e("ScreenshotDetector", "PixelCopy failed with code $copyResult. Falling back to draw canvas.")
                            fallbackDrawCanvas(mainDecorView, onBitmapCaptured)
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            } catch (oom: OutOfMemoryError) {
                Log.e("ScreenshotDetector", "OutOfMemoryError in PixelCopy: ${oom.message}", oom)
                onBitmapCaptured(null)
            } catch (e: Exception) {
                Log.e("ScreenshotDetector", "Error in PixelCopy request: ${e.message}. Falling back to draw canvas.", e)
                fallbackDrawCanvas(mainDecorView, onBitmapCaptured)
            }
        }

        private fun fallbackDrawCanvas(mainDecorView: View, onBitmapCaptured: (Bitmap?) -> Unit) {
            try {
                val bitmap = Bitmap.createBitmap(mainDecorView.width, mainDecorView.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                
                try {
                    // Get all window decor views using reflection
                    val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
                    val getInstanceMethod = windowManagerGlobalClass.getMethod("getInstance")
                    val windowManagerInstance = getInstanceMethod.invoke(null)
                    val mViewsField = windowManagerGlobalClass.getDeclaredField("mViews")
                    mViewsField.isAccessible = true
                    val views = mViewsField.get(windowManagerInstance) as List<View>
                    val viewsSnapshot = ArrayList(views)

                    for (view in viewsSnapshot) {
                        if (view.visibility == View.VISIBLE && view.width > 0 && view.height > 0) {
                            val coords = IntArray(2)
                            view.getLocationOnScreen(coords)
                            canvas.save()
                            canvas.translate(coords[0].toFloat(), coords[1].toFloat())
                            view.draw(canvas)
                            canvas.restore()
                        }
                    }
                    Log.d("ScreenshotDetector", "fallbackDrawCanvas: captured successfully via reflection")
                    onBitmapCaptured(bitmap)
                } catch (e: Exception) {
                    Log.e("ScreenshotDetector", "fallbackDrawCanvas: Error in reflection draw loop: ${e.message}", e)
                    try {
                        mainDecorView.draw(canvas)
                        Log.d("ScreenshotDetector", "fallbackDrawCanvas: captured via fallback draw")
                        onBitmapCaptured(bitmap)
                    } catch (ex: Exception) {
                        Log.e("ScreenshotDetector", "fallbackDrawCanvas: Error in fallback mainDecorView draw: ${ex.message}", ex)
                        onBitmapCaptured(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("ScreenshotDetector", "fallbackDrawCanvas: OutOfMemory or creation error: ${e.message}", e)
                onBitmapCaptured(null)
            }
        }
    }
}
