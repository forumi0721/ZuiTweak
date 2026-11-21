package kr.stonecold.zuitweak

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.stonecold.zuitweak.ui.theme.ZuiTweakTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.core.content.ContextCompat.getString
import androidx.activity.OnBackPressedCallback
import com.topjohnwu.superuser.Shell
import kr.stonecold.zuitweak.common.*
import java.util.Locale

private lateinit var prefsSnapshot: Map<String, *>

class SettingsActivity : ComponentActivity() {
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeLanguage(this)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val now = System.currentTimeMillis()
                if (backPressedTime + 2000 > now) {
                    finish()
                } else {
                    Toast.makeText(this@SettingsActivity, getString(R.string.back_press_exit), Toast.LENGTH_SHORT).show()
                }
                backPressedTime = now
            }
        })

        SharedPrefsUtil.init()
        if (SharedPrefsUtil.isInitialized) {
            prefsSnapshot = SharedPrefsUtil.getAllOptions()
        }

        setContent {
            ZuiTweakTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(activity = this)
                }
            }
        }
    }


}

private fun reloadSettings(context: Context) {
    val packagesToReload = mutableListOf<String>()
    if (!SharedPrefsUtil.isInitialized || !::prefsSnapshot.isInitialized) {
        Toast.makeText(context, getString(context, R.string.module_not_loaded), Toast.LENGTH_SHORT).show()
        return
    }

    for (hook in HookManager.getAllHooks()) {
        val oldState = prefsSnapshot.getOrDefault(hook.javaClass.simpleName, hook.menuItem.defaultSelected)
        val newState = SharedPrefsUtil.getOptionValue(hook.javaClass.simpleName, hook.menuItem.defaultSelected)
        if (oldState != newState) {
            packagesToReload.addAll(hook.hookTargetPackage)
        }
    }

    if (packagesToReload.contains("android")) {
        Toast.makeText(context, getString(context, R.string.reboot_required_android), Toast.LENGTH_SHORT).show()
        return
    }
    prefsSnapshot = SharedPrefsUtil.getAllOptions()

    if (packagesToReload.isEmpty()) {
        Toast.makeText(context, getString(context, R.string.no_changes_detected), Toast.LENGTH_SHORT).show()
        return
    }

    val commands = mutableListOf<String>()

    packagesToReload.distinct()
        .filter { it != "android" }
        .forEach { pkg ->
            val result = Shell.cmd("pidof $pkg").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                commands.add("killall $pkg")
            }
        }

    if (commands.isEmpty()) {
        Toast.makeText(context, getString(context, R.string.no_running_processes), Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val result = Shell.cmd(*commands.toTypedArray()).exec()
        if (!result.isSuccess) {
            Toast.makeText(context, getString(context, R.string.failed_reload_settings), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, getString(context, R.string.settings_reloaded), Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "${getString(context, R.string.failed_reload_settings)}: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AppContent(modifier: Modifier = Modifier, activity: Activity? = null) {
    val deviceModel = remember { Constants.deviceModel }
    var isRooted by remember { mutableStateOf(false) }
    var region by remember { mutableStateOf("") }
    val isModuleEnabled by remember { mutableStateOf(SharedPrefsUtil.isInitialized) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val categories = HookOptions.getHookOptions(context)
    var expanded by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(LanguageUtil.getLanguage()) }

    LaunchedEffect(Unit) {
        scope.launch {
            isRooted = Util.isDeviceRooted()
            region = Constants.deviceRegion
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.app_title),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(id = R.string.by_stonecold),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Box(modifier = Modifier.align(Alignment.CenterVertically)) {

                    // 메뉴 버튼 추가
                    IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    // 드롭다운 메뉴
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        offset = DpOffset(x = (-8).dp, y = 0.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.settings_view)) },
                            onClick = {
                                expanded = false
                                val intent = Intent(context, SettingsViewActivity::class.java)
                                context.startActivity(intent)
                            })

                        if (isRooted || BuildConfig.DEBUG) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.reload)) },
                                enabled = isRooted && isModuleEnabled,
                                onClick = {
                                    expanded = false
                                    reloadSettings(context)
                                })
                        }

                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.language)) },
                            onClick = {
                                expanded = false
                                languageMenuExpanded = true
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.exit)) },
                            onClick = {
                                activity?.finish()
                            })
                    }

                    // 언어 선택을 위한 드롭다운 메뉴
                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false },
                        offset = DpOffset(x = (-8).dp, y = 0.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.korean)) },
                            onClick = {
                                selectedLanguage = "ko"
                                LanguageUtil.setLanguage("ko")
                                updateLanguage(context, "ko")
                                languageMenuExpanded = false
                            })

                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.english)) },
                            onClick = {
                                selectedLanguage = "en"
                                LanguageUtil.setLanguage("en")
                                updateLanguage(context, "en")
                                languageMenuExpanded = false
                            })
                    }
                }
            }

            Text(
                text = stringResource(id = R.string.device_info_description),
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.information),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${stringResource(id = R.string.device_model)} $deviceModel",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (deviceModel != "TB371FC" && deviceModel != "TB320FC") {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.unsupported_device),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.region),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = region,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (region == "UNKNOWN") Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.rooting_status),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isRooted) stringResource(id = R.string.rooted) else stringResource(id = R.string.not_rooted),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isRooted) MaterialTheme.colorScheme.onSurface else Color.Red
                )
            }
            Row(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.module_status),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isModuleEnabled) stringResource(id = R.string.enabled) else stringResource(id = R.string.disabled),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isModuleEnabled) MaterialTheme.colorScheme.onSurface else Color.Red
                )
            }
        }

        val isSupportedDevice = (deviceModel == "TB371FC" || deviceModel == "TB320FC")

        items(categories) { (category, options) ->
            CategorySection(category = category, options = options.map { it.copy(isEnabledOption = isSupportedDevice && it.isEnabledOption) }, context = context)
        }

        item {
            ConfigSection(context = context)
            NoticeSection()
        }
    }
}

