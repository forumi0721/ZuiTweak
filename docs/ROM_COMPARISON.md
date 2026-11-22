# 글로벌(ROW) vs PRC ROM 기능 차단 비교 조사

> 작성일: 2026-11-22
> 대상: `ref/TB320FC/17.0.267` (글로벌/ROW), `ref/TB371FC/16.0.474_CN` (PRC/CN)
> 목적: 글로벌롬에서 막아놓은 PRC 기능과, 그 반대(PRC롬에서 막아놓은 ROW 기능)를 ZuiTweak 관점에서 정리

---

## 1. 조사 대상

| 구분 | 경로 | 빌드 | Android | SDK | locale | 지역 prop |
|---|---|---|---|---|---|---|
| 글로벌 (ROW) | `ref/TB320FC/17.0.267/img` | `TB320FC_ROW_OPEN_USER_Q00031.0_V_ZUI_17.0.267_ST_250429` | 15 | 35 | `en-US` | `ro.config.lgsi.region=row` |
| PRC (CN) | `ref/TB371FC/16.0.474_CN/img` | `TB371FC_CN_OPEN_USER_Q00028.1_U_ZUI_16.0.474_ST_241129` | 14 | 34 | `zh-Hans-CN` | `ro.config.lgsi.region=prc` |

두 롬은 모델(TB320FC vs TB371FC), Android 버전(15 vs 14), SoC(GPU `taro` vs `kona`)가 다르므로,
순수 지역 차이와 버전/기기 차이를 구분해서 봐야 한다.

---

## 2. 차단 메커니즘 (핵심)

**지역 게이팅은 사실상 `product/build.prop` 한 줄로 제어된다.**

```
// GLOBAL product/build.prop
ro.config.lgsi.region=row

// PRC product/build.prop
ro.config.lgsi.region=prc
```

양쪽 롬의 `com.lenovo.common.utils.LenovoUtils` 코드는 동일하다.

```java
public static boolean isRowVersion() {
    String str = SystemProperties.get("ro.config.lgsi.region");
    return !TextUtils.isEmpty(str) ? str.equalsIgnoreCase("ROW") : ...;
}
public static boolean isPrcVersion() {
    String str = SystemProperties.get("ro.config.lgsi.region");
    return !TextUtils.isEmpty(str) ? str.equalsIgnoreCase("PRC") : ...;
}
```

설정 앱(`ZuiSettings.apk`)에는 **PRC/ROW 양쪽 분기 코드가 모두 들어있고**, prop 값에 따라
`isPrcVersion()` / `isRowVersion()` 결과가 달라져 특정 메뉴가 보이거나 숨겨진다.

게이팅 표현 해석:

| 표현 | PRC롬 | 글로벌롬 |
|---|---|---|
| `isPrcVersion() ? 0 : 3` | 사용 가능 (0) | 차단 (3 = UNSUPPORTED) |
| `isRowVersion() ? 3 : 0` | 사용 가능 (0) | 차단 (3) |
| `isRowVersion() ? 0 : 3` | 차단 (3) | 사용 가능 (0) |
| `isPrcVersion() ? 3 : 0` | 차단 (3) | 사용 가능 (0) |

여기에 2차 게이트가 겹치기도 한다: `LgsiFeatures.enabled("...")` 플래그, `ZuiConfig.getBoolean(...)`,
`ro.config.zui.*` / `persist.zui.*` prop, 그리고 **해당 APK가 실제로 존재하는지** 여부.

---

## 3. 표 A. 글로벌롬에서 막힌 PRC 기능

> `0` = AVAILABLE, `3` = UNSUPPORTED (AOSP Settings 기준)

### 3-1. 이미 ZuiTweak이 처리 중

| 기능 | 클래스 / 메서드 | 게이팅 | 대응 훅 |
|---|---|---|---|
| Multiple Space / 앱 클론 | `applications/LenovoAppHeaderPreferenceController.initAppEntryList()` — `isPrcVersion() && AppCloneUtils.supportsAppClone()` | PRC 전용 | `HookEnableMultipleSpace` (ROW) |
| 멀티앱 검색 항목 | `applications/LenovoAppDashboardFragment.getRawDataToIndex()` — `isPrcVersion()` | PRC 전용 | 위 훅과 함께 처리 |
| 스마트 스플릿 / 패럴렐 월드 | `onevision/OneVisionSettingsFragment.initView()`, `onevision/EmbeddingAppFragment` | PRC/ROW 분기 | `HookEnableOneVisionSmartSplit` (ROW) |
| 배터리 과열 알림 | `battery/ZuiBatteryOverheatPreferenceController.getAvailabilityStatus()` — `isRowVersion()?3:0` | PRC 전용 | `HookEnableBatteryOverheatNotify` (ROW) |
| 숨겨진 디스플레이 설정 | `display/GameEnhancePreferenceController`, `VideoColorEnhancePreferenceController`, `VideoQualityEnhancePreferenceController` | 글로벌은 플래그로 0 가능 | `HookEnableHiddenDisplaySettings` (COMMON) |
| WLAN 핫스팟 / WiFi Direct | `TopLevelTetherPreferenceController`, `WifiEnterpriseRestrictionUtils`, `LenovoUtils.isSupportTether()` | 기업 정책/Region 복합 | `HookEnableHotspot`, `HookEnableWifiDirect` |

