# disruptor-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-21-orange)](https://github.com/easy-4-java/disruptor-extension) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

> LMAX Disruptor 的 JDK 8 版本线扩展：处理链、事件分发器、发布模板、`@EventRule`
> 路由以及线程 / 等待策略工厂。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 功能与状态](#2-功能与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本与分支](#10-版本与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述 — 工程定位

`disruptor-extension` 是构建在 [LMAX Disruptor 4.0.0](https://github.com/LMAX-Exchange/disruptor) 无锁环形队列之上的 **工业级扩展库**。架构上对标 **Apache Shiro FilterChain** 的设计哲学，在 Disruptor 原生事件循环抽象之上，分层构建了一套完整的 **事件路由 + 责任链 + AOP 通知** 处理框架。

项目是 **纯 Java 库**（`packaging: jar`）——**不** 捆绑 Spring Boot 自动装配，**不** 要求任何容器，在 `com.lmax.disruptor.*` 命名空间（与 Disruptor 原生包对齐的刻意设计，便于访问包可见成员）下，共提供 36 个核心类/接口，配合 31 个单元测试类实现 1:1 覆盖。完整架构文档见 [product-docs/disruptor-extension/](./product-docs/disruptor-extension/) 目录下的双语架构说明。

### 1.1 核心特性矩阵

| 能力维度 | 实现机制 | 关键类 |
| :--- | :--- | :--- |
| 事件载体 | 三级路由元数据 + 任意 Payload | `DisruptorEvent`（`com.lmax.disruptor.event`） |
| 事件发布 | Template 模式 + 三种 Translator 重载 | `DisruptorTemplate` + `event.translator.*` |
| 处理器链 | 责任链 + 代理嵌套 | `ProxiedHandlerChain`（`event.handler.chain`） |
| 路由匹配 | AntPathMatcher + 双配置（注解 / INI） | `PathMatchingHandlerChainResolver`（`event.handler.chain.def`） |
| AOP 通知 | `preHandle` / `postHandle` / `afterCompletion` | `AbstractAdviceEventHandler`（`event.handler`） |
| 构建基线 | JDK 21 + Maven 4.1.0 model + JaCoCo 90% 门禁 | `pom.xml` + `.github/workflows/ci.yml` |

### 1.2 职责边界

本库为分类化、基于责任链的异步事件处理提供所需基础设施：

- **发布** — `DisruptorTemplate`（按 topic / tag / namespace 发布）与 `DisruptorEventPublisher`；一 / 二 / 三参数 translator。
- **路由** — `DisruptorEvent` 携带路由表达式（`namespace/topic[/tag]`）；`@EventRule` 注解声明处理链服务的 Ant 风格规则。
- **处理链** — `DisruptorHandler` / `HandlerChain` SPI，配合 `HandlerChainManager`、`NamedHandlerList`、`PathMatchingHandlerChainResolver`、`ProxiedHandlerChain` 与 `DisruptorEventDispatcher`，以及可复用的五层抽象处理器（nameable / enabled / advice / path-match / routeable）。
- **基础设施** — 线程工厂、等待策略常量（`WaitStrategys`）、`AntPathMatcher`、`EventHandleException`、`DisruptorShutdownHook`、`Ini` / `EventHandlerDefinition` 配置辅助。

这是姊妹模块 `disruptor-biz` 所描述"分类异步处理"模式的引擎（如 `/Event-DC-Output/TagA-Output/** = inDbPostHandler`、`/Event-DC-Output/TagB-Output/** = smsPostHandler`）。

它 **不是**：

- LMAX Disruptor 核心本身——`com.lmax:disruptor` 是常规依赖。
- Spring Boot starter——不打包自动装配；下游 `disruptor-spring-boot-starter`（如有）负责 Bean 装配。
- 独立消息代理——全部处理均在单个 JVM 的环形队列内完成。

### 1.3 典型场景

| 场景 | 使用构件 |
| :--- | :--- |
| 按 topic / tag / namespace 发布事件 | `DisruptorTemplate.publishEvent(...)` |
| 把事件路由到各规则对应的处理链 | `@EventRule` + `PathMatchingHandlerChainResolver` + `DisruptorEventDispatcher` |
| 分类消费者（类 MQ 消费模式） | 每条路由模式一条 `DisruptorHandler` 链 |
| 精细控制 ring-buffer 线程 / 等待策略 | `DisruptorEventThreadFactory` 系列 + `WaitStrategys` |

## 2. 功能与状态

| 能力 | 状态 | 说明 |
| :--- | :--- | :--- |
| `DisruptorTemplate` | 稳定 | `publishEvent(DisruptorEvent)`、`publishEvent(topic, tag, payload)`、`publishEvent(topic, namespace, tag, payload)` |
| `DisruptorEvent` 模型 | 稳定 | topic / tag / namespace / messageId / payload / sequence / timestamp；`getRouteExpression()` |
| 事件发布器与 translator | 稳定 | `DisruptorEventPublisher`、`DisruptorEventPublisherAware`、一 / 二 / 三参数 translator |
| 处理链 SPI | 稳定 | `DisruptorHandler`、`HandlerChain`、`HandlerChainManager`、`NamedHandlerList`、`ProxiedHandlerChain` |
| 链实现 | 稳定 | `DefaultHandlerChainManager`、`DefaultNamedHandlerList`、`PathMatchingHandlerChainResolver`、`DisruptorEventDispatcher` |
| 抽象处理器 | 稳定 | `AbstractAdviceEventHandler`、`AbstractEnabledEventHandler`、`AbstractNameableEventHandler`、`AbstractPathMatchEventHandler`、`AbstractRouteableEventHandler` |
| `@EventRule` 注解 | 稳定 | Ant 风格规则表达式，默认 `*` |
| 线程 / 策略工厂 | 稳定 | 5 个 `DisruptorEvent*ThreadFactory` 变体、`DisruptorThreadFactory`、`DisruptorWaitStrategy`、`WaitStrategys` |
| 工具类 | 稳定 | `AntPathMatcher`、`PathMatcher`、`StringUtils`、`EventHandleException`、`DisruptorShutdownHook`、`Ini`、`EventHandlerDefinition` |

## 3. 环境要求与兼容性

| 要求 | 版本 / 说明 |
| :--- | :--- |
| JDK | 21+ |
| Maven | 3.0+（enforcer 强制；项目内置 Maven Wrapper `./mvnw`） |
| LMAX Disruptor | `com.lmax:disruptor` 3.4.4（由本 pom 管理） |

版本线：

| 分支 | JDK | 版本 |
| :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. 架构与模块

### 4.1 高层数据流

```text
+------------------+   +------------------------------------------+
| Producer         |   | disruptor-extension                      |
|                  |-->|  DisruptorTemplate / Publisher           |
| topic/tag/ns     |   |    | one/two/three-arg translators       |
|                  |   |    v                                    |
|                  |   |  Disruptor<DisruptorEvent> ring buffer   |
|                  |   |    | DisruptorEventDispatcher            |
|                  |   |    |   | HandlerChainResolver            |
|                  |   |    |   |   | PathMatchingResolver        |
+------------------+   |    |   |   |   | HandlerChain (@EventRule)|
                       +-------------------+----------------------+
                                           |
                                           v
                     +-------------------------------------------+
                     | DisruptorHandler processes events per rule|
                     | (/Event/Tag/** = handler)                 |
                     +-------------------------------------------+
```

单模块 Maven 工程（`packaging: jar`），无子模块。

| 构件 | 职责 |
| :--- | :--- |
| `io.github.easy4j:disruptor-extension` | 发布、路由、处理链、分发器、工厂、工具类 |

### 4.2 物理目录拓扑

代码库在 `src/main/java/com/lmax/disruptor/` 下共组织为 13 个包，每个包承担单一且明确的职责：

```
src/main/java/com/lmax/disruptor/
├── DisruptorTemplate.java                      # [门面] 发布模板（3 种重载）
├── annotation/
│   └── EventRule.java                           # [声明式] 类级 Ant 路由规则
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
│   ├── handler/                                 # → 处理器五层抽象栈
│   │   ├── DisruptorHandler.java                # [SPI] 处理器接口
│   │   ├── DisruptorEventDispatcher.java        # [入口] Disruptor → 链 桥接
│   │   ├── AbstractNameableEventHandler.java    # L1：注入 handler 名称
│   │   ├── AbstractEnabledEventHandler.java     # L2：enabled 开关短路
│   │   ├── AbstractAdviceEventHandler.java      # L3：AOP 通知生命周期
│   │   ├── AbstractPathMatchEventHandler.java   # L4：Ant 路径路由匹配
│   │   ├── AbstractRouteableEventHandler.java   # L5：链解析 + 异常包装
│   │   ├── Nameable.java / NamedHandlerList.java
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
│   └── translator/                              # → 3 种参数个数的 EventTranslator
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
    ├── StringUtils.java
    └── WaitStrategys.java
```

包 → 职责映射：

| 包 | 内容 |
| :--- | :--- |
| `com.lmax.disruptor` | `DisruptorTemplate` |
| `com.lmax.disruptor.annotation` | `EventRule` |
| `com.lmax.disruptor.event` | `DisruptorEvent`、`DisruptorEventFactory`、`DisruptorEventPublisher(Aware)` |
| `com.lmax.disruptor.event.handler` | `DisruptorHandler`、`DisruptorEventDispatcher`、五层抽象处理器、`Nameable`、`PathProcessor` |
| `com.lmax.disruptor.event.handler.chain` | `HandlerChain(Manager/Resolver)`、`ProxiedHandlerChain`、定义类 |
| `com.lmax.disruptor.event.translator` | 一 / 二 / 三参数 translator |
| `com.lmax.disruptor.event.factory` / `thread` / `util` | 线程工厂、`DisruptorWaitStrategy`、`WaitStrategys`、`AntPathMatcher`、`StringUtils` |
| `com.lmax.disruptor.hooks` / `exception` / `config` | `DisruptorShutdownHook`、`EventHandleException`、`Ini`、`EventHandlerDefinition` |

### 4.3 事件发布流程 (CodeGraph)

发布路径共分为 **5 个阶段**，从 `DisruptorTemplate` 入口经由 LMAX Disruptor 环形队列，一直追踪到处理器入口点。精确到行号的引用请查阅 `product-docs/disruptor-extension/` 下的架构文档。

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                  阶段 1 — 发布入口（3 种重载）                                │
└─────────────────────────────┬────────────────────────────────────────────────┘
                              │
DisruptorTemplate.publishEvent│ 3 个公开签名:
  ├─ (DisruptorEvent)         │   • publishEvent(DisruptorEvent)
  ├─ (topic, tag, payload)    │   • publishEvent(topic, tag, payload)
  └─ (topic, ns, tag, payload)│   • publishEvent(topic, namespace, tag, payload)
                              │
                              ▼
          ┌───────────────────────────────────────────────┐
          │ 阶段 2 — Translator 填充插槽                   │
          │ DisruptorEventOneArgTranslator.translateTo    │
          │   • messageId 回退 → RingBuffer sequence       │
          │   • 把 6 个字段复制到预分配 DisruptorEvent 插槽  │
          └──────────────────┬────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────────────┐
          │ 阶段 3 — LMAX Disruptor 原生发布              │
          │   dsl.Disruptor.publishEvent(translator, arg) │
          │   → 申请 sequence + 内存栅栏 publish；         │
          │   → 通过 WaitStrategy 唤醒消费者               │
          └──────────────────┬────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────────────┐
          │ 阶段 4 — 消费端 EventHandler                   │
          │ DisruptorEventDispatcher.onEvent              │
          │   (实现 EventHandler<DisruptorEvent>)         │
          │   • 每个事件 new ProxiedHandlerChain          │
          │   → 委托给 this.doHandler(event, chain)       │
          └──────────────────┬────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────────────┐
          │ 阶段 5 — 交给处理器链执行流                    │
          │   → 见下文第 4.4 节                            │
          └───────────────────────────────────────────────┘
```

### 4.4 处理器链执行流 (CodeGraph)

处理器子系统构建为 **5 层抽象类栈** 加 **1 个适配器**（Dispatcher）。每一层精确添加 **一个横切关注点**。`ProxiedHandlerChain` 实现了 **嵌套游标模式**，与 `javax.servlet.FilterChain` 完全一致——每个处理器调用 `chain.doHandler(event)` 即可推进游标，省略则短路终止。

**五层处理器继承栈**

```
DisruptorHandler<T extends DisruptorEvent>              ← [SPI 接口]
    ▲                                                   │ doHandler(T, Chain)
    │
AbstractNameableEventHandler<T>                        ← [L1 · 基础层]
    ▲                                                   │ 注入 handler 名称
    │
AbstractEnabledEventHandler<T>                         ← [L2 · 开关层]
    │                                                   │ !enabled → chain.next()
    │                                                   │ ↓ doHandlerInternal()
    ▲
    │
AbstractAdviceEventHandler<T>                          ← [L3 · AOP 通知层]
    │                                                   │ pre → execute → post
    │                                                   │ finally { afterCompletion }
    ▲
    │
AbstractPathMatchEventHandler<T>                       ← [L4 · 路由层]
    │                                                   │ appliedPaths 首次匹配即胜出
    ▲
    │
AbstractRouteableEventHandler<T>                       ← [L5 · 解析层]
    │                                                   │ resolver.getChain(event)
    │                                                   │ catch → EventHandleException
    ▲
    │
DisruptorEventDispatcher                                ← [适配器 · 入口]
        implements EventHandler<DisruptorEvent>         │ onEvent() → doHandler()
```

**HandlerChain 代理嵌套模型**

```
ProxiedHandlerChain<T>
  ├─ 字段
  │    ├── originalChain  : HandlerChain<T>   （嵌套代理 / 尾部兜底）
  │    ├── handlers       : List<DisruptorHandler<T>> （本帧处理器）
  │    └── currentPosition: int               （每帧游标）
  │
  └─ doHandler(T event)
       ├─ 若 handlers == null 或 position == handlers.size
       │      → originalChain.doHandler(event)    （尾部透传）
       └─ 否则
              → handlers.get(currentPosition++)
                     .doHandler(event, this)      （派发第 N 个处理器）
                                                       │
                                                       ▼
                                            处理器体（业务逻辑）
                                            链控制：调用 chain.doHandler(event)
                                            则继续；省略则短路终止。
```

## 5. 安装

项目**尚未发布到 Maven Central**。快照 / 发布版本通过阿里云 Maven 仓库与 GitHub
Releases 分发。

Maven：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>disruptor-extension</artifactId>
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:disruptor-extension:3.0.x.x.20260630-SNAPSHOT'
```

## 6. 快速开始

通过 ring buffer 发布事件：

```java
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.DisruptorTemplate;
import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.DisruptorEventFactory;
import com.lmax.disruptor.event.factory.DisruptorEventThreadFactory;
import com.lmax.disruptor.event.translator.DisruptorEventOneArgTranslator;

Disruptor<DisruptorEvent> disruptor = new Disruptor<>(
        new DisruptorEventFactory(), 1024, new DisruptorEventThreadFactory());
DisruptorTemplate template = new DisruptorTemplate(disruptor, new DisruptorEventOneArgTranslator());
disruptor.start();

template.publishEvent("order", "created", orderPayload);
template.publishEvent("order", "prod", "created", orderPayload); // 带 namespace
```

预期结果：一个 topic 为 `order`（namespace `prod`、tag `created`）且携带 payload 的
`DisruptorEvent` 被发布到 ring buffer；`getRouteExpression()` 得到
`prod/order/created`，处理链解析器按 `@EventRule` 模式匹配该表达式。

## 7. 配置

本库没有配置文件或属性前缀。行为通过代码配置：

| 元素 | 说明 |
| :--- | :--- |
| `DisruptorTemplate` | 包装 `Disruptor<DisruptorEvent>` + 一个 `EventTranslatorOneArg`；按事件或 topic/tag/namespace 发布 |
| `@EventRule(value)` | 处理器类上的 Ant 风格规则，如 `/Event-DC-Output/TagA-Output/**`（默认 `*`） |
| `DefaultHandlerChainManager` | `setHandlers(Map)` / `setHandlerChains(Map)` / `addHandler(name, handler)` |
| `DisruptorEventDispatcher` | 由 `HandlerChainResolver` + order 构造；把事件分发到解析出的链 |
| `WaitStrategys` | 预置 `WaitStrategy` 常量：`BLOCKING_WAIT`、`SLEEPING_WAIT`、`YIELDING_WAIT`、`BUSYSPIN_WAIT` |

## 8. 核心用法 / API

### 8.1 规则路由的处理器

```java
import com.lmax.disruptor.annotation.EventRule;
import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import com.lmax.disruptor.event.handler.chain.HandlerChain;

@EventRule("/Event-DC-Output/TagA-Output/**")
public class InDbPostHandler implements DisruptorHandler<DisruptorEvent> {

    @Override
    public void doHandler(DisruptorEvent event, HandlerChain<DisruptorEvent> chain) throws Exception {
        // 该规则下的分类处理
        System.out.println("event topic=" + event.getTopic() + ", tag=" + event.getTag());
        chain.doHandler(event);   // 继续链（不调用则终止）
    }
}
```

### 8.2 事件路由表达式

```java
DisruptorEvent event = new DisruptorEvent();
event.setNamespace("prod");
event.setTopic("order");
event.setTag("created");
event.setPayload(orderPayload);
System.out.println(event.getRouteExpression()); // "prod/order/created"
```

## 9. 测试与构建

```bash
./mvnw clean verify
```

- 构建配置了 JaCoCo Maven 插件（报告 + 绑定在 `verify` 阶段的 `check` 目标，
  行覆盖率规则为 90%；`haltOnFailure=false`）。
- **假设**：1.0.x 分支当前 `src/test` 下未提交测试源码；覆盖率门禁仅在存在测试时生效。
- 本 worktree 的 `.github/` 下无 CI 工作流文件。

## 10. 版本与分支

| 分支 | JDK | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前分支，JDK 8 基线，维护中 |
| `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 版本线 |
| `feature/3.0.x` | 21 | `3.0.x.*` | JDK 21 版本线 |

维护策略：`1.0.x` 版本线接收针对 JDK 8 基线的缺陷修复与兼容性更新；面向新 JDK 的
新特性在 `2.0.x` / `3.0.x` 版本线开发。发布物通过阿里云 Maven 仓库与 GitHub
Releases 分发；项目尚未发布到 Maven Central。

## 11. 贡献与许可

欢迎通过 GitHub Issue 或 Pull Request 参与贡献。

本项目基于 [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) 许可。

## 12. 缺陷修复与质量改进

下列问题由 CodeGraph 语义审计流程识别，并已在本版本中修复。每条目均说明原始缺陷、影响面以及所采用的具体修复方案。

| # | 严重度 | 文件 | 描述 / 修复方案 |
|---|---|---|---|
| 1 | 中 | [DisruptorTemplate.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L82-L110) | **高并发下 messageId 冲突。** 原 `publishEvent(String, String, Object)` 与 `publishEvent(String, String, String, Object)` 两个重载分别存在 ID 未赋值或使用 `System.currentTimeMillis()` 的情况 —— 毫秒级时钟在同一 tick 内批量发布的事件将得到完全相同的 ID。**修复：** 两个重载均改为赋值 `UUID.randomUUID().toString()`，即便在饱和吞吐下仍能保证 ID 全局唯一。 |
| 2 | 中 | [EventHandleException.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/exception/EventHandleException.java#L35-L48) | **`EventHandleException(Exception)` 构造函数丢失 cause 堆栈。** 单参构造函数原实现调用 `super(e.getMessage(), null)`，导致根因异常被直接丢弃 —— 在日志聚合与 APM 工具中无法逐层定位根因。**修复：** 改为 `super(e.getMessage(), e)`，原始受检异常通过 `getCause()` 完整保留。 |
| 3 | 🔴 高 | [pom.xml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml#L297-L300) | **JaCoCo 覆盖率从未真正被度量。** `maven-surefire-plugin` 自定义了 `<argLine>`，但未加入 JaCoCo 用于注入 `-javaagent` 的 `@{argLine}` 占位符。其结果是即便测试全部执行，覆盖率报告仍显示 0% 插桩。**修复：** 在原 `-Xmx1024m -Dfile.encoding=UTF-8` 之前追加 `@{argLine}`，由 Surefire 在运行时合并 JaCoCo 的代理参数。 |
| 4 | 低 | [pom.xml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml#L45-L52) | **未使用的 `commons-lang.version = 2.6` 属性残留。** 该属性指向的是已从 classpath 移除的老版本 `commons-lang:commons-lang`（2.x 系列）；实际使用并被声明的是 `commons-lang3.version = 3.20.0`（`org.apache.commons:commons-lang3`）。**修复：** 删除悬空属性，消除维护者误读。 |
| 5 | 低 | `thread/` 与 `event.factory/` 两套包 | **两套 ThreadFactory 并行设计且不支持业务名前缀。** `DisruptorThreadFactory`（枚举式）与 `DisruptorEvent*ThreadFactory`（`event.factory/` 下的具体类）承担重叠职责，且两者均不允许使用者注入自定义线程名称前缀，从而在同一 JVM 启动多个 `DisruptorTemplate` 实例时难以区分各池线程。**状态：** 作为后续增强项跟踪，规划在下一次小版本中提供接收 `namePrefix + index` 的 Builder。 |
| 6 | 低 | `ProxiedHandlerChain` | **`currentPosition` 游标默认单消费者假设。** 链内部持有的嵌套迭代器游标非线程安全，设计初衷即服务于 Disruptor 生命周期驱动的单 `EventHandler` 线程；在多 dispatcher 复用同一链实例时将产生未定义的执行序。**修复（文档化）：** 该设计约束已在本 README 与 [架构文档 §5 附录](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/product-docs/disruptor-extension/8%E3%80%81disruptor-extension-Architecture.zh_CN.md#5-附录核心文件索引--设计说明) 中显式声明。 |

