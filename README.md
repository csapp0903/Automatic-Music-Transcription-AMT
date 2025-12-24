# 音乐转谱 (Music Transcription App)

一个功能强大的 Android 应用，可以将音频文件自动转换为曲谱，并支持多种格式导出。

## 功能特点

- 📁 **多种音频输入方式**
  - 从本地文件系统选择音频文件
  - 通过 URL 下载在线音频
  - 支持常见音频格式：MP3, WAV, M4A, FLAC, OGG

- 🎵 **智能音频转谱**
  - 自动检测音频中的音符
  - 识别音高、时长和力度
  - 生成完整的音乐曲谱

- 💾 **多格式导出**
  - **MIDI (.mid)**: 标准 MIDI 格式，可在任何 MIDI 播放器和 DAW 中使用
  - **MusicXML (.xml)**: 通用的曲谱交换格式，可在 MuseScore、Finale 等软件中编辑
  - **PDF (.pdf)**: 可打印的曲谱文档，包含五线谱表示和音符表格

## 技术栈

- **开发语言**: Kotlin
- **UI 框架**: Jetpack Compose (Material Design 3)
- **架构**: MVVM (Model-View-ViewModel)
- **异步处理**: Kotlin Coroutines & Flow
- **主要依赖库**:
  - OkHttp: 网络请求和文件下载
  - Mobile FFmpeg: 音频格式转换
  - Leff MIDI: MIDI 文件生成
  - iText 7: PDF 文档生成

## 项目结构

```
app/src/main/java/com/musictranscription/app/
├── model/              # 数据模型
│   ├── Note.kt        # 音符数据类
│   └── MusicScore.kt  # 曲谱数据类
├── service/           # 核心服务
│   ├── AudioDownloader.kt    # 音频下载服务
│   └── AudioTranscriber.kt   # 音频转谱服务
├── export/            # 导出功能
│   ├── MidiExporter.kt       # MIDI 导出
│   ├── MusicXmlExporter.kt   # MusicXML 导出
│   └── PdfExporter.kt        # PDF 导出
├── viewmodel/         # 视图模型
│   └── MainViewModel.kt      # 主视图模型
├── ui/                # 用户界面
│   ├── MainScreen.kt         # 主界面
│   └── theme/               # 主题配置
└── MainActivity.kt    # 主活动
```

## 构建和运行

### 环境要求

- Android Studio Arctic Fox 或更高版本
- JDK 17
- Android SDK API 26 (Android 8.0) 或更高
- Gradle 8.2+

### 构建步骤

1. 克隆仓库
```bash
git clone https://github.com/csapp0903/Automatic-Music-Transcription-AMT.git
cd Automatic-Music-Transcription-AMT
```

2. 在 Android Studio 中打开项目

3. 同步 Gradle 依赖
```bash
./gradlew sync
```

4. 构建 APK
```bash
./gradlew assembleDebug
```

5. 安装到设备
```bash
./gradlew installDebug
```

或直接在 Android Studio 中点击 "Run" 按钮。

## 使用说明

1. **选择音频源**
   - 点击"选择音频文件"从设备中选择音频文件
   - 或输入音频 URL，点击"下载音频"

2. **转换曲谱**
   - 音频加载完成后，点击"开始转换"
   - 等待转换完成，应用会显示检测到的音符数量

3. **导出结果**
   - 选择所需格式（MIDI、MusicXML 或 PDF）
   - 点击对应的导出按钮
   - 导出的文件保存在应用的 exports 目录中

## 导出文件位置

导出的文件保存在以下目录：
```
/Android/data/com.musictranscription.app/files/exports/
```

您可以通过文件管理器访问这些文件，或使用任何支持相应格式的应用打开。

## 权限说明

应用需要以下权限：
- **INTERNET**: 从 URL 下载音频文件
- **READ_EXTERNAL_STORAGE / READ_MEDIA_AUDIO**: 读取本地音频文件
- **WRITE_EXTERNAL_STORAGE**: 保存导出的文件（Android 9 及以下）

## 关于音频转谱算法

当前版本使用简化的音频分析算法作为演示。在生产环境中，建议集成以下专业的 AMT（Automatic Music Transcription）模型：

- **Basic Pitch** (Spotify): 开源的音频转 MIDI 模型
- **Onsets and Frames**: 适用于钢琴音乐转录
- **MT3** (Google Magenta): 多音轨音乐转录

您可以在 `AudioTranscriber.kt` 中替换分析算法。

## 改进建议

- [ ] 集成真实的机器学习模型（如 Basic Pitch）
- [ ] 添加实时音频录制功能
- [ ] 支持更多导出格式（如 ABC notation）
- [ ] 优化 PDF 输出的五线谱渲染
- [ ] 添加曲谱预览功能
- [ ] 支持多轨道音频分析
- [ ] 添加音符编辑功能

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，请通过 GitHub Issues 联系。