### 3-2. 신규 후보 (Xposed로 비교적 쉽게 해제 가능)

| 기능 | 클래스 / 메서드 | 게이팅 | 비고 / 선행 조건 |
|---|---|---|---|
| 사전설치 앱 복원 | `applications/RestorePreinstalledAppsPreferenceController.getAvailabilityStatus()` | `isPrcVersion()?0:3` | 가장 단순, 후보 1순위 |
| 앱 정보 '공유' 항목 | `applications/appinfo/SharePreferenceController.getAvailabilityStatus()` | `isPrcVersion()?0:3` | 단순 |
| 수면 중 전력 절약 | `battery/ZuiPowerSavingDuringSleepController.isSupport()` | `!isCommercial && LgsiFeatures("ZuiNightPowerSave") && isPrcVersion()` | 글로벌롬에 `ZuiNightPowerSave` 플래그 존재 |
| 오픈소스 라이선스 정보 | `deviceinfo/legal/OpenSourceInfoController.getAvailabilityStatus()` | `isPrcVersion()?0:3` | 단순 |
| 잠금화면 갤러리 자동변경 | `display/LockScreenGalleryController.getAvailabilityStatus()` | `isPrcVersion()?0:3` | 설정 키 `keyguard_gallery_enable` |
| 알림 배지 관리자 | `notification/BadgeManagerController.getAvailabilityStatus()` | `isRowVersion()?3:0` | `com.zui.homesettings` 설치 필요(양쪽 존재) |
| Lenovo 서비스 메뉴 | `deviceinfo/controller/LenovoServicePreferenceController.getAvailabilityStatus()` | `isRowVersion()?3:0` | 전화번호 `4009908888` 하드코딩 |
| 출처 없는 앱 설치 허용 | `emergency/InstallOthersPreferenceController` (`isRowVersion()?3:0`), `emergency/AppInstallationGuardPreferenceController` (`isPrcVersion()?0:3`) | PRC 전용 | ⚠️ PRC `ZuiPackageInstaller` APK 필요 |
| Lenovo 계정 / 클라우드 백업 | `homepage/controller/TopLevelLenovoAccountPreferenceController`, `backup/BackupPreferenceController`, `account/LenovoAccountFragment` | `isRowVersion()` → 3 | ⚠️ Lenovo 계정 앱/서비스 필요 |
| WAPI 인증서 관리 | `wifi/WifiWapiPreferenceController.isAvailable()` | `isRowVersion()` → false | 중국 WAPI 표준 |
| 규제정보 / CMIIT ID | `deviceinfo/controller/RegulatoryInfoPreferenceController`, `deviceinfo/aboutphone/RegulatoryLabelFragmentForPrc` | `isPrcVersion()` 분기 | 글로벌에도 `fragment_regulatory_label_for_prc` 리소스 존재 |
| 얼굴 인식 등록 개수 1→5 | `fap/utils/FapUtils.getFaceMaxSize()` | `isRowVersion()` → 1, PRC → `config_faceMaxTemplatesPerUser`(5) | `debug_prc_face_id` 설정으로 우회 가능 |
| 비디오 모드 / AI 자막 / 거리알림 | `lab/VideoModePreferenceController`, `lab/VideoModeFragment`, `lab/LabFeaturesSettings` | `isRowVersion()` → 3 | `video_mode_feature_enable` 설정도 필요 |
| 키보드 PRC 전용 항목 | `keyboard/ShortcutSwitchListFragment.initData()` (Win+S, Ctrl+Enter), `keyboard/AppQuickSettingsFragment.buildCandidates()` (Privacy Shield, AI 자막), `keyboard/AppFunctionOptionsFragment` (소천 AI) | `isPrcVersion()` 분기 | `HookChangeKeyboardHotkeys`는 TB371FC 한정 |
| OneVision 아이콘/문구 | `onevision/EmbeddingAppFragment`, `OneVisionSettingsFragment` | PRC → 가로모드, ROW → 스마트 로테이션 | 스플릿 훅과 연계 |
| 작업표시줄 기본 ON | `system/NavigationGifPreference.getDefaultStates()` | ROW 0 / PRC 1 | PRC 기본값 흉내 |
| Lenovo One / OneFS 전송 | `transfer/LenovoOneSettings`, `transfer/LenovoOneFSSettings`, `TopLevelLenovoOnePreferenceController` | `!isRowVersion()` → 표시 | PRC 전용 코드 다수 |

### 3-3. PRC롬에만 존재하는 코드 / APK (Xposed만으론 불가)

- **Settings 코드**: 학습모드(Study Launcher) 대규모 패키지(`learningmode/*`), 충전 최적화·배터리 사용량 차트 등 `fuelgauge/ZuiBattery*`, 지문·얼굴 등록 확장(`fap/*`), 기기 업데이트/소프트웨어 RF, 화면 주사율 설정(`ScreenRefreshRate*`)
- **APK**: `ZuiSecurity`, `ZuiClone`, `ZuiPackageInstaller`, `ZuiPermissionController`, `ZuiTelecom`, `ZuiContacts`, `ZuiGallery`, `ZuiBrowser`, `ZuiFileManager`, `ZuiCalendar`, `XuiCalculator`, `ZuiAlarm`
- **중국 서비스**: `BaiduNetworkLocation`, `BaiduNetdisk`, `SogouInput`, `iqiyi`, `kwai_HD`, `QQMusic`, `Weibo_HDwm`, `Moffice_196`, `Touchnotes_HD`, `LenovoStore`, `LenovoSmartBuy_lenovopad`, `Music`
- **생체/결제**: `ifaa_service`, `SoterService`, `UFace`, `GFTest`, `LenovoFaceDetectService`, `QFingerprintService`
- **기타**: `PerformanceMode`, `HyperEngine`, `QColor`, `QdcmFF`, `DataMonitor`, `ZUISetupWizardExtPRC`

