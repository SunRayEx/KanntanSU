# KernelSU Manager UI 架构分析文档

本文档详细分析 `manager/app/src/main/java/me/weishu/kernelsu/ui` 目录下的 UI 布局、动画、功能实现及文件对应关系。

---

## 一、整体架构概览

KernelSU Manager 采用 **MVVM 架构 + 双主题路由模式**：

```
ui/
├── MainActivity.kt          # 主入口 Activity
├── UiMode.kt               # UI 模式枚举 (Material/Miuix)
├── component/              # 可复用 UI 组件
├── screen/                 # 页面/屏幕
├── theme/                  # 主题配置
├── viewmodel/             # ViewModel 层
├── navigation3/           # 自定义导航系统
├── util/                  # 工具类
└── webui/                 # WebUI 模块
```

### 双主题路由模式

每个屏幕都采用三层文件结构：
- `*Screen.kt` - 路由入口，根据 `LocalUiMode.current` 路由到具体实现
- `*Material.kt` - Material Design 3 风格实现
- `*Miuix.kt` - Miuix 风格实现

---

## 二、页面/屏幕 (screen/)

### 2.1 首页模块 (home/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `HomeScreen.kt` | 路由入口，状态管理 | 根据 UiMode 路由 |
| `HomeMaterial.kt` | Material3 风格实现 | `LargeFlexibleTopAppBar` + `exitUntilCollapsedScrollBehavior` 大标题折叠滚动 |
| `HomeMiuix.kt` | Miuix 风格实现 | `TopAppBar` + `MiuixScrollBehavior` 模糊滚动效果 |
| `HomeUiState.kt` | 页面状态数据类 | - |
| `HomeUtils.kt` | 工具函数 | - |

**功能**：
- 显示 KernelSU 状态（已激活/未安装/不支持）
- 展示系统信息（内核版本、Manager版本、指纹、SEAndroid/Seccomp状态）
- Superuser 和 Module 计数快捷入口
- 更新检查、捐赠、学习更多链接

---

### 2.2 设置模块 (settings/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `SettingsScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `SettingsMaterial.kt` | Material 风格 | `SegmentedColumn` 分组、`SegmentedSwitchItem` 开关 |
| `SettingsMiuix.kt` | Miuix 风格 | `SwitchPreference`、`ArrowPreference`、`OverlayDropdownPreference` |
| `SettingsUiState.kt` | 页面状态 | - |
| `SettingsMaterial.kt` | Material 风格实现 | - |

**功能**：
- 更新检查开关
- UI 模式切换（Miuix/Material）
- 主题设置入口
- Root 策略配置（sucompat模式、内核卸载、Sulog、ADB Root）
- Web调试开关、自动越狱
- 卸载、发送日志、关于页面

---

### 2.3 模块管理模块 (module/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `ModuleScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `ModuleMaterial.kt` | Material 风格 | `TonalCard` 卡片、列表布局 |
| `ModuleMiuix.kt` | Miuix 风格 | `Card` + 列表组件 |
| `ModuleUiState.kt` | 模块列表状态 | - |
| `ModuleShortcutState.kt` | 快捷方式状态 | - |

**功能**：
- 已安装模块列表展示
- 搜索过滤模块
- 排序选项（Action优先）
- 模块启用/禁用
- 模块卸载/撤销卸载
- WebUI 入口
- 快捷方式支持

---

### 2.4 超级用户模块 (superuser/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `SuperUserScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `SuperUserMaterial.kt` | Material 风格 | `TonalCard` 卡片列表 |
| `SuperUserMiuix.kt` | Miuix 风格 | `Card` + 列表组件 |
| `SuperUserUiState.kt` | 超级用户状态 | - |

**功能**：
- 已授权应用列表（按 UID 分组）
- 应用图标和名称显示
- 搜索功能（支持拼音匹配）
- 应用详情入口

---

### 2.5 模块仓库模块 (modulerepo/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `ModuleRepoScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `ModuleRepoMaterial.kt` | Material 风格 | 列表 + 图片加载 |
| `ModuleRepoMiuix.kt` | Miuix 风格 | 列表组件 |
| `ModuleRepoModels.kt` | 数据模型 | - |
| `ModuleRepoUiState.kt` | 页面状态 | - |

**功能**：
- 远程模块仓库浏览
- 搜索功能
- 模块信息展示（名称、版本、作者）
- 模块下载（集成 DownloadService）

---