@Composable
fun ConfigSection(context: Context) {
    var isIconHidden by remember { mutableStateOf(isLauncherIconHidden(context)) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(id = R.string.settings),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.hide_launcher_icon),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isIconHidden,
                onCheckedChange = {
                    isIconHidden = it
                    setLauncherIconHidden(context, it)
                    Toast.makeText(context, getString(context, R.string.settings_changed), Toast.LENGTH_LONG).show()
                }
            )
        }
        Text(
            text = stringResource(id = R.string.hide_icon_description),
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun NoticeSection() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(id = R.string.notice),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.feature_not_guaranteed),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.imported_settings),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

fun initializeLanguage(context: Context) {
    val savedLanguage = LanguageUtil.getLanguage()
    var language = savedLanguage
    val defaultLocale = Locale.getDefault().language

    if (language.isNullOrEmpty()) {
        language = if (defaultLocale == "ko") "ko" else "en"
    }

    if (defaultLocale != language) {
        val locale = Locale.forLanguageTag(language.replace('_', '-'))
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        ZuiTweakApplication.appContext.createConfigurationContext(config)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        @Suppress("DEPRECATION")
        ZuiTweakApplication.appContext.resources.updateConfiguration(config, context.resources.displayMetrics)

        HookManager.registerHooks()
    }
}

fun updateLanguage(context: Context, language: String) {
    val locale = Locale.forLanguageTag(language.replace('_', '-'))
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.createConfigurationContext(config)

    val refreshIntent = Intent(context, SettingsActivity::class.java)
    refreshIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    context.startActivity(refreshIntent)
}

fun isLauncherIconHidden(context: Context): Boolean {
    val componentName = ComponentName(context, MainActivity::class.java)
    val pm = context.packageManager
    val componentEnabledSetting = pm.getComponentEnabledSetting(componentName)
    return componentEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
}

fun setLauncherIconHidden(context: Context, hidden: Boolean) {
    val componentName = ComponentName(context, MainActivity::class.java)
    val pm = context.packageManager
    pm.setComponentEnabledSetting(
        componentName,
        if (hidden) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
        PackageManager.DONT_KILL_APP
    )
}

@Composable
fun CategorySection(category: String, options: List<HookOption>, context: Context) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = category,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        options.forEach { option ->
            if (SharedPrefsUtil.isInitialized && !option.isEnabledOption) {
                SharedPrefsUtil.deleteOptionValue(option.key)
            }
            OptionItem(context = context, option = option)
        }
    }
}

@Composable
fun OptionItem(context: Context, option: HookOption, modifier: Modifier = Modifier) {
    var isChecked by remember { mutableStateOf(SharedPrefsUtil.isInitialized && SharedPrefsUtil.getOptionValue(option.key, option.defaultEnabled)) }
    val isEnabled by remember { mutableStateOf(SharedPrefsUtil.isInitialized && option.isEnabledOption) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    if (SharedPrefsUtil.isInitialized) {
                        SharedPrefsUtil.setOptionValue(option.key, isChecked)
                    } else {
                        Toast.makeText(context, getString(context, R.string.settings_changed), Toast.LENGTH_LONG).show()
                    }
                },
                enabled = isEnabled
            )
        }
        Text(
            text = option.description,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZuiTweakTheme {
        AppContent()
    }
}
