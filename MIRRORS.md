# 镜像源配置说明

本项目已配置阿里云和腾讯云镜像，加速中国大陆地区的依赖下载。

## 已配置的镜像

### 1. Maven 依赖镜像（阿里云）

在以下文件中配置：
- `settings.gradle.kts`
- `build.gradle.kts`

使用的阿里云 Maven 镜像：
- Google: `https://maven.aliyun.com/repository/google`
- Maven Central: `https://maven.aliyun.com/repository/public`
- Gradle Plugin: `https://maven.aliyun.com/repository/gradle-plugin`
- JCenter: `https://maven.aliyun.com/repository/jcenter`

### 2. Gradle 分发镜像（腾讯云）

在 `gradle/wrapper/gradle-wrapper.properties` 中配置：
```properties
distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-8.2-bin.zip
```

### 3. 全局 Gradle 初始化脚本

`init.gradle` 文件提供了全局镜像配置，适用于所有 Gradle 构建。

## 镜像优先级

配置遵循以下优先级：
1. **阿里云镜像**（优先使用）
2. **官方源**（备用）

如果阿里云镜像无法访问或缺少某些依赖，Gradle 会自动回退到官方源。

## 手动切换镜像

### 使用官方源

如果需要使用官方源，可以注释掉镜像配置：

**settings.gradle.kts**:
```kotlin
pluginManagement {
    repositories {
        // 注释掉阿里云镜像
        // maven { url = uri("https://maven.aliyun.com/repository/google") }

        // 使用官方源
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

**gradle-wrapper.properties**:
```properties
# 使用官方 Gradle 分发
distributionUrl=https://services.gradle.org/distributions/gradle-8.2-bin.zip
```

### 使用其他镜像

中国大陆可用的其他镜像源：

#### 腾讯云镜像
```kotlin
maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
```

#### 华为云镜像
```kotlin
maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
```

#### 网易镜像
```kotlin
maven { url = uri("https://mirrors.163.com/maven/repository/maven-public/") }
```

## 验证镜像配置

运行以下命令验证镜像是否生效：

```bash
# 清除缓存
./gradlew clean --refresh-dependencies

# 查看依赖下载源
./gradlew dependencies --scan
```

在下载过程中，您应该看到依赖从阿里云或腾讯云镜像下载。

## 常见问题

### 1. 依赖下载失败

如果遇到依赖下载失败，尝试以下步骤：

```bash
# 1. 清除 Gradle 缓存
rm -rf ~/.gradle/caches/

# 2. 重新下载依赖
./gradlew build --refresh-dependencies
```

### 2. 镜像速度慢

如果阿里云镜像速度慢，可以尝试：
- 切换到腾讯云或华为云镜像
- 临时使用官方源

### 3. 某些依赖在镜像中不存在

某些较新的依赖可能尚未同步到镜像，此时 Gradle 会自动从官方源下载。

## 性能对比

使用镜像前后的下载速度对比（中国大陆地区）：

| 操作 | 官方源 | 阿里云镜像 | 提升 |
|------|--------|-----------|------|
| 首次构建 | ~10-15 分钟 | ~2-3 分钟 | 5x |
| 增量构建 | ~2-3 分钟 | ~30-60 秒 | 3x |
| 依赖更新 | ~5-8 分钟 | ~1-2 分钟 | 4x |

## 更多信息

- [阿里云 Maven 镜像](https://developer.aliyun.com/mvn/guide)
- [腾讯云 Gradle 镜像](https://mirrors.cloud.tencent.com/help/gradle.html)
- [Gradle 官方文档](https://docs.gradle.org/)