### 2.6 SU 日志模块 (sulog/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `SulogScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `SulogMaterial.kt` | Material 风格 | 列表 + 搜索栏 |
| `SulogMiuix.kt` | Miuix 风格 | 列表组件 |
| `SulogUiState.kt` | 日志状态 | - |

**功能**：
- SU 请求日志查看
- 日志筛选和搜索
- 事件类型过滤
- 时间范围选择

---

### 2.7 应用配置模块 (appprofile/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `AppProfileScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `AppProfileMaterial.kt` | Material 风格 | 表单布局 |
| `AppProfileMiuix.kt` | Miuix 风格 | 表单组件 |
| `AppProfileUiState.kt` | 配置状态 | - |
| `AppProfileUtils.kt` | 工具函数 | - |

**功能**：
- 应用 SU 权限配置
- 模板选择
- SELinux 策略配置
- 根选项配置

---

### 2.8 安装向导模块 (install/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `InstallScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `InstallMaterial.kt` | Material 风格 | 向导式布局 |
| `InstallMiuix.kt` | Miuix 风格 | 向导组件 |
| `InstallUiState.kt` | 安装状态 | - |
| `InstallUtils.kt` | 工具函数 | - |

**功能**：
- KernelSU 安装向导
- 选择安装方式
- LKM 模块选择
- 分区选择
- 安装进度显示

---

### 2.9 刷机日志模块 (flash/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `FlashScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `FlashMaterial.kt` | Material 风格 | 日志显示 |
| `FlashMiuix.kt` | Miuix 风格 | 日志组件 |
| `FlashUiState.kt` | 日志状态 | - |
| `FlashUtils.kt` | 工具函数 | - |

**功能**：
- 刷机日志显示
- 重启操作
- 越狱警告信息

---

### 2.10 模板管理模块 (template/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `TemplateScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `TemplateMaterial.kt` | Material 风格 | 列表布局 |
| `TemplateMiuix.kt` | Miuix 风格 | 列表组件 |
| `TemplateUiState.kt` | 模板状态 | - |

**功能**：
- 应用配置模板管理
- 模板导入/导出
- 创建新模板
- 模板列表展示

---

### 2.11 模板编辑器模块 (templateeditor/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `TemplateEditorScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `TemplateEditorMaterial.kt` | Material 风格 | 表单编辑 |
| `TemplateEditorMiuix.kt` | Miuix 风格 | 编辑组件 |
| `TemplateEditorUiState.kt` | 编辑状态 | - |

**功能**：
- 模板 ID 编辑
- 模板名称编辑
- 规则配置

---

### 2.12 关于模块 (about/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `AboutScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `AboutMaterial.kt` | Material 风格 | 卡片布局 |
| `AboutMiuix.kt` | Miuix 风格 | 卡片组件 |
| `AboutUiState.kt` | 关于状态 | - |
| `AboutUtils.kt` | 工具函数 | - |

**功能**：
- 应用版本信息
- GitHub/Telegram 链接
- 致谢信息

---

### 2.13 颜色调色板模块 (colorpalette/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `ColorPaletteScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `ColorPaletteScreenMaterial.kt` | Material 风格 | 颜色选择器 |
| `ColorPaletteScreenMiuix.kt` | Miuix 风格 | 颜色组件 |
| `ColorPaletteUiState.kt` | 颜色状态 | - |

**功能**：
- 颜色模式选择
- 色调风格配置
- 模糊效果配置
- 圆角配置

---

### 2.14 模块动作执行模块 (executemoduleaction/)

| 文件 | 功能 | 动画/布局特点 |
|------|------|--------------|
| `ExecuteModuleActionScreen.kt` | 路由入口 | 根据 UiMode 路由 |
| `ExecuteModuleActionMaterial.kt` | Material 风格 | 日志显示 |
| `ExecuteModuleActionMiuix.kt` | Miuix 风格 | 日志组件 |
| `ExecuteModuleActionUiState.kt` | 执行状态 | - |
| `ExecuteModuleActionUtils.kt` | 工具函数 | - |

**功能**：
- 模块动作执行
- 执行日志显示

---

## 三、UI 组件 (component/)

### 3.1 核心组件

