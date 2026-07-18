# KanntanSU Integration Guide

## Overview

Complete implementation guide for integrating the KanntanSU black-white minimalist UI into an existing KernelSU manager fork.

---

## Files Created

| File | Package | Purpose |
|------|---------|---------|
| `Theme.kt` | `com.kanntan.su.ui.theme` | Pure black (#000000) & white (#FFFFFF) MD2 theme |
| `SystemInfo.kt` | `com.kanntan.su.ui.screen.home` | Data classes for real-time system info |
| `HomeViewModel.kt` | `com.kanntan.su.ui.screen.home` | StateFlow ViewModel with real-time ksud data |
| `HomeScreen.kt` | `com.kanntan.su.ui.screen.home` | Full composable with exact geometry |
| `MainActivity.kt` | `com.kanntan.su` | Entry point launcher |
| `Natives.kt` | `com.kanntan.su` | Stub redirecting to `me.weishu.kernelsu.Natives` |
| `KernelSUApplication.kt` | `com.kanntan.su` | Application class for `ksuApp` |

---

## Exact Geometry Specification

### Top Header (280dp height)
- **Solid black (#000000)** rectangle, fully tappable
- **Text layout** (positioned right of geometric pattern):
  - "KanntanSU" - 68sp bold white, at ~160dp from left, ~100dp from top
  - "is Ready" status - 38sp white, directly below "KanntanSU"
- **Geometric splicing** (top-left corner):
  - Large WHITE square at top-left corner of header (size: 40% of header height)
  - Small BLACK square at bottom-right of white square (35% of white square size)
  - White square is ON TOP

### Gradient Strip (below header, ~48dp)
- Vertical gradient: #1C1C1C to #333333

### Separator Bar (2dp)
- Horizontal gradient: #4A4A4A → #8A8A8A → #FFFFFF

### Content Area (white)
- Chinese bold labels + regular values
- Multi-line fingerprint support
- Real-time SELinux status

### Footer (bottom-right ONLY)
- **NO patterns in bottom-left** - pure white
- **Geometric pattern** (bottom-right corner):
  - Large WHITE square at bottom-right (75% of footer height)
  - Small BLACK square at top-left of white square (35% of white square size)
  - **Black square is clickable** - triggers floating menu

### Floating Menu (no background scrim)
- Pure floating Card - no overlay behind it
- Vertical Card with elevation
- 3 items: Modules, Application, Setting
- Icons: black square, upward triangle, circle

### Selected States
- **Modules**: Black rounded-square + white dot top-left
- **Application**: Black triangle + thicker outline + inner shadow
- **Setting**: Black outer circle + white inner ring

---

## Integration Steps

### 1. Copy Files to Your Project

Copy these files to your manager project under `com.kanntan.su` package:

```
manager/app/src/main/java/com/kanntan/su/
├── MainActivity.kt
├── KernelSUApplication.kt  
├── Natives.kt
└── ui/
    ├── theme/
    │   └── Theme.kt
    └── screen/
        └── home/
            ├── HomeScreen.kt
            ├── HomeViewModel.kt
            └── SystemInfo.kt
```

### 2. Update `me/weishu/kernelsu/ui/MainActivity.kt`

Add imports (around line 103-109):
```kotlin
import com.kanntan.su.ui.screen.home.HomeScreen
import com.kanntan.su.ui.screen.home.HomeViewModel
import com.kanntan.su.ui.screen.home.HomeActions
import com.kanntan.su.ui.theme.KanntanSUTheme
```

Change route entry (line ~187):
```kotlin
entry<Route.Main> { KanntanSUHomeScreen() }
```

Add KanntanSUHomeScreen function (at end of file):
```kotlin
@Composable
private fun KanntanSUHomeScreen() {
    val homeViewModel: HomeViewModel = viewModel()
    
    val homeActions = HomeActions(
        onInstallClick = {
            // Navigate to Flash screen
        },
        onModulesClick = {
            // Navigate to modules
        },
        onApplicationClick = {
            // Navigate to superuser apps
        },
        onSettingClick = {
            // Navigate to settings
        }
    )
    
    KanntanSUTheme(darkTheme = false, wallpaperEnabled = false) {
        HomeScreen(viewModel = homeViewModel, actions = homeActions)
    }
}
```

### 3. AndroidManifest.xml

**Option A: Change launcher activity (simple)**
```xml
<activity
    android:name="com.kanntan.su.MainActivity"
    android:exported="true"
    android:theme="@style/Theme.KernelSU">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

**Option B: Keep original activity (recommended)**
- Use `me.weishu.kernelsu.ui.MainActivity` as launcher
- It will call `KanntanSUHomeScreen()` for Route.Main

### 4. Update build.gradle.kts

```kotlin
android {
    namespace = "com.kanntan.su"  // Or keep "me.weishu.kernelsu"
}
```

---

## Wallpaper Replacement Guide

### Header Wallpaper

In `HomeScreen.kt`, find `HeaderArea()`:

```kotlin
// Replace:
.background(PureBlack)

// With:
.background(wallpaperBitmap?.asImageBitmap() ?: PureBlack)
```

Add import:
```kotlin
import androidx.compose.ui.graphics.asImageBitmap
```

### Footer Wallpaper

Same pattern in `FooterArea()`.

### Loading Wallpaper

```kotlin
@Composable
private fun loadWallpaperBitmap(): android.graphics.Bitmap? {
    val context = LocalContext.current
    // Load from SharedPreferences URI, ContentResolver, or image picker
    // Return resized Bitmap or null for fallback
}
```

Pass to HomeScreen:
```kotlin
HomeScreen(
    viewModel = viewModel(),
    actions = actions,
    wallpaperBitmap = loadWallpaperBitmap()
)
```

---

## Real-time Data Sources

| Data | Source | API |
|------|--------|-----|
| Kernel Version | Linux kernel | `Os.uname().release` |
| Manager Version | PackageManager | `getPackageInfo().versionName` |
| System Fingerprint | Build | `Build.FINGERPRINT` |
| SELinux Status | Shell | `getenforce` command |
| Seccomp Status | Kernel | `prctl(PR_GET_SECCOMP)` |
| KSU Version | JNI | `me.weishu.kernelsu.Natives.version` |
| Root Available | JNI | `me.weishu.kernelsu.Natives.isManager` |

---

## Build & Test

```bash
cd manager
./gradlew clean
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/KernelSU_v*-debug.apk
```

---

## File Structure Reference

```
manager/app/src/main/java/
├── com/kanntan/su/
│   ├── MainActivity.kt
│   ├── KernelSUApplication.kt
│   ├── Natives.kt
│   └── ui/
│       ├── theme/
│       │   └── Theme.kt
│       └── screen/
│           └── home/
│               ├── HomeScreen.kt
│               ├── HomeViewModel.kt
│               └── SystemInfo.kt
└── me/weishu/kernelsu/
    └── ui/
        └── MainActivity.kt  (modified to call KanntanSUHomeScreen)
```

---

## Troubleshooting

**UI not showing changes:**
- Run `./gradlew clean` - incremental builds may skip changes
- Clear app cache: `adb shell pm clear me.weishu.kernelsu`

**Compilation errors:**
- Ensure all imports are correct
- Verify package names match
- Check that `me.weishu.kernelsu.ksuApp` is accessible

**Natives errors:**
- `Natives.kt` redirects to `me.weishu.kernelsu.Natives`
- No native recompilation needed for JNI calls

---

*End of Integration Guide*