---

## 4. 표 B. PRC롬에서 막힌 ROW/글로벌 기능 (반대 케이스)

같은 코드의 반대 분기. PRC롬 이용자가 글로벌 기능을 쓰고 싶을 때 해당한다.

### 4-1. Settings 코드 레벨

| 기능 | 클래스 / 메서드 | 게이팅 | 대응 훅 |
|---|---|---|---|
| 화면 켜기 (Turn screen on) | `applications/specialaccess/TurnScreenOnPreferenceController.getAvailabilityStatus()` | `isRowVersion()?0:3` → PRC에서 3 | ⚠️ 소스에 `HookEnableTurnScreenOn` 존재하나 `HookManager`에 등록되지 않음 ("테스트 필요") |
| 펌웨어 버전 검색 페이지 | `deviceinfo/FirmwareVersionSettings.isPageSearchEnabled()` | `isRowVersion()` → PRC에서 false | 미구현 |
| 소프트웨어 RF 정보 | `deviceinfo/controller/SoftwareRfPreferenceController.isAvailable()` | `isRowVersion() && prop` → PRC에서 false | 미구현 |
| 배터리 제조일자 표시 | `deviceinfo/controller/BatteryManufacturingDateController.isSupportBatteryDateFeature()` | `... && isRowVersion()` → PRC에서 false | 미구현 |
| Focus Mode / 알림 없는 시간 | `notificationfree/FocusModeFragment.isPageSearchEnabled()`, `zen/NotifiFreePreferenceController.getAvailabilityStatus()` | `isRowVersion()?0:3` → PRC에서 3 | 미구현 |
| 이더넷 설정 | `ethernet/EthernetSettings.hasEthernet()` | `isRowVersion() \|\| isCommercialProject()` → PRC(비상용)에서 미노출 | 미구현 |
| Captive Portal 설정 | `development/CaptivePortalPreferenceController.isAvailable()` | `(PadKirby/PadAsphalt) && isPrcVersion()` → false | 미구현 |
| 모바일 데이터 사용량 설정 일부 | `sim/DualCardPrefsFragment.getNonIndexableKeys()` | `isROWVersion()` 분기 | 미구현 |
| 키보드 마이크/카메라 토글, 실시간 자막 | `keyboard/AppQuickSettingsFragment.buildCandidates()` | ROW 항목(15/16/18번)만 추가 | 미구현 |
| 키보드 Alt+Shift, Ctrl+Shift 단축키 | `keyboard/ShortcutSwitchListFragment.initData()` | `isRowVersion()` 분기 | 미구현 |
| 키보드 AI Now | `keyboard/AppFunctionOptionsFragment.getDefaultKey()/buildCandidates()` | ROW → AI Now, PRC → 소천( XiaoTian) | 미구현 |
| 기기 이름 prop 소스 | `core/PermissionReceiver.getPropName()`, `deviceinfo/ZuiDeviceNameHelper.getPropName()` | ROW → `ro.odm.lenovo.series`, PRC → `ro.zuk.product.market` 등 | PRC롬에 해당 prop 없으면 빈 값 |
| 태스크스위치 토글 | `system/taskbar/TaskbarSwitchPreferenceController.getAvailabilityStatus()` | 글로벌 상시 0 / PRC `isTaskbarToggleEnable()?0:3` | 미구현 |
| 숨겨진 디스플레이 설정 (게임 HDR, 비디오 컬러/화질) | `display/VideoColorEnhancePreferenceController`, `display/VideoQualityEnhancePreferenceController` | 글로벌은 `ZuiVideoColorEnhance` 플래그/리소스로 0 가능, **PRC는 하드코딩 `return 3`** | ✅ `HookEnableHiddenDisplaySettings`가 커버 |
| 읽기 모드 관련 | `display/readmode/ReadModePreferenceController`, `ReadingModeSaveEnergy*Controller` | 글로벌 `LgsiFeatures("ZuiReadingMode"/"ZuiReadingModeLowRefresh")`, PRC는 리소스 bool / 상수 | 위 훅과 연계 |

> 참고: 반대 방향이라고 해서 전부 PRC롬이 불리한 것은 아니다. 예:
> `ZuiEmergencyDashboardFragment.refshSecurityPatch()`는 `isRowVersion()`이면 보안 패치 항목을 **제거**하므로
> PRC롬에서 더 많은 정보를 보여준다. 이런 항목은 표 A(PRC 기능)에 해당한다.

### 4-2. APK 레벨 (PRC롬에 아예 없음)