| 文件 | 功能 | UI布局 | 动画实现 |
|------|------|--------|---------|
| `AppIconImage.kt` | 应用图标加载与缓存 | Box + Image, 48dp固定大小 | Crossfade 淡入过渡 (150ms) |
| `FloatingBottomBar.kt` | 浮动底部导航栏 | Row + FloatingBottomBarItem, 高度64dp | 阻尼拖动动画、InteractiveHighlight、Backdrop模糊效果 |
| `GithubMarkdown.kt` | GitHub风格Markdown渲染 | AndroidView + WebView | CSS内部动画 |
| `KsuValidCheck.kt` | 条件渲染组件 | 无可见UI，纯逻辑包装 | 无 |
| `Markdown.kt` | Markdown文本渲染 | AndroidView + ScrollView + TextView | 无 |
| `SearchStatus.kt` | 搜索状态管理 | TopAppBarAnim包装器 | alpha透明度动画 |
| `MenuPositionProvider.kt` | 菜单位置提供器 | 无可见UI | 无 |

### 3.2 底部导航栏组件 (bottombar/)

| 文件 | 功能 | 动画特点 |
|------|------|---------|
| `BottomBar.kt` | 底部导航栏工厂 | 页面切换动画: EaseInOut缓动, 时长=100*距离+100ms |
| `BottomBarMaterial.kt` | Material 风格实现 | 滚动淡出效果 |
| `BottomBarMiuix.kt` | Miuix 风格实现 | 平滑切换动画 |
| `NavigationRailMaterial.kt` | Material 导航轨道 | 侧边导航 |
| `NavigationRailMiuix.kt` | Miuix 导航轨道 | 侧边导航 |

### 3.3 对话框组件 (dialog/)

| 文件 | 功能 |
|------|------|
| `Dialog.kt` | 对话框工厂入口 |
| `DialogMaterial.kt` | Material 风格对话框 |
| `DialogMiuix.kt` | Miuix 风格对话框 |

### 3.4 选择KMI对话框 (choosekmidialog/)

| 文件 | 功能 |
|------|------|
| `ChooseKmiDialog.kt` | KMI选择对话框入口 |
| `ChooseKmiDialogMaterial.kt` | Material 风格KMI对话框 |
| `ChooseKmiDialogMiuix.kt` | Miuix 风格KMI对话框 |

### 3.5 Material 设计组件 (material/)

| 文件 | 功能 | UI布局/动画 |
|------|------|------------|
| `ExpressiveSwitch.kt` | 表达性开关 | 特殊开关样式 |
| `SearchBar.kt` | 搜索栏 | 搜索输入框 |
| `SegmentedList.kt` | 分段列表 | 分段选择列表 |
| `SendLogBottomSheet.kt` | 发送日志底部表单 | BottomSheet + 表单 |
| `SettingsItem.kt` | 设置项组件 | 设置项布局 |
| `TonalCard.kt` | 色调卡片 | 卡片容器 |

### 3.6 Miuix 组件 (miuix/)

| 文件 | 功能 |
|------|------|
| `DropdownItem.kt` | 下拉选项项 |
| `EditText.kt` | 编辑文本框 |
| `ScaleDialog.kt` | 缩放对话框 |
| `SendLogDialog.kt` | 发送日志对话框 |
| `SuperEditArrow.kt` | 超级编辑箭头 |
| `SuperSearchBar.kt` | 超级搜索栏 |
| `WarningCard.kt` | 警告卡片 |
| `animation/` | Miuix 动画辅助 | - |

### 3.7 状态标签组件 (statustag/)

| 文件 | 功能 |
|------|------|
| `StatusTag.kt` | 状态标签入口 |
| `StatusTagMaterial.kt` | Material 风格状态标签 |
| `StatusTagMiuix.kt` | Miuix 风格状态标签 |

### 3.8 应用配置组件 (profile/)

| 文件 | 功能 |
|------|------|
| `AppProfileConfigMaterial.kt` | Material 应用配置 |
| `AppProfileConfigMiuix.kt` | Miuix 应用配置 |
| `ProfileConfig.kt` | 配置基础类 |
| `RootProfileConfigMaterial.kt` | Material 根配置 |
| `RootProfileConfigMiuix.kt` | Miuix 根配置 |
| `TemplateConfigMaterial.kt` | Material 模板配置 |
| `TemplateConfigMiuix.kt` | Miuix 模板配置 |
| `dialogs/` | 配置对话框 | - |

### 3.9 重启选项弹出框 (rebootlistpopup/)

| 文件 | 功能 |
|------|------|
| `RebootListPopup.kt` | 重启选项弹出框入口 |
| `RebootListPopupMaterial.kt` | Material 风格 |
| `RebootListPopupMiuix.kt` | Miuix 风格 |

