# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android Todo application built with Kotlin and Jetpack Compose, following Material You (Material Design 3) design principles. Uses MVVM architecture with Room database for local storage.

## Build & Run Commands


### 本地开发机器执行

```bash
# Sync dependencies
./gradlew sync

# Build debug APK
./gradlew assembleDebug

# Run on connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest  # Instrumentation tests
```

### 服务器开发（仅限代码编辑）

```bash
# 查看项目结构
ls -la

# Git 操作
git status
git add .
git commit -m "message"
git push
```

**Requirements:**
- Android Studio Hedgehog+, JDK 17
- Min SDK 26, Target SDK 35

## Architecture

```
app/src/main/java/com/arttest/todo/
├── data/           # Room database, DAO, entities
├── ui/             # Compose UI (theme, components, screens)
├── viewmodel/      # TodoViewModel (state management)
└── MainActivity.kt # Entry point
```

**Key layers:**
- **Data layer**: `TodoDatabase` (Room singleton), `TodoDao` (queries as `Flow`), `TodoItem` (entity with Priority/Category enums)
- **ViewModel**: `TodoViewModel` - combines database flows, applies filtering/sorting, exposes `StateFlow<UiState>` and `StateFlow<FilterState>`
- **UI layer**: Single-activity architecture with Compose. `HomeScreen` displays todos, `EditTodoScreen` for editing, dialogs for add/edit

**State management:**
- Filtering logic in `applyFilter()` combines search query, filter type (ALL/ACTIVE/COMPLETED/HIGH_PRIORITY/DUE_SOON), and category
- Results sorted by completion status, priority, then due date

## Key Conventions

- All DAO queries return `Flow` for reactive updates
- ViewModel uses `viewModelScope.launch` for mutations
- Material You dynamic colors enabled for Android 12+ (`dynamicColor = true`)
- Manual DI via `ViewModelProvider` (no Hilt/Koin)
- Type converters for `Priority`, `Category`, `LocalDate`, `LocalDateTime`
