package cloud.zheteng.androidkeyboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import cloud.zheteng.androidkeyboard.ui.theme.KeyboardSwitchTheme

/**
 * MainActivity using Jetpack Compose to display a button for switching keyboards.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            KeyboardSwitchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

/**
 * Composable function to handle the keyboard switching logic.
 */
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // State to hold the current permission status
    var hasNotificationPermission by remember {
        mutableStateOf(false)
    }

    // Set the initial permission state based on the Android version and actual check
    LaunchedEffect(Unit) {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            true // Permission is granted by default on older versions.
        }
    }

    // This launcher is only used on API 33+
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = openInputSelect(context),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Switch Keyboard")
        }
        Button(
            onClick = {
                val notifIntent = Intent(context, NotificationService::class.java)
                context.startForegroundService(notifIntent)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Launch Service")
        }

        if (!hasNotificationPermission) {
            Text(text = "Notification permission is required to send alerts.")
            Button(
                onClick = {
                    // Conditional check for Android 13 (Tiramisu) or higher
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                context as ComponentActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        ) {
                            Log.d("ks", "Showing rationale.")
                            // A real app might show a dialog here first.
                        }
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // For API < 33, permission is granted by default
                        hasNotificationPermission = true
                        Log.d("ks", "Permission granted by default on this OS version.")
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Grant Notification Permission")
            }
        } else {
            // Optional: Show a message when permission is granted
            Text(text = "Notifications are enabled.")
        }
    }
}

@Composable
private fun openInputSelect(context: Context): () -> Unit = {
    // Get the InputMethodManager system service using a safe cast.
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

    // Use a safe call operator (?.) to check if the service is not null
    // and then attempt to show the keyboard picker.
    val dialogShown = imm?.showInputMethodPicker() ?: false

    // If the dialog wasn't shown for some reason, fall back to the settings page.
    if (dialogShown == false) {
        try {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            context.startActivity(intent)
            Toast.makeText(context, "Opening Keyboard Settings...", Toast.LENGTH_SHORT)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Could not open keyboard settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    } else {
        // The dialog was successfully shown.
        Toast.makeText(
            context,
            "Displaying keyboard selection dialog...",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KeyboardSwitchTheme {
        MainScreen()
    }
}