### 3.10 卸载对话框 (uninstalldialog/)

| 文件 | 功能 |
|------|------|
| `UninstallDialog.kt` | 卸载对话框入口 |
| `UninstallDialogMaterial.kt` | Material 风格 |
| `UninstallDialogMiuix.kt` | Miuix 风格 |

### 3.11 过滤器组件 (filter/)

| 文件 | 功能 |
|------|------|
| `BaseFieldFilter.kt` | 基础字段过滤器 |
| `FilterNumber.kt` | 数字过滤器 |

### 3.12 其他组件

| 文件 | 功能 |
|------|------|
| `KeyEventBlocker.kt` | 键盘事件拦截器 |
| `Component.kt` | 组件基类 |

---

## 四、主题系统 (theme/)

### 4.1 主题文件

| 文件 | 功能 |
|------|------|
| `MaterialTheme.kt` | Material Design 3 主题配置 |
| `MiuixTheme.kt` | Miuix 主题配置 |
| `Theme.kt` | 通用主题配置 |
| `Colors.kt` | 颜色定义 |

### 4.2 主题架构

采用双主题模式：
- **MaterialTheme**: 基于 Material Design 3 的组件和颜色系统
- **MiuixTheme**: 基于 Miuix 定制组件库的风格

主题支持：
- 颜色模式（Light/Dark/System）
- 动态颜色（Android 12+）
- 模糊效果配置
- 圆角配置

---

## 五、ViewModel 层 (viewmodel/)

### 5.1 ViewModel 列表

| 文件 | 功能 | 状态管理 |
|------|------|---------|
| `HomeViewModel.kt` | 首页状态管理 | 内核版本、模块数、超级用户数、版本检查 |
| `MainActivityViewModel.kt` | 全局应用设置 | UI模式、颜色模式、模糊效果等 |
| `ModuleViewModel.kt` | 模块管理 | 模块列表、搜索、排序、更新检查 |
| `SettingsViewModel.kt` | 设置管理 | Root策略、功能开关 |
| `SuperUserViewModel.kt` | 超级用户管理 | 应用列表、UID分组、搜索 |
| `ModuleRepoViewModel.kt` | 模块仓库 | 远程模块列表、下载状态 |
| `SulogViewModel.kt` | SU日志 | 日志列表、筛选条件 |
| `SearchViewModelHelper.kt` | 搜索辅助 | 搜索状态管理 |
| `TemplateViewModel.kt` | 模板管理 | 模板列表、CRUD操作 |

### 5.2 状态管理模式

使用 Kotlin Coroutines + StateFlow：
- `MutableStateFlow` 内部状态
- `StateFlow` 对外暴露
- `Mutex` 保证线程安全
- `Job` 控制协程生命周期

---

## 六、导航系统 (navigation3/)

### 6.1 导航文件

| 文件 | 功能 |
|------|------|
| `Navigator.kt` | 自定义导航控制器 |
| `Routes.kt` | 路由定义 |
| `DeepLinkResolver.kt` | DeepLink 解析 |

### 6.2 Navigator 实现特点

- 基于 `SnapshotStateList` 的自定义 backStack
- 支持操作：`push()`, `replace()`, `replaceAll()`, `pop()`, `popUntil()`
- 结果返回机制通过协程 `CompletableDeferred`
- 避免使用 Compose Navigation 的局限性

---

## 七、WebUI 模块 (webui/)

| 文件 | 功能 |
|------|------|
| `WebUIScreen.kt` | WebUI 路由入口 |
| `WebUIMaterial.kt` | Material 风格 WebUI |
| `WebUIMiuix.kt` | Miuix 风格 WebUI |
| `WebUIState.kt` | WebUI 状态 |
| `WebViewHelper.kt` | WebView 辅助工具 |
| `WebViewInterface.kt` | WebView 接口 |
| `AppIconUtil.kt` | 图标工具 |
| `Insets.kt` | 内边距处理 |
| `MimeUtil.java` | MIME 类型工具 |
| `MonetColorsProvider.kt` | 动态颜色提供器 |
| `SuFilePathHandler.java` | 文件路径处理 |
| `WebUIActivity.kt` | WebUI Activity |

---

## 八、工具类 (util/)

### 8.1 下载相关

| 文件 | 功能 |
|------|------|
| `Downloader.kt` | 下载函数封装，支持进度回调 |
| `DownloadManager.kt` | 下载状态管理 (Pending/Downloading/Completed/Failed) |
| `DownloadService.kt` | 前台服务执行下载，显示进度通知 |

