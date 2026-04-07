# To Do App - Material You 风格记事本应用

一个使用现代 Android 技术栈开发的待办事项管理应用，采用 Material You (Material Design 3) 设计语言。

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架构**: MVVM
- **数据库**: Room
- **异步**: Kotlin Coroutines + Flow
- **依赖注入**: 手动 DI (ViewModelProvider)

## 核心功能

- ✅ 创建、编辑、删除待办事项
- ✅ 复选完成状态
- ✅ 优先级设置（高/中/低）
- ✅ 分类管理（工作/个人/购物/健康/学习/其他）
- ✅ 截止日期设置
- ✅ 搜索功能
- ✅ 多维度筛选（全部/未完成/已完成/高优先级/即将到期）
- ✅ 删除已完成事项
- ✅ 统计显示（未完成数/总数）
- ✅ 本地提醒通知（WorkManager 调度）
- ✅ 子任务管理
- ✅ 重复任务设置（每天/每周/每月/每年）
- ✅ 批量操作（批量完成/删除/标记未完成）
- ✅ 数据导出/导入（JSON 格式）
- ✅ 数据统计图表（完成趋势/分类分布/优先级分布）

## Material You 设计特性

- 🎨 动态色彩系统（支持 Android 12+ 动态取色）
- 🔘 Pill 形按钮和芯片
- 📐 大圆角卡片（24px+）
- 🌈 色调表面系统（tonal surfaces）
- ✨ 流畅的 cubic-bezier 动画
- 🎯  tactile 触觉反馈（active:scale-95）
- 💫 有机模糊背景装饰

## 项目结构

```
app/src/main/java/com/arttest/todo/
├── data/
│   ├── TodoItem.kt          # 数据实体和枚举
│   ├── TodoDao.kt           # 数据访问对象
│   └── TodoDatabase.kt      # Room 数据库
├── ui/
│   ├── theme/
│   │   ├── Color.kt         # 颜色系统
│   │   ├── Theme.kt         # 主题定义
│   │   └── Type.kt          # 排版系统
│   ├── components/
│   │   ├── TodoItemCard.kt  # Todo 卡片
│   │   ├── PriorityChip.kt  # 优先级芯片
│   │   ├── CategoryChip.kt  # 分类芯片
│   │   ├── CategorySelector.kt # 分类选择器
│   │   ├── SearchBar.kt     # 搜索栏和筛选
│   │   └── AddEditTodoDialog.kt # 添加/编辑对话框
│   └── screens/
│       ├── HomeScreen.kt    # 主界面
│       └── EditTodoScreen.kt # 编辑界面
├── viewmodel/
│   └── TodoViewModel.kt     # ViewModel
└── MainActivity.kt          # 主 Activity
```

## 构建说明

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 35
- Min SDK 26 (Android 8.0)

### 构建步骤

1. 克隆项目到本地
2. 使用 Android Studio 打开 `ToDoApp` 目录
3. 等待 Gradle 同步完成
4. 运行模拟器或连接真机
5. 点击 Run 按钮

### 生成 APK

```bash
./gradlew assembleDebug
```

APK 将生成在 `app/build/outputs/apk/debug/` 目录

## 数据库设计

### TodoItem 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| title | String | 标题 |
| description | String | 描述 |
| isCompleted | Boolean | 完成状态 |
| priority | Priority | 优先级 (枚举) |
| category | Category | 分类 (枚举) |
| dueDate | LocalDate? | 截止日期 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |
| hasReminder | Boolean | 是否有提醒 |
| reminderTime | LocalDateTime? | 提醒时间 |
| repeatType | RepeatType | 重复类型 (枚举) |
| repeatEndDate | LocalDate? | 重复结束日期 |

### SubTask 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| parentTodoId | Long | 父任务 ID (外键) |
| title | String | 子任务标题 |
| description | String | 子任务描述 |
| isCompleted | Boolean | 完成状态 |
| priority | Priority | 优先级 |
| dueDate | LocalDate? | 截止日期 |
| sortOrder | Int | 排序顺序 |

## 截图

（待添加）

## 后续计划

- [x] 本地提醒通知
- [x] 子任务功能
- [x] 重复任务设置
- [x] 数据统计图表
- [x] 批量操作功能
- [x] 导出/导入功能
- [ ] 云同步备份
- [ ] 桌面小部件
- [ ] 更多主题配色

## License

MIT License