| 분류 | 글로벌롬에만 있는 APK | PRC롬 대체품 |
|---|---|---|
| Google 앱 | `Chrome`, `Gmail2`, `Maps`, `Meet`, `Messages`, `Photos`, `Videos`, `YouTube`, `YouTubeKids`, `YTMusic`, `Drive`, `Books`, `Phonesky`, `GWP`, `Velvet`, `AssistantShell`, `SearchSelector` | ZUI 앱(`ZuiBrowser`, `ZuiGallery`, `Music`, ...) |
| Google 서비스/컴포넌트 | `AndroidPlatformServices`, `GooglePackageInstaller`, `DocumentsUIGoogle*`, `LatinImeGoogle`, `CaptivePortalLoginGoogle*`, `NetworkStackGoogle*`, `Wellbeing`, `FamilyLinkParentalControls`, `FamilySpace`, `GoogleKidsSpace`, `PlayGames`, `PersonalSafety`, `DeviceDiagnostics`, `CrossDeviceServices` | AOSP/ZUI 대응 컴포넌트 |
| 기타 | `Opera`, `OperaProvider`, `RuStore`, `AppCloud`, `AppHub`, `EntertainmentSpace`, `TabtopPicks`, `Xpan`, `Turbo`, `MyScript_Nebo`, `MyScript_Calculator`, `CalculatorGoogle`, `CalendarGoogle`, `DeskClockGoogle`, `ZuiAIService`, `ZuiStandbyMode`, `ZUIDemoMode`, `ZuiKeyboardUpdateOlympia` | 해당 없음 / PRC 전용 앱 |

### 4-3. PRC롬에 없는 글로벌 prop

| prop | 글로벌 | PRC | 영향 |
|---|---|---|---|
| `ro.apex.updatable` | `true` | (없음) | Mainline 모듈 OTA 업데이트 불가 |
| `ro.config.lgsi.default_refreshrate` | `144` | `0` | 기본 주사율 |
| `ro.surface_flinger.game_default_frame_rate_override` | `60` | (없음) | 게임 프레임 고정 |
| `ro.surface_flinger.set_launcher_timer_ms` | `30000` | (없음) | 런처 타이머 |
| `ro.config.vc_call_vol_steps` | `7` | (없음) | 통화 볼륨 단계 |
| `dalvik.vm.enable_pr_dexopt` | `true` | (없음) | PR dexopt |
| `persist.wm.extensions.activity_embedding_guard_with_android_15` | `false` | (없음) | Activity embedding |

> 전체 prop 목록과 기능 설명은 **5장** 참조.

---

## 5. prop 전체 정리 및 기능 설명

### 5-0. 조사 범위 및 주의

- 4개 파티션(system / product / system_ext / vendor)의 **`build.prop`, `default.prop`, `wps.prop` 전체**를 경로 보존 추출해 비교.
  - 고유 key 수: 글로벌 **655개**, PRC **593개** (양쪽 합집합은 더 많음)
- 대부분은 **SoC·버전·기기 차이**이며, 지역 게이팅에 관여하는 prop은 소수다. 아래 표에서 `추정`은 디컴파일 코드에서 직접 사용처를 확인하지 못한 항목이다.
- `=--` 는 해당 롬에 prop이 없다는 뜻이다 (값이 빈 문자열과는 다름).

### 5-1. 지역 / 로케일 / 유통

| prop | 글로벌 | PRC | 하는 일 |
|---|---|---|---|
| `ro.config.lgsi.region` | `row` | `prc` | **마스터 스위치.** `LenovoUtils.isPrcVersion()/isRowVersion()`이 읽어 표 A/B의 모든 메뉴를 켜고 끔 |
| `ro.config.lgsi.fp.incremental` | `..._ROW` | `..._PRC` | 빌드 지문(표시용) |
| `ro.config.lgsi.sw.version` | `TB320FC_RF01_250429` | `TB371FC_S100474_..._PRC` | 펌웨어 버전 문자열 |
| `ro.software.version` | `--` | `TB371FC_RF01_241129` | PRC 전용 소프트웨어 버전 |
| `ro.product.locale` | `en-US` | `zh-Hans-CN` | 시스템 기본 로케일 |
| `ro.product.locale.language` | `--` | `zh-Hans` | PRC 언어 명시 |
| `ro.product.locale.region` | `--` | `CN` | PRC 지역 명시 |
| `persist.sys.country` | `--` | `CN` | 국가 코드 (PRC SetupWizard/서비스 분기) |
| `persist.sys.language` | `--` | `zh-Hans` | 시스템 언어 (PRC) |
| `persist.sys.timezone` | `--` | `Asia/Shanghai` | 기본 시간대 |
| `ro.boot.wificountrycode` | `US` | `CN` | **WiFi 규제 도메인.** 사용 가능 채널/전력이 국가별로 달라짐 (5GHz 채널 제한 등) |
| `ro.com.google.clientidbase` | `--` | `android-lenovo` | PRC GMS 클라이언트 ID |
| `ro.channel.com.newskyer.draw` | `--` | `LENOVO-PRELOAD` | 유통 채널 / 사전설치 번들 구분 (PRC) |
| `ro.vendor.config.lgsi.market_name` | `Lenovo Legion Tab` | `小新Pad Pro 12.7（骁龙版）` | 설정 > 기기 정보에 표시되는 시장명 |
| `ro.vendor.config.lgsi.short_market_name` | `--` | `小新Pad Pro 12.7` | 짧은 시장명 |
| `ro.vendor.config.lgsi.ota.model` | `TB320FC_ROW` | `LenovoTB371FC_PRC` | OTA 모델 식별자 |
| `ro.com.google.gmsversion` | `15_202501` | `14_202311` | GMS 버전 (Android 버전 차이 반영) |