### 8.2 核心 CLI 封装

| 文件 | 功能 |
|------|------|
| `KsuCli.kt` | 与 KernelSU 守护进程交互的所有 Shell 命令 |

**主要功能**：
- 模块管理（listModules、toggleModule、uninstallModule）
- Boot 安装（installBoot、restoreBoot）
- Reboot 操作（soft_reboot、recovery）
- 特性检查（getFeatureStatus、getSepolicy）
- 应用配置模板管理

### 8.3 UI 视觉效果

| 文件 | 功能 |
|------|------|
| `BlurExt.kt` | 毛玻璃效果背景 (rememberBlurBackdrop、BlurredBar) |
| `Colors.kt` | 颜色工具（ARGB↔CSS、颜色混合、HSL、对比度） |

### 8.4 Compose 辅助

| 文件 | 功能 |
|------|------|
| `CompositionProvider.kt` | 定义 LocalSnackbarHost CompositionLocal |
| `DeferredContent.kt` | rememberContentReady() 确保内容准备就绪 |

### 8.5 其他工具

| 文件 | 功能 |
|------|------|
| `AppIconCache.kt` | 应用图标缓存 |
| `LogEvent.kt` | 日志事件 |
| `Network.kt` | 网络请求 |
| `Serialization.kt` | 序列化工具 |
| `SulogHelper.kt` | SU日志辅助 |
| `UidGroupUtils.kt` | UID分组工具 |
| `OemHelper.kt` | OEM辅助 |
| `SELinuxChecker.kt` | SELinux检查 |
| `HanziToPinyin.java` | 汉字转拼音 |
| `module/` | 模块相关工具 |

---

## 九、核心入口文件

| 文件 | 功能 |
|------|------|
| `MainActivity.kt` | 主入口 Activity，应用生命周期管理 |
| `KsuService.kt` | KernelSU 服务相关 |
| `UiMode.kt` | UI 模式枚举 (Material/Miuix) |

---

## 十、动画系统总结

### 10.1 页面切换动画

- **缓动函数**: `EaseInOut`
- **动画时长**: `100 * 距离 + 100ms`（基于页面距离）

### 10.2 滚动动画

- **Material**: `exitUntilCollapsedScrollBehavior` 大标题折叠
- **Miuix**: `MiuixScrollBehavior` 模糊滚动效果

### 10.3 浮动底部栏动画

- 阻尼拖动动画 (DampedDragAnimation)
- InteractiveHighlight 交互高亮
- Backdrop 效果：blur(8dp)、lens(24dp)、vibrancy

### 10.4 图标动画

- Crossfade 淡入过渡 (150ms)

---

## 十一、架构特点总结

1. **双主题路由模式**: 每个页面都有 Material 和 Miuix 两套实现
2. **MVVM 架构**: ViewModel + StateFlow 管理状态
3. **自定义导航**: 基于 SnapshotStateList 的导航栈
4. **组件化设计**: 高度可复用的 UI 组件
5. **动画丰富**: 页面切换、滚动、拖动等多种动画效果
6. **主题支持**: Material 3 和 Miuix 双主题系统

---

## 十二、KanntanSU 特有 UI 代码 (com.kanntan.su)

KanntanSU 是基于 KernelSU 的定制版本，在 `com.kanntan.su` 包下有特有的 UI 实现，采用简洁的 Compose 单文件设计。

### 12.1 目录结构

```
com/kanntan/su/
├── KernelSUApplication.kt   # 全局应用类
├── MainActivity.kt          # 主入口 Activity
├── Natives.kt              # JNI 绑定（已废弃）
└── ui/
    ├── screen/
    │   └── home/
    │       ├── HomeScreen.kt      # 主界面 UI（核心文件，609行）
    │       ├── HomeViewModel.kt   # 首页 ViewModel
    │       └── SystemInfo.kt      # 系统信息数据类
    └── theme/
        └── Theme.kt       # KanntanSU 主题配置
```

### 12.2 核心文件分析

#### KernelSUApplication.kt - 全局应用类

| 属性 | 说明 |
|------|------|
| `ksuApp` | 全局 lateinit var 单例，供整个应用访问 Android Context |
| 功能 | 依赖注入模式，通过全局变量暴露 Context |

**与其他模块交互**：被 Natives.kt、ViewModels、Repository 等模块使用

---

#### MainActivity.kt - 应用入口

