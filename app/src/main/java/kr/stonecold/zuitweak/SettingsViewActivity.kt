package kr.stonecold.zuitweak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.stonecold.zuitweak.common.*
import kr.stonecold.zuitweak.ui.theme.ZuiTweakTheme

class SettingsViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZuiTweakTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CacheSettingsScreen()
                }
            }
        }
    }

    private fun getAllSettings(): String {
        val stringBuilder = StringBuilder()
        SharedPrefsUtil.init(this)

        if (!SharedPrefsUtil.isInitialized) {
            stringBuilder.append("SharedPrefsUtil has not been initialized")
        } else {
            val allEntries = SharedPrefsUtil.getAllOptions().toSortedMap()
            if (allEntries.isEmpty()) {
                stringBuilder.append("No settings stored.")
            } else {
                for ((key, value) in allEntries) {
                    stringBuilder.append("Key: $key, Value: $value\n")
                }
            }
        }
        return stringBuilder.toString()
    }

    @Composable
    fun CacheSettingsScreen() {
        var settingsText by remember { mutableStateOf("Settings will be displayed here.") }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            settingsText = withContext(Dispatchers.IO) {
                getAllSettings()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = settingsText,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    coroutineScope.launch {
                        settingsText = withContext(Dispatchers.IO) {
                            getAllSettings()
                        }
                    }
                }) {
                    Text(text = "Load Settings")
                }
                Button(onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            SharedPrefsUtil.clearAllOptions()
                        }
                        settingsText = "Settings have been cleared."
                        ActivityCompat.finishAffinity(this@SettingsViewActivity) // 모든 액티비티 종료
                        //System.exit(0) // 프로세스 종료
                    }
                }) {
                    Text(text = "Clear Settings")
                }
            }
        }
    }
}
