# disruptor-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-8-orange)](https://github.com/easy-4-java/disruptor-extension) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

> LMAX Disruptor extensions for the JDK 8 line: handler chains, event dispatcher,
> publishing template, `@EventRule` routing and thread/wait-strategy factories.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`disruptor-extension` extends the [LMAX Disruptor](https://github.com/LMAX-Exchange/disruptor)
ring buffer with the infrastructure needed for classified, chain-based async event
processing:

- **Publishing** — `DisruptorTemplate` (topic / tag / namespace based publishing) and
  `DisruptorEventPublisher`; one/two/three-arg translators.
- **Routing** — `DisruptorEvent` carries a route expression
  (`namespace/topic[/tag]`); the `@EventRule` annotation declares the Ant-style rule
  (`/Event-DC-Output/TagA-Output/**`) a handler chain serves.
- **Handler chains** — `DisruptorHandler` / `HandlerChain` SPI with
  `HandlerChainManager` (incl. `DefaultHandlerChainManager`), `NamedHandlerList`,
  `PathMatchingHandlerChainResolver`, `ProxiedHandlerChain` and
  `DisruptorEventDispatcher`, plus reusable abstract handlers (advice, enabled,
  nameable, path-match, routeable).
- **Infrastructure** — thread factories, wait-strategy constants (`WaitStrategys`),
  `AntPathMatcher`, `EventHandleException`, `DisruptorShutdownHook`, `Ini` /
  `EventHandlerDefinition` config helpers.

This is the engine behind the "classified async processing" pattern described in
the companion `disruptor-biz` module (e.g. `/Event-DC-Output/TagA-Output/** =
inDbPostHandler`, `/Event-DC-Output/TagB-Output/** = smsPostHandler`).

What it is **not**:

- Not the LMAX Disruptor core itself — `com.lmax:disruptor` (3.4.4) is a regular
  dependency.
- Not a Spring Boot starter — no auto-configuration is provided.

Typical scenarios:

| Scenario | What you use |
| :--- | :--- |
| Publish events by topic / tag / namespace | `DisruptorTemplate.publishEvent(...)` |
| Route events to per-rule handler chains | `@EventRule` + `PathMatchingHandlerChainResolver` + `DisruptorEventDispatcher` |
| Classified consumers (like message-queue consumption) | `DisruptorHandler` chain per rule |
| Custom ring-buffer threads / wait strategies | `DisruptorEventThreadFactory` family, `WaitStrategys` |

## 2. Features & Status

| Capability | Status | Notes |
| :--- | :--- | :--- |
| `DisruptorTemplate` | Stable | `publishEvent(DisruptorEvent)`, `publishEvent(topic, tag, payload)`, `publishEvent(topic, namespace, tag, payload)` |
| `DisruptorEvent` model | Stable | topic / tag / namespace / messageId / payload / sequence / timestamp; `getRouteExpression()` |
| Event publishers & translators | Stable | `DisruptorEventPublisher`, `DisruptorEventPublisherAware`, one/two/three-arg translators |
| Handler-chain SPI | Stable | `DisruptorHandler`, `HandlerChain`, `HandlerChainManager`, `NamedHandlerList`, `ProxiedHandlerChain` |
| Chain implementations | Stable | `DefaultHandlerChainManager`, `DefaultNamedHandlerList`, `PathMatchingHandlerChainResolver`, `DisruptorEventDispatcher` |
| Abstract handlers | Stable | `AbstractAdviceEventHandler`, `AbstractEnabledEventHandler`, `AbstractNameableEventHandler`, `AbstractPathMatchEventHandler`, `AbstractRouteableEventHandler` |
| `@EventRule` annotation | Stable | Ant-style rule expression, default `*` |
| Thread / strategy factories | Stable | 5 `DisruptorEvent*ThreadFactory` variants, `DisruptorThreadFactory`, `DisruptorWaitStrategy`, `WaitStrategys` |
| Utilities | Stable | `AntPathMatcher`, `PathMatcher`, `StringUtils`, `EventHandleException`, `DisruptorShutdownHook`, `Ini`, `EventHandlerDefinition` |

## 3. Requirements & Compatibility

| Requirement | Version / Notes |
| :--- | :--- |
| JDK | 8+ |
| Maven | 3.0+ (enforced; Maven Wrapper `./mvnw` included) |
| LMAX Disruptor | `com.lmax:disruptor` 3.4.4 (managed by this pom) |

Version lines:

| Branch | JDK | Version |
| :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. Architecture & Modules

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

Single-module Maven project (`packaging: jar`). No child modules.

| Artifact | Responsibility |
| :--- | :--- |
| `io.github.easy4j:disruptor-extension` | Publishing, routing, handler chains, dispatcher, factories, utilities |

Key packages:

| Package | Content |
| :--- | :--- |
| `com.lmax.disruptor` | `DisruptorTemplate` |
| `com.lmax.disruptor.annotation` | `EventRule` |
| `com.lmax.disruptor.event` | `DisruptorEvent`, `DisruptorEventFactory`, `DisruptorEventPublisher(Aware)` |
| `com.lmax.disruptor.event.handler` | `DisruptorHandler`, `DisruptorEventDispatcher`, abstract handlers, `Nameable`, `PathProcessor` |
| `com.lmax.disruptor.event.handler.chain` | `HandlerChain(Manager/Resolver)`, `ProxiedHandlerChain`, definitions |
| `com.lmax.disruptor.event.translator` | one / two / three-arg translators |
| `com.lmax.disruptor.event.factory` / `thread` / `util` | thread factories, `DisruptorWaitStrategy`, `WaitStrategys`, `AntPathMatcher`, `StringUtils` |
| `com.lmax.disruptor.hooks` / `exception` / `config` | `DisruptorShutdownHook`, `EventHandleException`, `Ini`, `EventHandlerDefinition` |

## 5. Installation

The project is **not yet published to Maven Central**. Snapshots/releases are
distributed through the Aliyun Maven repository and GitHub Releases.

Maven:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>disruptor-extension</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:disruptor-extension:1.0.x.20260630-SNAPSHOT'
```

## 6. Quick Start

Publish an event through the ring buffer:

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
template.publishEvent("order", "prod", "created", orderPayload); // with namespace
```

Expected result: a `DisruptorEvent` with topic `order` (namespace `prod`, tag
`created`) and the payload is published to the ring buffer; `getRouteExpression()`
yields `prod/order/created`, which the handler-chain resolver matches against
`@EventRule` patterns.

## 7. Configuration

The library has no configuration file or property prefix. Behaviour is configured
in code:

| Element | Description |
| :--- | :--- |
| `DisruptorTemplate` | Wrap a `Disruptor<DisruptorEvent>` + an `EventTranslatorOneArg`; publish by event or by topic/tag/namespace |
| `@EventRule(value)` | Ant-style rule on a handler class, e.g. `/Event-DC-Output/TagA-Output/**` (default `*`) |
| `DefaultHandlerChainManager` | `setHandlers(Map)` / `setHandlerChains(Map)` / `addHandler(name, handler)` |
| `DisruptorEventDispatcher` | Constructed with a `HandlerChainResolver` + order; dispatches events through the resolved chain |
| `WaitStrategys` | Pre-built `WaitStrategy` constants: `BLOCKING_WAIT`, `SLEEPING_WAIT`, `YIELDING_WAIT`, `BUSYSPIN_WAIT` |

## 8. Core Usage / API

### 8.1 A rule-routed handler

```java
import com.lmax.disruptor.annotation.EventRule;
import com.lmax.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.event.handler.DisruptorHandler;
import com.lmax.disruptor.event.handler.chain.HandlerChain;

@EventRule("/Event-DC-Output/TagA-Output/**")
public class InDbPostHandler implements DisruptorHandler<DisruptorEvent> {

    @Override
    public void doHandler(DisruptorEvent event, HandlerChain<DisruptorEvent> chain) throws Exception {
        // classified processing for this rule
        System.out.println("event topic=" + event.getTopic() + ", tag=" + event.getTag());
        chain.doHandler(event);   // continue the chain (or not, to stop)
    }
}
```

### 8.2 Event route expression

```java
DisruptorEvent event = new DisruptorEvent();
event.setNamespace("prod");
event.setTopic("order");
event.setTag("created");
event.setPayload(orderPayload);
System.out.println(event.getRouteExpression()); // "prod/order/created"
```

## 9. Testing & Build

```bash
./mvnw clean verify
```

- The build is configured with the JaCoCo Maven plugin (report + `check` goal with a
  90% line-coverage rule bound to the `verify` phase; `haltOnFailure=false`).
- **Assumption**: the 1.0.x branch currently checks in no test sources under
  `src/test`; coverage thresholds are therefore enforced only when tests exist.
- No CI workflow files are present under `.github/` in this worktree.

## 10. Versioning & Branches

| Branch | JDK | Version | Notes |
| :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | Current branch, JDK 8 baseline, maintained |
| `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 line |
| `feature/3.0.x` | 21 | `3.0.x.*` | JDK 21 line |

Maintenance policy: the `1.0.x` line receives bug fixes and compatibility updates
for the JDK 8 baseline. New features targeting newer JDKs land on the `2.0.x` /
`3.0.x` lines. Releases are published to the Aliyun Maven repository and as
GitHub Releases; the project is not yet published to Maven Central.

## 11. Contributing & License

Contributions are welcome — please open issues or pull requests on GitHub.

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