### 5-2. ZUI 기능 플래그 (게이팅 직결)

| prop | 글로벌 | PRC | 하는 일 (확인된 사용처) |
|---|---|---|---|
| `ro.config.zui.education` | `--` | `true` | `TianJiaoUtils.isTianjiaoSupport()` → 학습모드/동반학습, TianJiao 얼굴 관리, 학습런처 메뉴 표시 |
| `ro.zui.software.safeurl` | `--` | `true` | `ZuiEmergencyDashboardFragment.isSafeUrlSupported()` → SafeCenter의 안전 URL 검사 스위치 표시 |
| `ro.zui.software.weblimit` | `--` | `true` | `LearningModeContentLimit` → 학습모드의 "웹 콘텐츠 제한" 메뉴 표시 |
| `ro.config.lgsi.DataMonitor_enabled` | `--` | `true` | `DualCardPrefsFragment.PROPERTY_NETWORK_CONTROL` → SIM/데이터 사용량 제어 메뉴 |
| `ro.zui.software.networkdiag` | `--` | `--` | `DualCardPrefsFragment.PROPERTY_NETWORK_DETECTION` — 양쪽 build.prop에 없음(기본값 사용 추정) |
| `ro.config.lgsi.graffitinotes.cloudservice` | `--` | `true` | 낙서 메모(Graffiti Notes) 클라우드 동기화 활성화 (추정) |
| `ro.config.lgsi.ov_split.default_no` | `--` | `4` | OneVision 분할 화면 기본 앱 개수/값 (추정) |
| `ro.config.lgsi.fingerprint.position` | `--` | `top_power` | 지문 센서 위치(상단 전원 버튼) 안내/UI 분기 |
| `persist.zui.clone.writeSms.enable` | `--` | `true` | 앱 클론(중복 앱)에서 SMS 쓰기 허용 (ZuiClone.apk) |
| `persist.zui.clone_allow_backup.on` | `--` | `false` | 앱 클론 데이터 백업 허용 여부 |
| `ro.config.bindcore.enabled` | `--` | `true` | Lenovo bindcore 서비스(프로세스 코어 바인딩) 활성화 (추정) |
| `persist.wm.zui_app_start_animation` | `--` | `1` | PRC 앱 시작 애니메이션 활성화 (추정) |
| `ro.zui.filemanager.authorization` | `true` | `true` | 파일매니저 권한 부여 로직 (양쪽 동일) |
| `ro.config.lgsi.ovbase_enabled` | `true` | `true` | OneVision 기반 기능 (양쪽 동일) |
| `ro.sys.zui.param.a / b / c` | `--` | `0.0000000001 / 0.15259 / 0` | `BrightnessUtils`의 감마→리니어 밝기 변환 곡선 파라미터 (글로벌은 기본값 `0.000002/0.022/0.88` 사용) |
| `ro.sys.zui.theme.list` | `...lime,grace,pink,blue,golden,thin,paintingr` | `...thin,lime,superhero,avengers,grace,...` | 선택 가능 테마 목록. PRC는 `superhero`, `avengers` 추가 |
| `ro.com.zui.version` | `17.0` | `16.0` | ZUI 버전 표기 |
| `ro.config.lgsi.os.version` | `17.0` | `--` | 글로벌 전용 OS 버전 prop |
| `ro.config.lgsi.ui.version` | `17.0` | `16.0` | UI 버전 |
| `ro.config.lgsi.sys.project` | `asphalt` | `lgsi` | 내부 프로젝트 코드명 |
| `ro.config.lgsi.os.name` | `ZUI` | `--` | OS 이름 |

### 5-3. 디스플레이 / 주사율 / 색상

