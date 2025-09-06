package cloud.zheteng.androidkeyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    KeyboardSwitcherScreen()
                }
            }
        }
    }
}

/**
 * Composable function to handle the keyboard switching logic.
 */
@Composable
fun KeyboardSwitcherScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                // Get the InputMethodManager system service using a safe cast.
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

                // Use a safe call operator (?.) to check if the service is not null
                // and then attempt to show the keyboard picker.
                val dialogShown = imm?.showInputMethodPicker() ?: false

                // If the dialog wasn't shown for some reason, fall back to the settings page.
                if (dialogShown == false) {
                    try {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        context.startActivity(intent)
                        Toast.makeText(context, "Opening Keyboard Settings...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Could not open keyboard settings.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // The dialog was successfully shown.
                    Toast.makeText(context, "Displaying keyboard selection dialog...", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Switch Keyboard")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KeyboardSwitchTheme {
        KeyboardSwitcherScreen()
    }
}