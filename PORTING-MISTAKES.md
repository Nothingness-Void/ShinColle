# Porting Mistakes And Guardrails

这份文档只记录已经实际踩过、并且以后必须避免重复发生的错误。

## 1. 旧实体贴图直接套到 `PlayerModel`

### 错误
- 我一开始用现代 `PlayerModel` 去渲染旧版舰娘实体。
- 结果是贴图不是“丢失”，而是 UV 全部错位，实体外观严重变形。

### 原因
- 旧版实体贴图对应的是自定义 `ModelRenderer` 盒模型，不是 vanilla 玩家皮肤布局。
- 只要模型骨架不一致，贴图文件本身再正确也会显示错误。

### 固化规则
- 旧版 `Entity*.png` 一律先假定为“自定义 UV 模型贴图”，不能默认认为能兼容 vanilla 骨架。
- 只要旧版有独立 `Model*.java`，就必须优先复用该模型结构，而不是先塞进 `PlayerModel`。
- “能看到实体”不等于“迁移完成”，必须确认模型结构、贴图映射、缩放三者同时正确。

## 2. `ResourceLocation` 使用了大写路径

### 错误
- 运行时我访问了 `shincolle:legacy_model_sources/ModelMidwayHime.java` 这类大写路径。
- 进入世界后渲染到对应实体时直接崩溃。

### 原因
- 1.20 的 `ResourceLocation` path 只能包含 `[a-z0-9/._-]`。
- 旧项目很多资源和源码文件名带大写，但运行时路径不能照搬。

### 固化规则
- 运行时资源路径一律小写。
- 任何新加的 `ResourceLocation.fromNamespaceAndPath(...)` 都要先检查 path 是否全小写。
- 旧资源如果源文件名带大写，构建阶段必须做小写映射，不能在运行时直接引用原名。

## 3. 改了 `processResources` 规则，但没强制重跑资源输出

### 错误
- 我给 `processResources` 加了重命名规则，但第一次验证时任务被判成了 `UP-TO-DATE`。
- 构建输出里还保留旧的大写文件名，导致修复没有真正生效。

### 原因
- Gradle 增量构建不会自动替你清掉旧输出。
- 资源管线一旦改了复制/重命名逻辑，仅看源码改动和 `compileJava` 成功不够。

### 固化规则
- 只要修改了 `processResources` 的复制、过滤、重命名规则，必须执行：

```powershell
./gradlew cleanProcessResources processResources
```

- 执行后必须直接检查输出目录，而不是只看 Gradle 成功：

```powershell
Get-ChildItem .\\build\\resources\\main\\assets\\shincolle\\legacy_model_sources
```

- 验证标准不是“任务成功”，而是“输出文件名和运行时路径完全一致”。

## 4. 渲染重构后没有先做“进世界”级别冒烟

### 错误
- 我完成编译后就让你进客户端测试，但没有先自己确认“创建/进入世界”这一步。
- 结果崩溃发生在真正渲染实体的阶段，而不是启动阶段。

### 原因
- 实体渲染问题往往只会在世界内、目标实体实际出现在视野里时触发。
- `compileJava`、主菜单可进、资源加载通过，都不能证明实体渲染链安全。

### 固化规则
- 任何实体渲染改动后，最低冒烟标准必须是：
  - 能进主菜单
  - 能进入世界
  - 视野内至少生成 1 个友方舰娘
  - 视野内至少生成 1 个深海/姬级实体
- 只有这一步通过，才能把客户端交给你继续测。

## 5. Windows 下 `JAVA_HOME` 设置不够严谨

### 错误
- 我第一次用 `cmd` 启动客户端时，`JAVA_HOME` 的写法有问题，导致被识别成无效目录。

### 原因
- Windows `cmd` 对带空格路径很敏感，`set JAVA_HOME=...` 很容易因为尾部空格或引号问题失效。

### 固化规则
- 在 `cmd` 里设置 `JAVA_HOME` 一律使用：

```cmd
set "JAVA_HOME=C:\Program Files\Java\jdk-17.0.3.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
```

- 不再使用没有包裹引号的 `set JAVA_HOME=...` 写法。

## 实体迁移后的强制自检清单

每次改实体、模型、贴图、资源加载时，提交给你之前必须完整做完下面这套检查。

1. 路径检查
- 新增的 `ResourceLocation` path 是否全小写。
- `build/resources/main` 里的实际输出文件名是否与运行时 path 一致。

2. 构建检查
- 运行 `./gradlew compileJava`
- 如果改过资源复制规则，再运行 `./gradlew cleanProcessResources processResources`

3. 客户端检查
- 客户端能启动到主菜单
- 能进入世界
- 世界内生成至少一只普通舰娘
- 世界内生成至少一只大型深海单位
- 不出现贴图错位、紫黑格、进世界崩溃

4. 结论要求
- 没做完上述检查，不能再说“已经可以试”
- 如果只是“代码层通过、运行时未验证”，必须明确写出来

## 当前阶段结论

这次最大的教训是：
- 旧项目迁移里，“编译通过”只能证明语法正确，不能证明资源路径、模型结构、运行时渲染正确。
- 对旧版自定义模型，必须把“资源命名规范”和“旧模型骨架复用”当成第一优先级，而不是后补。