| prop | 글로벌 | PRC | 하는 일 |
|---|---|---|---|
| `ro.config.lgsi.default_refreshrate` | `144` | `0` | 부팅 시 기본 주사율. PRC는 `0` = 시스템 결정 |
| `ro.config.lgsi.surface_flinger.available_refreshrates` | `30_60_120_144` | `30_60_120_144` | 선택 가능 주사율 목록 (양쪽 동일) |
| `ro.config.lgsi.aod_require_fps` | `0` | `0` | AOD 요구 FPS (양쪽 동일) |
| `ro.config.lgsi.low_power_mode_require_fps` | `60` | `60` | 저전력 모드 FPS (양쪽 동일) |
| `ro.config.lgsi.reading_mode_require_fps` | `30` | `30` | 읽기 모드 FPS (양쪽 동일) |
| `ro.config.lgsi.surface_flinger.widen_range_default` | `60` | `60` | 확장 주사율 기본값 |
| `ro.config.lgsi.surface_flinger.widen_range_altered` | `90` | `90` | 확장 주사율 변경값 |
| `ro.surface_flinger.game_default_frame_rate_override` | `60` | `--` | 게임 기본 프레임 60 고정 (글로벌) |
| `ro.surface_flinger.set_launcher_timer_ms` | `30000` | `--` | 런처 타이머 |
| `ro.surface_flinger.set_touch_timer_ms` | `2000` | `200` | 터치 타임아웃 |
| `ro.surface_flinger.primary_display_orientation` | `--` | `ORIENTATION_270` | PRC 패널 기본 회전 방향 |
| `ro.sf.lcd_density` | `--` | `340` | PRC 화면 밀도 |
| `persist.sys.sf.native_mode` | `257` | `256` | SurfaceFlinger 색상 모드 ID (257=글로벌 팔레트, 256=PRC) |
| `persist.sys.colormode_modes` | `257:native;0:standard` | `256:native;0:standard;255:P3` | 색상 모드 목록. PRC는 P3 모드 추가 |
| `persist.sys.colormode_api` | `google` | `google` | 색상 API (양쪽 동일) |
| `persist.sys.sf.color_temperature.cold/warm` | `0.957,0.953,1.0` / `1.0,0.961,0.920` | `0.949019,0.952941,1.0` / `1.0,0.952941,0.921568` | 색온도 보정값 |
| `vendor.display.comp_mask` | `0` | `6` | HWC composition mask |
| `vendor.display.enable_optimize_refresh` | `0` | `1` | 주사율 최적화 (PRC 활성) |
| `vendor.display.disable_idle_time_hdr / video` | `--` | `1` | 유휴 시 HDR/비디오 레이어 비활성화 (PRC, 절전/발열 대응 추정) |
| `vendor.display.enable_camera_smooth / posted_start_dyn` | `--` | `1` | PRC 디스플레이 파이프라인 튜닝 |
| `debug.sf.*` (phase offset, backpressure 등) | `--` | 다수 존재 | PRC SurfaceFlinger 고주사율 타이밍 튜닝 |
| `debug.sf.hw`, `debug.egl.hw` | `--` | `0` | PRC 디버그 플래그 (HW 경로 우회) |

### 5-4. 오디오 / 벨소리

| prop | 글로벌 | PRC | 하는 일 |
|---|---|---|---|
| `ro.config.ringtone` | `Ring_Synth_04.ogg` | `StereoTime.ogg` | 기본 벨소리 |
| `ro.config.notification_sound` | `Pollux.ogg` / `OnTheHunt.ogg` | `StereoTwinkling.ogg` / `pixiedust.ogg` | 기본 알림음 (파티션별 상이) |
| `ro.config.alarm_alert` | `Oxygen.ogg` / `Alarm_Classic.ogg` | `EtherealMallets.ogg` / `Alarm_Classic.ogg` | 기본 알람음 |
| `ro.config.vc_call_vol_steps` | `7` | `--` | 통화 볼륨 단계 수 (글로벌) |
| `audio.sys.noisy.broadcast.delay` | `600` | `500` | 오디오 noisy broadcast 지연 |
| `ro.audio.flinger_standbytime_ms` | `2000` | `--` | 오디오 flinger 대기 시간 |
| `vendor.audio.hw.aac.encoder` | `false` | `true` | AAC 하드웨어 인코더 (PRC 활성) |
| `persist.vendor.audio.fluence.*` | `--` | 다수 | PRC 통화 오디오 Fluence(노이즈 캔슬링) 설정 |
| `persist.audio.fluence.*` | 다수 | `--` | 글로벌 통화 오디오 Fluence 설정 |
| `persist.vendor.audio.ambisonic.*` | `--` | `false` | 앰비소닉 오디오 (PRC 비활성) |
| `ro.vendor.audio.para_version` | `--` | `Spinel-Audiopara-drv-v16-...` | PRC 오디오 파라미터 버전 |
| `ro.vendor.dolby.dax.version` | `DAX3_3.13.0.9_r1` | `DAX3_3.7.1.0_r1` | Dolby Atmos DAX 버전 — **ZuiTweak의 Dolby Atmos 훅과 관련** |

### 5-5. Bluetooth / 펜

| prop | 글로벌 | PRC | 하는 일 |
|---|---|---|---|
| `bluetooth.profile.asha.central.enabled` | `--` | `true` | 보청기(ASHA) 프로파일 (PRC) |
| `ro.bluetooth.leaudio_offload.supported` | `true` | `--` | LE Audio 오프로드 지원 (글로벌) |
| `persist.bluetooth.leaudio.*` | 다수 | `--` | LE Audio 동작 옵션 (글로벌) |
| `persist.vendor.qcom.bluetooth.aptxadaptiver2_1_support` | `true` | `false` | aptX Adaptive 지원 여부 |
| `bluetooth.device_id.vendor_id` | `0x001D` | `--` | Bluetooth_VENDOR_ID |
| `bluetooth.hfp.codec_aptx_voice.enabled` | `true` | `--` | aptX Voice 코덱 (글로벌) |
| `persist.vendor.qcom.bluetooth.soc` | `--` | `hastings` | Bluetooth 칩셋 |
| `persist.sys.lenovo.btpen.name` | `--` | `Lenovo Precision Pen 3` | PRC 펜 이름 |
| `ro.vendor.config.lgsi.pen_info` | `0x617F:0x17EF:Lenovo Tab Pen PLus` | `0x612B:0x17EF:Lenovo Precision Pen 3` | 펜 VID:PID:이름 — **HookEnablePenService / HookDisableAutoPenButton과 관련** |

### 5-6. 카메라

