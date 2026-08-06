# disruptor-extension

[![Java](https://img.shields.io/badge/Java-21-orange)] [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

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

[English](./README.md) | [简体中文](./README.zh-CN.md)

## 1. 项目概述

`disruptor-extension` 为 [LMAX Disruptor](https://github.com/LMAX-Exchange/disruptor)
ring buffer 扩展出分类化、基于责任链的异步事件处理所需的基础设施：

- **发布** — `DisruptorTemplate`（按 topic / tag / namespace 发布）与
  `DisruptorEventPublisher`；一 / 二 / 三参数 translator。
- **路由** — `DisruptorEvent` 携带路由表达式（`namespace/topic[/tag]`）；
  `@EventRule` 注解声明处理链服务的 Ant 风格规则
  （`/Event-DC-Output/TagA-Output/**`）。
- **处理链** — `DisruptorHandler` / `HandlerChain` SPI，配合
  `HandlerChainManager`（含 `DefaultHandlerChainManager`）、`NamedHandlerList`、
  `PathMatchingHandlerChainResolver`、`ProxiedHandlerChain` 与
  `DisruptorEventDispatcher`，以及可复用的抽象处理器（advice、enabled、nameable、
  path-match、routeable）。
- **基础设施** — 线程工厂、等待策略常量（`WaitStrategys`）、`AntPathMatcher`、
  `EventHandleException`、`DisruptorShutdownHook`、`Ini` / `EventHandlerDefinition`
  配置辅助。

这是姊妹模块 `disruptor-biz` 所描述"分类异步处理"模式的引擎
（如 `/Event-DC-Output/TagA-Output/** = inDbPostHandler`、
`/Event-DC-Output/TagB-Output/** = smsPostHandler`）。

它不是：

- LMAX Disruptor 核心本身——`com.lmax:disruptor`（3.4.4）是常规依赖。
- Spring Boot starter——不提供自动装配。

典型场景：

| 场景 | 使用内容 |
| :--- | :--- |
| 按 topic / tag / namespace 发布事件 | `DisruptorTemplate.publishEvent(...)` |
| 把事件路由到各规则对应的处理链 | `@EventRule` + `PathMatchingHandlerChainResolver` + `DisruptorEventDispatcher` |
| 分类消费者（类似消息队列消费） | 每条规则一条 `DisruptorHandler` 链 |
| 自定义 ring buffer 线程 / 等待策略 | `DisruptorEventThreadFactory` 系列、`WaitStrategys` |

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

关键包：

| 包 | 内容 |
| :--- | :--- |
| `com.lmax.disruptor` | `DisruptorTemplate` |
| `com.lmax.disruptor.annotation` | `EventRule` |
| `com.lmax.disruptor.event` | `DisruptorEvent`、`DisruptorEventFactory`、`DisruptorEventPublisher(Aware)` |
| `com.lmax.disruptor.event.handler` | `DisruptorHandler`、`DisruptorEventDispatcher`、抽象处理器、`Nameable`、`PathProcessor` |
| `com.lmax.disruptor.event.handler.chain` | `HandlerChain(Manager/Resolver)`、`ProxiedHandlerChain`、定义类 |
| `com.lmax.disruptor.event.translator` | 一 / 二 / 三参数 translator |
| `com.lmax.disruptor.event.factory` / `thread` / `util` | 线程工厂、`DisruptorWaitStrategy`、`WaitStrategys`、`AntPathMatcher`、`StringUtils` |
| `com.lmax.disruptor.hooks` / `exception` / `config` | `DisruptorShutdownHook`、`EventHandleException`、`Ini`、`EventHandlerDefinition` |

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