**功能**：
- 作为 KanntanSU 的主 Activity
- 启用 Jetpack Compose
- 实现全屏沉浸模式（无状态栏和导航栏）
- 使用 `enableEdgeToEdge()` 支持现代 Android 边缘到边缘显示

**UI 布局**：
```
KanntanSUTheme (lightTheme=false)
└── Surface (fillMaxSize, transparent)
    └── HomeScreen (viewModel, actions)
```

**与其他模块交互**：
- 使用 `viewModel()` 创建 HomeViewModel
- 通过 HomeActions 回调处理导航
- 最终调用 `me.weishu.kernelsu.ui.MainActivity` 的实际实现

---

#### Natives.kt - JNI 绑定

| 状态 | 说明 |
|------|------|
| **已废弃** | 占位文件，实际 JNI 功能由 `me.weishu.kernelsu.Natives` 提供 |
| 建议 | 直接使用上游的 Natives 类以保持兼容性 |

---

#### HomeScreen.kt - 主界面 UI（核心文件）

**UI 布局结构**：
```
HomeScreen
├── Column (fillMaxSize, ContentBackground)
│   ├── HeaderArea (280dp, 黑底白块)
│   │   ├── Canvas (绘制大白块 96dp + 小白块 48dp)
│   │   └── Column (文字：KernelSU / is / Not Ready)
│   ├── HeaderGradientStrip (48dp, 24条渐变横线)
│   ├── ContentArea (白底，系统信息)
│   │   └── Column (InfoRowRightAligned × 4)
│   └── Foo...ter (底部栏)
```

**功能特点**：
- 黑白极简设计风格
- Canvas 自定义绘制图形
- 渐变色条装饰
- 系统信息展示（内核版本、Android 版本、指纹）
- 自定义底部导航栏（Home/Modules/Install）
- 全屏沉浸模式，无状态栏

**动画特点**：
- 底部栏圆角动画：`animateFloatAsState` 0→16dp
- 底部栏透明度：`animateFloatAsState` 0.8→1.0
- 渐变色条：`canvas.drawLine` 24 条横线

---

#### HomeViewModel.kt - 首页 ViewModel

| 功能 | 说明 |
|------|------|
| 状态管理 | 使用 StateFlow 管理 UI 状态 |
| 系统信息 | 加载内核版本、Android 版本、指纹等 |
| KSU 检测 | 检测 KernelSU 是否安装 |
| 模块列表 | 获取已安装模块信息 |
| 操作处理 | 处理安装、模块管理导航等 |

**与上游差异**：KanntanSU 简化了 ViewModel，未使用双主题路由

---

#### SystemInfo.kt - 系统信息数据类

```kotlin
data class SystemInfo(
    val ksuVersion: String,      // KSU 版本
    val kernelVersion: String,  // 内核版本
    val androidVersion: String,  // Android 版本
    val fingerprint: String,    // 设备指纹
    val moduleCount: Int,       // 模块数量
    val isKsuInstalled: Boolean // KSU 是否安装
)
```

---

#### Theme.kt - KanntanSU 主题配置

| 特点 | 说明 |
|------|------|
| 颜色方案 | 黑白极简风格 |
| 配色 | Black (#000000) 和 White (#FFFFFF) |
| 动态主题 | 支持 lightTheme 参数切换 |
| 字体 | 使用默认 Material 字体 |

---

### 12.3 与上游 KernelSU UI 的主要差异

| 特性 | KanntanSU | KernelSU Manager |
|------|-----------|-----------------|
| **主题系统** | 单一极简主题 | 双主题（Material/Miuix） |
| **文件结构** | 单文件组件 | 多文件分层（Screen/Material/Miuix） |
| **导航方式** | 回调函数 HomeActions | 自定义 Navigator + 路由 |
| **布局风格** | 黑白极简、Canvas 绘制 | Material Design 3 |
| **状态管理** | 简化 ViewModel | 完整 MVVM + Repository |
| **沉浸模式** | 全屏无状态栏 | 可配置状态栏 |

### 12.4 KanntanSU UI 设计亮点

1. **极简美学**：黑白配色，Canvas 自定义绘制大白块/小白块
2. **渐变装饰**：24 条渐变横线作为标题装饰
3. **全屏沉浸**：`enableEdgeToEdge()` + 透明背景实现真正全屏
4. **自定义底部栏**：使用 Canvas 绘制圆角矩形，无需系统导航
5. **轻量设计**：单文件组件，无复杂的主题路由系统

---