| prop | 글로벌 | PRC | 하는 일 |
|---|---|---|---|
| `vendor.camera.aux.packagelist` | `org.codeaurora.snapcam,com.zui.camera` | `org.codeaurora.snapcam` | 보조 카메라 앱 목록. 글로벌은 ZuiCamera 추가 |
| `vendor.camera.packagename.zui` | `1` | `--` | ZuiCamera를 카메라 앱으로 지정 (글로벌) |
| `ro.camera.enableCamera1MaxZsl` | `1` | `--` | Camera1 max ZSL 활성화 (글로벌) |
| `ro.vendor.config.lgsi.camerainfo` | `8/2/13` | `Lgsi-Common-Camera` | 카메라 모듈 정보 |
| `ro.com.google.lens.oem_camera_package` | `com.zui.camera` | `com.zui.camera` | Google Lens 연동 카메라 (양쪽 동일) |

### 5-7. ART / dexopt / 성능

| prop | 글로벌 | PRC | 하는 일 |
|---|---|---|---|
| `ro.dalvik.vm.enable_uffd_gc` | `true` | `false` | userfaultfd 기반 GC (글로벌 활성, Android 15 기능) |
| `dalvik.vm.enable_pr_dexopt` | `true` | `--` | Partial/PR dexopt (글로벌) |
| `pm.dexopt.post-boot` | `verify` | `extract` | 부팅 후 dexopt 방식 |
| `pm.dexopt.install-bulk-secondary-downgraded` | `verify` | `extract` | 대량 설치 시 dexopt 방식 |
| `ro.llndk.api_level` | `202404` | `--` | LLNDK API 레벨 (글로벌) |
| `ro.apex.updatable` | `true` | `--` | **Mainline(APEX) 모듈 OTA 업데이트 가능 여부** |
| `dalvik.vm.heapstartsize / heapminfree / heapmaxfree / heaptargetutilization` | `16m / 8m / 32m / 0.5` | `8m / 512k / 8m / 0.75` | ART 힙 GC 튜닝 (파티션별 상이 값도 존재) |
| `ro.vendor.perf-hal.ver` | `2.3` | `2.2` | Qualcomm perf HAL 버전 |

### 5-8. 스토리지 / 암호화 / USB

| prop | 글로벌 | PRC | 하는 일 |
|---|---|---|---|
| `external_storage.casefold.enabled` | `1` | `--` | 외부 저장소 대소문자 무시 (FUSE) |
| `external_storage.projid.enabled` | `1` | `--` | project ID 기반 할당량 |
| `external_storage.sdcardfs.enabled` | `0` | `--` | sdcardfs 비활성 (FUSE 전환) |
| `persist.sys.fuse.passthrough.enable` | `true` | `--` | FUSE passthrough |
| `ro.crypto.volume.filenames_mode` | `--` | `aes-256-cts` | 파일명 암호화 방식 (PRC) |
| `ro.crypto.volume.metadata.method` | `--` | `dm-default-key` | 메타데이터 암호화 (PRC) |
| `ro.crypto.set_dun` | `--` | `true` | FBE DUN 설정 |
| `vendor.usb.diag.func.name` | `ffs` | `diag` | USBdiag 기능 이름 |
| `vendor.usb.use_ffs_mtp` | `1` | `0` | MTP를 FFS로 처리 여부 |
| `persist.sys.usb.config` | `none` | `--` | 부팅 시 USB 설정 |

### 5-9. 디버그 / 로그 / 보안 기타

| prop | 글로벌 | PRC | 하는 일 |
|---|---|---|---|
| `persist.sys.lenovo.log.*` | 다수 (FALSE 기본) | `--` | Lenovo 커스텀 로그 수집 설정 |
| `persist.debug.sensors.*`, `persist.vendor.sensors.debug.*` | `v` | `--` | 센서 HAL 디버그 로그 |
| `debug.stagefright.ccodec` | `4` | `1` | Codec2 / OMX 우선순위 |
| `debug.c2.use_dmabufheaps` | `1` | `--` | Codec2 dmabuf heap |
| `persist.backup.ntpServer` | `0.pool.ntp.org` | `0.pool.ntp.org` | NTP 서버 (양쪽 동일, 따옴표 표기만 차이) |
| `ro.adb.secure` | `1` | `1` | ADB 인증 (양쪽 동일) |
| `keyguard.no_require_sim` | `true` | `true` | SIM 없이 잠금화면 사용 (양쪽 동일) |
| `drm.service.enabled` | `true` | `true` | DRM 서비스 (양쪽 동일) |
| `ro.control_privapp_permissions` | `enforce` | `enforce` | priv-app 권한 강제 (양쪽 동일) |
| `ro.oem_unlock_supported` | `1` | `1` | OEM unlock (양쪽 동일) |
| `sys.app.cold_start_flag` | `--` | `0000...` | PRC 앱 콜드스타트 플래그 (추정) |

### 5-10. 하드웨어 고유값 (지역 아님 — 참고)

| prop | 글로벌 | PRC | 의미 |
|---|---|---|---|
| `ro.board.platform` | `taro` | `kona` | SM8475(8+ Gen 1) vs SM8250(870) |
| `ro.soc.model` | `--` | `SM8250` | SoC 모델 |
| `ro.vendor.config.lgsi.cpuinfo` | `Qualcomm SM8475` | `870` | CPU 표기 |
| `ro.product.board` | `taro` | `spinel` | 보드 코드명 |
| `ro.gfx.driver.1` | `...taro.api32` | `...kona.api30` | Adreno GPU 드라이버 APK |
| `ro.vendor.config.lgsi.screen_size` | `11` | `12.5` | 화면 크기(인치) |
| `ro.vendor.config.lgsi.not_support_vibrator` | `--` | `true` | PRC 모델 진동 미지원 |

