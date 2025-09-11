# ComposeStudyKit

🎯 **Jetpack Compose 学习案例集合**

一个基于 Jetpack Compose 的 Android 学习型项目，旨在帮助开发者掌握现代 Android 开发的最佳实践。

## 🚀 项目特性

### 🌐 网络请求框架
- **真实 API 调用**：使用 WanAndroid 开放接口
- **DataStore 缓存**：完整的缓存策略演示
- **并发性能测试**：独立并发处理，重组性能分析
- **基础网络框架**：Retrofit + OkHttp + Moshi 技术栈

### 🗄️ 全局 DataStore 工具
- **简便易懂的 API**：支持 String、Int、Boolean、Float、Long
- **响应式数据流**：数据变化时自动更新 UI
- **全局单例**：无需手动传入 Context
- **批量操作**：默认配置、数据导出等

## 🛠️ 技术栈

- **UI 框架**：Jetpack Compose
- **架构模式**：MVVM + Repository
- **网络请求**：Retrofit + OkHttp + Moshi
- **本地缓存**：DataStore Preferences
- **异步处理**：Kotlin Coroutines + Flow
- **开发语言**：Kotlin

## 📱 功能展示

### 主要模块
1. **首页导航**：清晰的案例分类和介绍
2. **网络请求案例**：4 种不同的网络请求演示
3. **DataStore 工具演示**：全局配置管理功能

### 学习重点
- ✅ **UIState 设计最佳实践**
- ✅ **并发请求处理优化**
- ✅ **重组性能分析**
- ✅ **类型安全的缓存实现**
- ✅ **响应式编程应用**

## 🔧 运行环境

- **JDK**：17+
- **Android Studio**：支持 Jetpack Compose
- **最低 SDK**：24 (Android 7.0)
- **目标 SDK**：36

## 📖 快速开始

```bash
# 克隆项目
git clone https://github.com/thisfeng/ComposeStudyKit.git

# 打开项目
# 使用 Android Studio 打开项目文件夹

# 运行项目
# 点击运行按钮或使用 Shift+F10
```

## 🎯 学习路径

1. **从首页开始**：了解项目结构和技术特性
2. **网络请求案例**：学习现代网络请求架构
3. **DataStore 工具**：掌握全局配置管理
4. **代码阅读**：深入理解实现细节

## 📋 项目结构

```
app/src/main/java/com/thisfeng/composestudykit/
├── ui/                     # UI 层
│   ├── screen/            # 界面
│   └── viewmodel/         # ViewModel
├── data/                  # 数据层
│   ├── api/              # API 接口
│   ├── model/            # 数据模型
│   └── repository/       # 仓库层
├── network/              # 网络框架
├── cache/                # 缓存策略
└── utils/                # 工具类
```

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 开源协议

MIT License

---

⭐ 如果这个项目对您有帮助，请给个 Star 支持一下！