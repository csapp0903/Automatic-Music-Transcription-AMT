# 构建 APK 指南

由于当前环境没有 Android SDK，无法直接构建 APK。请按照以下步骤在您的本地环境中构建：

## 环境要求

1. **安装 Android Studio**
   - 下载地址: https://developer.android.com/studio
   - 或使用阿里云镜像: https://developer.android.google.cn/studio

2. **安装 JDK 17**
   ```bash
   # Ubuntu/Debian
   sudo apt install openjdk-17-jdk

   # macOS
   brew install openjdk@17

   # Windows
   # 下载并安装 Oracle JDK 17
   ```

3. **配置 Android SDK**
   - 打开 Android Studio
   - Tools -> SDK Manager
   - 安装以下组件：
     - Android SDK Platform 34
     - Android SDK Build-Tools 34.0.0
     - Android SDK Platform-Tools
     - Android SDK Tools

## 构建步骤

### 方法一：使用 Android Studio（推荐）

1. 打开 Android Studio
2. File -> Open -> 选择项目目录
3. 等待 Gradle 同步完成
4. 点击菜单 Build -> Build Bundle(s) / APK(s) -> Build APK(s)
5. 构建完成后，APK 位于: `app/build/outputs/apk/debug/app-debug.apk`

### 方法二：使用命令行

1. 克隆项目
```bash
git clone https://github.com/csapp0903/Automatic-Music-Transcription-AMT.git
cd Automatic-Music-Transcription-AMT
```

2. 赋予执行权限
```bash
chmod +x gradlew
```

3. 构建 Debug APK
```bash
./gradlew assembleDebug
```

4. 构建 Release APK（需要签名）
```bash
./gradlew assembleRelease
```

## 常见构建错误及解决方案

### 1. 缺少 Android SDK

**错误信息**:
```
SDK location not found. Define location with sdk.dir in the local.properties file
```

**解决方案**:
创建 `local.properties` 文件（如果不存在）：
```properties
sdk.dir=/path/to/your/android-sdk
```

### 2. Gradle 下载失败

**错误信息**:
```
Could not download gradle-8.2-bin.zip
```

**解决方案**:
- 方法1: 使用 VPN 或代理
- 方法2: 手动下载 Gradle 并解压到 `~/.gradle/wrapper/dists/`
- 方法3: 使用 Android Studio 的 Gradle（它会自动处理）

### 3. 依赖下载失败

**错误信息**:
```
Could not resolve com.android.tools.build:gradle:8.2.0
```

**解决方案**:
项目已配置阿里云镜像。如果仍然失败：
```bash
# 清除缓存重试
./gradlew clean --refresh-dependencies
```

### 4. 内存不足

**错误信息**:
```
Expiring Daemon because JVM heap space is exhausted
```

**解决方案**:
编辑 `gradle.properties`，增加内存：
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### 5. 构建工具版本不匹配

**错误信息**:
```
Installed Build Tools revision 33.0.0 is corrupted
```

**解决方案**:
在 Android Studio 的 SDK Manager 中重新安装 Build Tools 34.0.0

## 输出文件位置

构建成功后，APK 文件位于：

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

## 安装到设备

### 使用 ADB 安装
```bash
# 连接设备或启动模拟器
adb devices

# 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 或使用 Gradle 任务
./gradlew installDebug
```

### 直接在设备上安装
1. 将 APK 文件复制到 Android 设备
2. 在文件管理器中点击 APK 文件
3. 允许从未知来源安装（如需要）
4. 点击安装

## Release 版本签名

创建 Release 版本需要签名密钥：

1. 生成密钥库
```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

2. 配置签名（在 `app/build.gradle.kts` 中）
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("my-release-key.jks")
            storePassword = "your-password"
            keyAlias = "my-key-alias"
            keyPassword = "your-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

3. 构建签名的 Release APK
```bash
./gradlew assembleRelease
```

## 项目完整性检查

在构建前，确保以下文件存在：

- [x] `build.gradle.kts` - 根级构建配置
- [x] `settings.gradle.kts` - 项目设置
- [x] `app/build.gradle.kts` - 应用级构建配置
- [x] `app/src/main/AndroidManifest.xml` - 应用清单
- [x] `app/src/main/java/` - Kotlin 源码
- [x] `app/src/main/res/` - 资源文件
- [x] `gradle/wrapper/` - Gradle Wrapper
- [x] `gradlew` 和 `gradlew.bat` - Gradle 包装脚本

## 获取帮助

如果遇到其他构建问题：
1. 查看完整的构建日志: `./gradlew assembleDebug --stacktrace`
2. 在 GitHub Issues 中提问
3. 参考 [Android 官方文档](https://developer.android.com/studio/build)