---

## 6. ZuiTweak 대응 현황 및 권장사항

### 현재 등록된 훅 (`HookManager.kt`)

- **COMMON**: 카메라 무음, 잠금화면 바로가기 제거, 전원종료 메시지 제거, 핫스팟, WiFi Direct, 키보드 단축키, 키보드 단축키 메뉴 비활성화, 숨겨진 디스플레이 설정, 게임서비스 아이콘, 자동 펜 버튼
- **ROW**: Multiple Space, 배터리 과열 알림, 스마트 스플릿
- **PRC**: 한국어 추가, 런처 온라인 검색 제거, DocumentsUI 크래시 수정, 학습 런처 비활성화, PRC 정보 제거, 스마트 로테이션
- **DEVICE**: 펜 서비스(TB371FC)
- **UNFUCKZUI**: 알림 아이콘, Dolby Atmos, 앱 강제종료, 바이러스 스캔, 패키지 설치관리자, 권한 컨트롤러

### 미등록(주석 처리) 훅

| 훅 | 이유 |
|---|---|
| `HookEnableTurnScreenOn` | 테스트 필요 — 표 B의 대표적인 미해제 ROW 기능 |
| `HookEnableTaskbarShowRecentApps` | 동작 안 함 |
| `HookEnableStudyLauncher` | 추가 패키지 필요 |
| `HookDisableTaskbar` | 기본 기능으로 제공 |

### 우선순위 제안

1. **가장 쉬운 신규 훅 (표 A-2)**: `RestorePreinstalledAppsPreferenceController`, `SharePreferenceController`, `OpenSourceInfoController`, `LockScreenGalleryController`, `BadgeManagerController`, `LenovoServicePreferenceController` — 모두 `getAvailabilityStatus()`를 `0`으로 리턴하게 하면 끝. `HookEnableBatteryOverheatNotify` 패턴 재사용.
2. **전역 스위치 검토**: `LenovoUtils.isPrcVersion()` 자체를 후킹하면 표 A 대부분이 한 번에 풀리지만, PRC 전용 APK를 참조하는 경로(`ZuiPackageInstaller`, `ZuiSecurity`, Lenovo 계정 등)에서 크래시 위험. 현재처럼 **스택트레이스로 호출처를 좁히는 방식**을 유지할 것.
3. **prop 방식 병행**: `ro.config.lgsi.region`은 `ro.` prop이라 Magisk `resetprop`으로 부팅 시 덮어쓰면 이론상 전체 해제 가능. 다만 CTA/개인정보/Google 서비스 분기까지 함께 바뀌므로 신중. `ZuiTweak-magisk`가 이미 이 영역을 다루고 있으므로 연계 검토.
4. **표 B 대응**: `HookEnableTurnScreenOn`을 먼저 활성화해 보거나, `FirmwareVersionSettings`, `SoftwareRfPreferenceController`, `NotifiFreePreferenceController`를 같은 패턴으로 추가 가능.

---

## 7. 조사 방법 / 근거

| 항목 | 방법 |
|---|---|
|롬 식별|각 `system/build.prop`, `product/build.prop` 직접 추출 (`debugfs cat`)|
|파일 목록 비교|4개 파티션(system/product/system_ext/vendor) 전체를 `7z l -ba`로 덤프 후 파일명 비교|
|코드 비교|`ZuiSettings.apk` 양쪽 추출 → `jadx -r` 디컴파일 → `com.lenovo.settings` 클래스/메서드 비교|
|게이팅 분석|`isPrcVersion()` / `isRowVersion()` / `getAvailabilityStatus()` / `LgsiFeatures.enabled()` 호출부 전수 조사|
|prop 비교|전 파티션의 `*.prop`을 **경로 보존(`7z x`)** 추출해 key/value diff. 단순 `7z e`는 `build.prop` 파일명이 겹쳐 덮어써지므로 부정확|
|prop 기능 확인|디컴파일 소스에서 `SystemProperties.get("...")` 사용처를 역추적 (교육모드·SafeURL·웹제한·데이터모니터·밝기 파라미터 등)|
|SystemUI|`ZuiSystemUI.apk` 덱스 strings 확인 — 명시적 지역 분기는 없고 Settings가 주된 게이팅 지점|

임시 분석 산출물(재부팅/정리 전까지 유지):
```
/private/var/folders/nh/wd2l8gzn2jz86xy90hx4lxpr0000gn/T/opencode/
├── romlists/          # 파티션별 파일 목록
├── apks/              # 추출한 ZuiSettings.apk + jadx 결과
├── propdump/          # build.prop 덤프 (초기)
├── allprops/          # 전체 *.prop (평탄화 추출 — 파일명 충돌 있음)
├── allprops2/         # 전체 *.prop (경로 보존 재추출 — 정확한 버전)
├── systemui/          # ZuiSystemUI.apk
├── global_region_context.txt
├── prc_region_context.txt
├── availability_diff.json
├── region_classification.txt
├── region_methods.json
├── global.allprops2.txt / prc.allprops2.txt
└── analyze_availability.py / check_classes.py / classify_regions.py
```
