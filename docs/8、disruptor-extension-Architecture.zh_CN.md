# disruptor-extension 系统架构设计

> **文档说明**：disruptor-extension 系统架构设计，包含工程定位、物理目录拓扑、事件发布调用链与处理器链执行流的 CodeGraph 语义分析。
>
> **版本**：V3.0.x
> **最后更新**：2026-08-20

---

## 目录

- [1. 工程定位](#1-工程定位)
- [2. 物理目录拓扑](#2-物理目录拓扑)
- [3. 事件发布流程 (CodeGraph)](#3-事件发布流程-codegraph)
- [4. 处理器链执行流 (CodeGraph)](#4-处理器链执行流-codegraph)
- [5. 附录：核心文件索引](#5-附录核心文件索引)

---

## 1. 工程定位

`disruptor-extension` 是构建在 [LMAX Disruptor 4.0.0](https://github.com/LMAX-Exchange/disruptor) 无锁环形队列之上的 **工业级扩展库**。架构上对标 **Apache Shiro FilterChain** 的设计哲学，在 Disruptor 原生事件循环抽象之上，分层构建了一套完整的 **事件路由 + 责任链 + AOP 通知** 处理框架。

项目是 **纯 Java 库**（`packaging: jar`）：**不** 捆绑 Spring Boot 自动装配，**不** 要求任何容器。在 `com.lmax.disruptor.*` 命名空间（与 Disruptor 原生包对齐的刻意设计，便于访问包可见成员）下，共提供 36 个核心类/接口，配合 31 个单元测试类实现 1:1 覆盖。

### 1.1 核心特性矩阵

| 能力维度 | 实现机制 | 关键类 |
| :--- | :--- | :--- |
| 事件载体 | 三级路由元数据 + 任意 Payload | [DisruptorEvent.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/DisruptorEvent.java#L52-L67) |
| 事件发布 | Template 模式 + 三种 Translator 重载 | [DisruptorTemplate.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L38-L104) |
| 处理器链 | 责任链 + 代理嵌套 | [ProxiedHandlerChain.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/ProxiedHandlerChain.java#L41-L101) |
| 路由匹配 | AntPathMatcher + 双配置（注解 / INI） | [PathMatchingHandlerChainResolver.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/PathMatchingHandlerChainResolver.java#L42-L151) |
| AOP 通知 | `preHandle` / `postHandle` / `afterCompletion` 生命周期 | [AbstractAdviceEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractAdviceEventHandler.java#L38-L156) |
| 构建基线 | JDK 21 + Maven 4.1.0 model + JaCoCo 90% 门禁 | [pom.xml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml#L2-L524) |

### 1.2 职责边界

本库 **是**：

- "分类异步事件处理" 模式的参考引擎：事件携带 `{namespace, topic, tag}` 元数据发布，通过 Ant 风格路由匹配解析到各规则对应的处理链。
- 任何已使用 LMAX Disruptor、又希望在不重复造分发器的前提下引入 Shiro 式责任链语义的代码库的即插即用伴侣。

本库 **不是**：

- LMAX Disruptor 核心本身——`com.lmax:disruptor`（4.0.0）是常规依赖。
- Spring Boot starter——不打包自动装配；下游 `disruptor-spring-boot-starter`（如有）负责 Bean 装配。
- 独立消息代理——全部处理均在单个 JVM 的环形队列内完成。

### 1.3 典型场景

| 场景 | 使用构件 |
| :--- | :--- |
| 按 topic / tag / namespace 发布事件 | `DisruptorTemplate.publishEvent(...)` + 一 / 二 / 三参数 translator |
| 把事件路由到各规则对应的处理链 | `@EventRule` + `PathMatchingHandlerChainResolver` + `DisruptorEventDispatcher` |
| 分类消费者（类 MQ 消费模式） | 每条路由模式一条 `DisruptorHandler` 链 |
| 精细控制 ring-buffer 线程 / 等待策略 | `DisruptorEvent*ThreadFactory` 系列 + `WaitStrategys` |

---

## 2. 物理目录拓扑

```
src/main/java/com/lmax/disruptor/
├── DisruptorTemplate.java                      # [门面] 发布模板（3 种重载）
├── annotation/
│   └── EventRule.java                           # [声明式] 类级 Ant 路由规则（@Inherited）
├── config/
│   ├── EventHandlerDefinition.java              # [配置] 处理器链定义 DTO
│   └── Ini.java                                 # [配置] INI 格式解析器（612 行）
├── event/
│   ├── DisruptorEvent.java                      # [模型] 三级路由载体: ns/topic[/tag]
│   ├── DisruptorEventFactory.java               # [工厂] RingBuffer 预分配
│   ├── DisruptorEventPublisher.java             # [SPI] 发布接口
│   ├── DisruptorEventPublisherAware.java        # [SPI] 发布器注入感知
│   ├── factory/                                 # → 5 种 ThreadFactory 实现
│   │   ├── DisruptorEventDaemonThreadFactory.java
│   │   ├── DisruptorEventLoggerThreadFactory.java
│   │   ├── DisruptorEventMaxPriorityThreadFactory.java
│   │   ├── DisruptorEventThreadFactory.java
│   │   └── DisruptorEventWorkerThreadFactory.java
│   ├── handler/                                 # → 处理器核心（7 层抽象）
│   │   ├── DisruptorHandler.java                # [SPI] 处理器接口 (L42: doHandler)
│   │   ├── DisruptorEventDispatcher.java        # [入口] Disruptor → 链 桥接
│   │   ├── AbstractNameableEventHandler.java
│   │   ├── AbstractEnabledEventHandler.java
│   │   ├── AbstractAdviceEventHandler.java     # 通知层（AOP）
│   │   ├── AbstractPathMatchEventHandler.java  # 路径匹配层
│   │   ├── AbstractRouteableEventHandler.java  # 链解析层
│   │   ├── Nameable.java
│   │   ├── NamedHandlerList.java
│   │   ├── PathProcessor.java
│   │   └── chain/
│   │       ├── HandlerChain.java                # [SPI] 链接口
│   │       ├── HandlerChainManager.java         # [SPI] 链管理器接口
│   │       ├── HandlerChainResolver.java        # [SPI] 链解析器接口
│   │       ├── ProxiedHandlerChain.java         # 基于游标的嵌套代理链
│   │       └── def/
│   │           ├── DefaultHandlerChainManager.java
│   │           ├── DefaultNamedHandlerList.java
│   │           └── PathMatchingHandlerChainResolver.java
│   └── translator/                              # → 3 种 EventTranslator 变体
│       ├── DisruptorEventOneArgTranslator.java
│       ├── DisruptorEventTwoArgTranslator.java
│       └── DisruptorEventThreeArgTranslator.java
├── exception/
│   └── EventHandleException.java                # 运行时异常包装
├── hooks/
│   └── DisruptorShutdownHook.java               # JVM 关闭钩子
├── thread/
│   ├── DisruptorThreadFactory.java              # [枚举] 3 种线程工厂风格
│   └── DisruptorWaitStrategy.java               # [枚举] 4 种等待策略
└── util/
    ├── AntPathMatcher.java                      # Ant 风格路径匹配（Spring 移植）
    ├── PathMatcher.java
    ├── StringUtils.java                         # commons-lang3 + 路径/拼音/CJK 辅助
    └── WaitStrategys.java
```

### 2.1 包 → 职责映射

| 包 | 职责 | 类数 |
| :--- | :--- | ---: |
| `com.lmax.disruptor` | 根门面 — `DisruptorTemplate` | 1 |
| `com.lmax.disruptor.annotation` | 声明式路由 — `@EventRule` | 1 |
| `com.lmax.disruptor.config` | INI 解析器 + 链定义 DTO | 2 |
| `com.lmax.disruptor.event` | 事件模型、工厂、发布器 SPI | 4 |
| `com.lmax.disruptor.event.factory` | 5 种命名 ThreadFactory 变体 | 5 |
| `com.lmax.disruptor.event.handler` | 5 层处理器抽象 + 分发器 | 10 |
| `com.lmax.disruptor.event.handler.chain` | 链 SPI + 代理实现 | 4 |
| `com.lmax.disruptor.event.handler.chain.def` | 默认管理器 + Ant 解析器 | 3 |
| `com.lmax.disruptor.event.translator` | 3 种参数个数的 EventTranslator | 3 |
| `com.lmax.disruptor.exception` | 运行时异常包装 | 1 |
| `com.lmax.disruptor.hooks` | JVM 关闭钩子 | 1 |
| `com.lmax.disruptor.thread` | 线程工厂 + 等待策略枚举 | 2 |
| `com.lmax.disruptor.util` | 字符串 / 路径 / 等待策略工具 | 4 |
| **合计** | | **37** |

---

## 3. 事件发布流程 (CodeGraph)

发布路径共分为 **5 个阶段**，从 `DisruptorTemplate` 入口经由 LMAX Disruptor 环形队列，一直追踪到处理器入口。各阶段均链接到对应源码的精确行号。

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                  阶段 1 — 发布入口（3 种重载）                                    │
└──────────────────────────────────────┬───────────────────────────────────────────┘
                                       │
[DisruptorTemplate.publishEvent]       │ 3 个公开重载签名:
  ├── L64:  (DisruptorEvent event)     │   • publishEvent(DisruptorEvent)
  ├── L76:  (topic, tag, payload)      │   • publishEvent(topic, tag, payload)
  └── L94:  (topic, ns, tag, payload)  │   • publishEvent(topic, namespace, tag, payload)
                                       │
                                       ▼
                    ┌─────────────────────────────────────────────┐
                    │ 阶段 2 — Translator 填充插槽                │
                    │ DisruptorEventOneArgTranslator              │
                    │   translateTo() [L44-L52]                    │
                    │   • messageId 回退 → RingBuffer sequence     │
                    │   • 把 6 个字段复制到预分配的                  │
                    │     DisruptorEvent 插槽中                    │
                    └──────────────────┬──────────────────────────┘
                                       │
                                       ▼
                    ┌─────────────────────────────────────────────┐
                    │ 阶段 3 — LMAX Disruptor 原生发布            │
                    │   dsl.Disruptor.publishEvent(translator,    │
                    │                                eventWrapper)│
                    │   → 申请 sequence；                        │
                    │   → 内存栅栏 publish；                      │
                    │   → 通过 WaitStrategy 唤醒消费者             │
                    └──────────────────┬──────────────────────────┘
                                       │
                                       ▼
                    ┌─────────────────────────────────────────────┐
                    │ 阶段 4 — 消费端 EventHandler                │
                    │ DisruptorEventDispatcher.onEvent()          │
                    │   [L64-L72] (implements EventHandler<DE>)   │
                    │   • 每个事件 new ProxiedHandlerChain()      │
                    │   • 调用 this.doHandler(event, chain)       │
                    └──────────────────┬──────────────────────────┘
                                       │
                                       ▼
                    ┌─────────────────────────────────────────────┐
                    │ 阶段 5 — 交给处理器链执行流                   │
                    │   → 见下文第 4 节                            │
                    └─────────────────────────────────────────────┘
```

### 3.1 关键调用点（可点击引用）

| 阶段 | 调用点 | 文件与行号 |
| :--- | :--- | :--- |
| 1 | `publishEvent(DisruptorEvent)` — 原始事件直通 | [DisruptorTemplate.java#L56-L72](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L56-L72) |
| 1 | `publishEvent(topic, ns, tag, payload)` — 4 参数便捷重载，以 `System.currentTimeMillis()` 作为 messageId | [DisruptorTemplate.java#L87-L105](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L87-L105) |
| 2 | `DisruptorEventOneArgTranslator.translateTo` — 把封装对象复制到预分配插槽；sequence 充当兜底 messageId | [DisruptorEventOneArgTranslator.java#L36-L53](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/translator/DisruptorEventOneArgTranslator.java#L36-L53) |
| 4 | `DisruptorEventDispatcher.onEvent` — Disruptor EventHandler 桥接器；每事件实例化一次 ProxiedHandlerChain | [DisruptorEventDispatcher.java#L57-L73](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorEventDispatcher.java#L57-L73) |
| 4 | `DisruptorEventDispatcher.doHandler` — 向下委托到 5 层处理器抽象 | [DisruptorEventDispatcher.java#L111-L125](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorEventDispatcher.java#L111-L125) |

---

## 4. 处理器链执行流 (CodeGraph)

处理器子系统构建为 **5 层抽象类栈** 加 **1 个适配器**（Dispatcher）。每一层精确添加 **一个横切关注点**；在每个过渡阶段都严格满足里氏替换原则。CodeGraph 验证确认：每个子类只重写其父类的一个 `doHandlerInternal` / 生命周期方法，并把前后处理委托给 `super`。

### 4.1 五层处理器继承栈

```
DisruptorHandler<T extends DisruptorEvent>              ← [SPI 接口]
    ▲                                                   │
    │                                                   └─ doHandler(T event, HandlerChain<T> chain) throws Exception
    │
AbstractNameableEventHandler<T extends DisruptorEvent>  ← [L1 · 基础层]
    ▲                                                   │ 注入并保存 handler 名称（Nameable 实现）
    │
AbstractEnabledEventHandler<T extends DisruptorEvent>   ← [L2 · 开关层]
    │                                                   │ doHandler() → 若 !enabled 则短路交给 chain
    │                                                   │ ↓ 抽象 doHandlerInternal(T, Chain)
    ▲
    │
AbstractAdviceEventHandler<T extends DisruptorEvent>    ← [L3 · AOP 通知层]
    │                                                   │ doHandlerInternal() 把子调用包裹在：
    │                                                   │   preHandle → executeChain → postHandle
    │                                                   │   finally { cleanup → afterCompletion }
    ▲
    │
AbstractPathMatchEventHandler<T extends DisruptorEvent> ← [L4 · 路由层]
    │                                                   │ preHandle() 遍历 appliedPaths；
    │                                                   │   AntPathMatcher 首次匹配即胜出；
    │                                                   │   → 匹配时触发 onPreHandle(event) 钩子
    ▲
    │
AbstractRouteableEventHandler<T extends DisruptorEvent> ← [L5 · 链解析层]
    │                                                   │ doHandlerInternal() try{…} → 包装为 EventHandleException
    │                                                   │ executeChain() → resolver.getChain(event, chain)
    ▲                                                   │
    │
DisruptorEventDispatcher                                ← [适配器 · 入口点]
        implements EventHandler<DisruptorEvent>         │ onEvent() → doHandler() 启动引导
```

### 4.2 HandlerChain 代理嵌套模型

`ProxiedHandlerChain` 实现了 **嵌套游标模式**：它包装一个 *original 原链* 和 一个 *handler 列表*，并暴露一个位置游标——随着每个处理器调用 `chain.doHandler(event)` 而递增。这与 Servlet API 中 `javax.servlet.FilterChain` 的工作方式完全一致。

```
[HandlerChain<T> 接口]
    ▲
    │
[ProxiedHandlerChain<T>]
    │
    ├─ 字段
    │    ├── originalChain  : HandlerChain<T>   （嵌套代理 / 尾部兜底）
    │    ├── handlers       : List<DisruptorHandler<T>> （本帧处理器）
    │    └── currentPosition: int              （本帧游标，非线程安全）
    │
    └─ doHandler(T event)
         ├─ 若 handlers == null 或 position == handlers.size
         │      → originalChain.doHandler(event)     （尾部透传）
         └─ 否则
                → handlers.get(currentPosition++)
                       .doHandler(event, this)       （派发给第 N 个处理器）
                                                         │
                                                         ▼
                                             处理器体：任意业务逻辑
                                             链控制：调用 chain.doHandler(event)
                                             则继续；省略则短路终止。
```

### 4.3 链构建 + 解析流程

```
DefaultHandlerChainManager.createChain(chainName, "h1,h2,h3")
  │
  ├─ splitChainDefinition() → StringUtils.splits(",") → ["h1","h2","h3"]
  └─ for token: addToChain(chainName, token)
       ├─ getHandler(token) → 未注册则抛 IllegalArgumentException
       └─ ensureChain(chainName) → DefaultNamedHandlerList.add(handler)

PathMatchingHandlerChainResolver.getChain(event, originalChain)
  │
  ├─ handlerChainManager.getChainNames() → 遍历所有已注册链名
  ├─ pathMatches(pattern, event.getRouteExpression()) → AntPathMatcher.match
  └─ 首次匹配即胜出：
       handlerChainManager.proxy(originalChain, pathPattern)
          → configured.proxy(original)
              → new ProxiedHandlerChain(originalChain, handlers)
```

### 4.4 关键调用点（可点击引用）

| 层 / 关注点 | 方法 | 文件与行号 |
| :--- | :--- | :--- |
| SPI 契约 | `DisruptorHandler.doHandler(T, HandlerChain<T>)` | [DisruptorHandler.java#L40-L44](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorHandler.java#L40-L44) |
| L2 开关门 | `AbstractEnabledEventHandler.doHandler` — 短路逻辑 | [AbstractEnabledEventHandler.java#L61-L79](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractEnabledEventHandler.java#L61-L79) |
| L3 AOP 包装 | `AbstractAdviceEventHandler.doHandlerInternal` — pre/executeChain/post + finally 清理 | [AbstractAdviceEventHandler.java#L87-L157](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractAdviceEventHandler.java#L87-L157) |
| L4 Ant 路由 | `AbstractPathMatchEventHandler.preHandle` — appliedPaths 首次匹配循环 | [AbstractPathMatchEventHandler.java#L100-L129](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractPathMatchEventHandler.java#L100-L129) |
| L5 异常包装 | `AbstractRouteableEventHandler.doHandlerInternal` — catch → EventHandleException | [AbstractRouteableEventHandler.java#L71-L90](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractRouteableEventHandler.java#L71-L90) |
| 代理链推进 | `ProxiedHandlerChain.doHandler` — 游标递增 + 嵌套兜底 | [ProxiedHandlerChain.java#L78-L102](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/ProxiedHandlerChain.java#L78-L102) |
| 链解析器 | `PathMatchingHandlerChainResolver.getChain` — 首次匹配 Ant 解析 | [PathMatchingHandlerChainResolver.java#L107-L153](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/PathMatchingHandlerChainResolver.java#L107-L153) |
| 链构建 | `DefaultHandlerChainManager.createChain` — 逗号 token → NamedHandlerList | [DefaultHandlerChainManager.java#L139-L198](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/DefaultHandlerChainManager.java#L139-L198) |

---

## 5. 附录：核心文件索引

### 5.1 构建与 CI

- 构建清单 — [pom.xml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml)
- CI 工作流（feature/3.0.x，JDK 21 + Temurin）— [.github/workflows/ci.yml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/.github/workflows/ci.yml)

### 5.2 顶层入口点

- 发布门面 — [DisruptorTemplate.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java)
- Disruptor → 框架 适配器 — [DisruptorEventDispatcher.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorEventDispatcher.java)
- 路由载体 — [DisruptorEvent.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/DisruptorEvent.java) + [DisruptorEventFactory.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/DisruptorEventFactory.java)

### 5.3 SPI 契约

- 处理器 SPI — [DisruptorHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorHandler.java)
- 链 SPI — [HandlerChain.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/HandlerChain.java) + [HandlerChainManager.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/HandlerChainManager.java) + [HandlerChainResolver.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/HandlerChainResolver.java)
- 发布 SPI — [DisruptorEventPublisher.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/DisruptorEventPublisher.java)
- 声明式路由 SPI — [EventRule.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/annotation/EventRule.java)

### 5.4 参考实现

- 责任链代理 — [ProxiedHandlerChain.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/ProxiedHandlerChain.java)
- 默认链管理器（LinkedHashMap 保序）— [DefaultHandlerChainManager.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/DefaultHandlerChainManager.java)
- Ant 风格链解析器 — [PathMatchingHandlerChainResolver.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/PathMatchingHandlerChainResolver.java)
- 5 层处理器抽象 — [AbstractNameableEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractNameableEventHandler.java)、[AbstractEnabledEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractEnabledEventHandler.java)、[AbstractAdviceEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractAdviceEventHandler.java)、[AbstractPathMatchEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractPathMatchEventHandler.java)、[AbstractRouteableEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractRouteableEventHandler.java)

### 5.5 本次版本缺陷修复与设计说明

本节记录基于 CodeGraph 语义审计后落实的具体修复项与设计决策（完整严重度矩阵可参考项目 README §12）。标注 **(Doc)** 的条目表示为代码中有意保留、仅通过文档显式披露的设计约束。

| # | 标题 | 状态 | 说明 |
|---|---|---|---|
| 1 | `DisruptorTemplate.publishEvent(...)` — 并发下 messageId 时间戳冲突 | ✅ 已修复 | 3 参与 4 参便捷重载均改为赋值 `UUID.randomUUID().toString()`；对于接收调用者自行构造 `DisruptorEvent` 的 1 参形式则保持原样，以便使用者保留其外部已分配 ID。参见 [DisruptorTemplate.java#L82-L110](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L82-L110)。 |
| 2 | `EventHandleException(Exception)` — 根因堆栈被丢弃 | ✅ 已修复 | 构造函数现调用 `super(msg, e)`，可通过 `getCause()` 获取原始受检异常。参见 [EventHandleException.java#L35-L48](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/exception/EventHandleException.java#L35-L48)。 |
| 3 | `maven-surefire-plugin` — JaCoCo `@{argLine}` 未合并（覆盖率始终为 0%） | ✅ 已修复 | 在原有 Surefire arg 行前追加 `@{argLine}`，以便 JaCoCo 激活时自动注入其代理 JVM 参数。参见 [pom.xml#L297-L300](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml#L297-L300)。 |
| 4 | 悬空属性 `commons-lang.version`（2.x，从未被引用） | ✅ 已修复 | 已删除；classpath 仅保留实际使用的 `commons-lang3.version`（3.20.0）。参见 [pom.xml#L45-L52](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml#L45-L52)。 |
| 5 | 两套 ThreadFactory 子系统，且不支持业务命名前缀 | 🗓 规划中 | 基于枚举的 `DisruptorThreadFactory`（位于 `thread/`）与 `event/factory/` 下 5 个具体类职责重叠，且两者均不接受调用方自定义线程名前缀。下一小版本将规划基于 Builder 的重构（`DisruptorThreadFactory.builder().namePrefix("order-svc").daemon(true).build()`）；本次补丁不涉及 API 变更，不影响依赖方。 |
| 6 | **(Doc)** `ProxiedHandlerChain.currentPosition` — 默认单消费者假设 | 📌 已文档化 | `ProxiedHandlerChain` 中的嵌套游标为普通可变 `int` 字段，在每次 `doHandler` 调用时推进，未使用原子或 `ThreadLocal` 防护。这符合 Disruptor 的设计模型：链由环形缓冲线程池分配给每个 `EventHandler` 的 **单个** `EventHandler` 线程驱动。**若将同一个链实例在多个独立 `DisruptorEventDispatcher` 之间复用，或在同一个 Disruptor 上通过多次 `handleEventsWith` 注册共享该链，则游标不再同步，执行序未定义。** 建议方案：每个 dispatcher 始终重新构造 `ProxiedHandlerChain`（或重新调用 `HandlerChainResolver.getChain(event, chain)` 得到新链）。参见 [ProxiedHandlerChain.java#L60-L102](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/ProxiedHandlerChain.java#L60-L102) 对字段与调用点的实现。 |

---

**文档版本**：V3.0.x  
**创建日期**：2026-08-20  
**最后更新**：2026-08-20  
**文档状态**：✅ 待评审
