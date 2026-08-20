# disruptor-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-21-orange)](https://github.com/easy-4-java/disruptor-extension) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

> LMAX Disruptor extension library: handler chains, event dispatcher, publishing template,
> `@EventRule` routing, and thread/wait-strategy factories. Three maintained lines:
> `feature/1.0.x` (JDK 8 + Disruptor 3.4.4), `feature/2.0.x` (JDK 17 + Disruptor 3.4.4),
> and `feature/3.0.x` (JDK 21 + Disruptor 4.0.0).

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

## 1. Project Overview — Engineering Positioning

`disruptor-extension` is an **industrial-grade extension library** built on top of
the [LMAX Disruptor 4.0.0](https://github.com/LMAX-Exchange/disruptor) lock-free ring
buffer. Architecturally it mirrors the **Apache Shiro FilterChain** design philosophy,
layering a complete **Event Routing + Chain-of-Responsibility + AOP Advice** processing
framework on top of Disruptor's raw event loop abstraction.

The project is a **pure Java library** (`packaging: jar`) — it does **not** bundle
Spring Boot auto-configuration, does **not** require any container, and ships 36 core
classes / interfaces under `com.lmax.disruptor.*` (a deliberate package alignment
with Disruptor's own namespace, enabling access to package-visible members) paired
with 31 unit-test classes for 1:1 coverage. For a full architecture reference, see
the bilingual architecture documents under [product-docs/disruptor-extension/](./product-docs/disruptor-extension/).

### 1.1 Core Capability Matrix

| Dimension | Mechanism | Key Classes |
| :--- | :--- | :--- |
| Event Carrier | 3-level routing metadata + arbitrary Payload | `DisruptorEvent` (`com.lmax.disruptor.event`) |
| Event Publishing | Template pattern + 3 Translator overloads | `DisruptorTemplate` + `event.translator.*` |
| Handler Chain | Chain-of-Responsibility + nested proxy | `ProxiedHandlerChain` (`event.handler.chain`) |
| Route Matching | AntPathMatcher + dual config (Annotation / INI) | `PathMatchingHandlerChainResolver` (`event.handler.chain.def`) |
| AOP Advice | `preHandle` / `postHandle` / `afterCompletion` | `AbstractAdviceEventHandler` (`event.handler`) |
| Build Baseline | JDK 21 + Maven 4.1.0 model + JaCoCo 90% gate | `pom.xml` + `.github/workflows/ci.yml` |

### 1.2 Scope Boundaries

This library provides the infrastructure for classified, chain-based async event
processing:

- **Publishing** — `DisruptorTemplate` (topic / tag / namespace based publishing) and
  `DisruptorEventPublisher`; one/two/three-arg translators.
- **Routing** — `DisruptorEvent` carries a route expression (`namespace/topic[/tag]`);
  the `@EventRule` annotation declares the Ant-style rule a handler chain serves.
- **Handler chains** — `DisruptorHandler` / `HandlerChain` SPI with
  `HandlerChainManager`, `NamedHandlerList`, `PathMatchingHandlerChainResolver`,
  `ProxiedHandlerChain` and `DisruptorEventDispatcher`, plus reusable abstract
  handlers (advice, enabled, nameable, path-match, routeable).
- **Infrastructure** — thread factories, wait-strategy constants (`WaitStrategys`),
  `AntPathMatcher`, `EventHandleException`, `DisruptorShutdownHook`, `Ini` /
  `EventHandlerDefinition` config helpers.

This is the engine behind the "classified async processing" pattern described in
the companion `disruptor-biz` module (e.g. `/Event-DC-Output/TagA-Output/** =
inDbPostHandler`, `/Event-DC-Output/TagB-Output/** = smsPostHandler`).

What it is **not**:

- Not the LMAX Disruptor core itself — `com.lmax:disruptor` is a regular dependency.
- Not a Spring Boot starter — no auto-configuration is provided; a downstream
  `disruptor-spring-boot-starter` (if present) owns the wiring.
- Not a standalone message broker — all processing stays inside one JVM ring buffer.

### 1.3 Typical Scenarios

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
| JDK | 21+ (current `feature/3.0.x` branch; see matrix below for other lines) |
| Maven | 4.0+ (enforced; project uses Maven 4.1.0 modelVersion) |
| LMAX Disruptor | `com.lmax:disruptor` 4.0.0 on the 3.0.x line; 3.4.4 on 1.0.x / 2.0.x |

Version-line matrix:

| Branch | JDK | Bytecode | Current SNAPSHOT | Disruptor | Compile flags |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | 52 | `1.0.x.20260630-SNAPSHOT` | 3.4.4 (`javax.ws.rs` namespace) | `maven.compiler.source/target = 1.8` |
| `feature/2.0.x` | 17 | 61 | `2.0.x.20260630-SNAPSHOT` | 3.4.4 | `maven.compiler.release = 17` + `--add-opens` |
| `feature/3.0.x` | 21 | 65 | `3.0.x.20260630-SNAPSHOT` | 4.0.0 (`jakarta.ws.rs` namespace) | `maven.compiler.release = 21` + `--add-opens` |

## 4. Architecture & Modules

### 4.1 High-Level Data Flow

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

### 4.2 Physical Directory Topology

The codebase is organised into 13 packages under `src/main/java/com/lmax/disruptor/`,
each package owning one narrow responsibility:

```
src/main/java/com/lmax/disruptor/
├── DisruptorTemplate.java                      # [Facade] Publishing template (3 overloads)
├── annotation/
│   └── EventRule.java                           # [Declarative] Class-level Ant route rule
├── config/
│   ├── EventHandlerDefinition.java              # [Config] Chain-definition DTO
│   └── Ini.java                                 # [Config] INI-format parser (612 LOC)
├── event/
│   ├── DisruptorEvent.java                      # [Model] 3-level routing: ns/topic[/tag]
│   ├── DisruptorEventFactory.java               # [Factory] RingBuffer pre-allocation
│   ├── DisruptorEventPublisher.java             # [SPI] Publishing interface
│   ├── DisruptorEventPublisherAware.java        # [SPI] Publisher injection awareness
│   ├── factory/                                 # → 5 ThreadFactory variants
│   │   ├── DisruptorEventDaemonThreadFactory.java
│   │   ├── DisruptorEventLoggerThreadFactory.java
│   │   ├── DisruptorEventMaxPriorityThreadFactory.java
│   │   ├── DisruptorEventThreadFactory.java
│   │   └── DisruptorEventWorkerThreadFactory.java
│   ├── handler/                                 # → Handler 5-layer abstraction stack
│   │   ├── DisruptorHandler.java                # [SPI] Handler interface
│   │   ├── DisruptorEventDispatcher.java        # [Entry] Disruptor → Chain bridge
│   │   ├── AbstractNameableEventHandler.java    # L1: inject handler name
│   │   ├── AbstractEnabledEventHandler.java     # L2: enabled-gate short-circuit
│   │   ├── AbstractAdviceEventHandler.java      # L3: AOP advice lifecycle
│   │   ├── AbstractPathMatchEventHandler.java   # L4: Ant-path route matching
│   │   ├── AbstractRouteableEventHandler.java   # L5: chain resolution + exception wrap
│   │   ├── Nameable.java / NamedHandlerList.java
│   │   ├── PathProcessor.java
│   │   └── chain/
│   │       ├── HandlerChain.java                # [SPI] Chain interface
│   │       ├── HandlerChainManager.java         # [SPI] Chain manager interface
│   │       ├── HandlerChainResolver.java        # [SPI] Chain resolver interface
│   │       ├── ProxiedHandlerChain.java         # Nested cursor-based proxy chain
│   │       └── def/
│   │           ├── DefaultHandlerChainManager.java
│   │           ├── DefaultNamedHandlerList.java
│   │           └── PathMatchingHandlerChainResolver.java
│   └── translator/                              # → 3 EventTranslator arities
│       ├── DisruptorEventOneArgTranslator.java
│       ├── DisruptorEventTwoArgTranslator.java
│       └── DisruptorEventThreeArgTranslator.java
├── exception/
│   └── EventHandleException.java                # Runtime exception wrapper
├── hooks/
│   └── DisruptorShutdownHook.java               # JVM shutdown hook
├── thread/
│   ├── DisruptorThreadFactory.java              # [Enum] 3 ThreadFactory styles
│   └── DisruptorWaitStrategy.java               # [Enum] 4 Wait strategies
└── util/
    ├── AntPathMatcher.java                      # Ant-style path matching (Spring-port)
    ├── PathMatcher.java
    ├── StringUtils.java
    └── WaitStrategys.java
```

Package → responsibility mapping:

| Package | Content |
| :--- | :--- |
| `com.lmax.disruptor` | `DisruptorTemplate` |
| `com.lmax.disruptor.annotation` | `EventRule` |
| `com.lmax.disruptor.event` | `DisruptorEvent`, `DisruptorEventFactory`, `DisruptorEventPublisher(Aware)` |
| `com.lmax.disruptor.event.handler` | `DisruptorHandler`, `DisruptorEventDispatcher`, 5 abstract handlers, `Nameable`, `PathProcessor` |
| `com.lmax.disruptor.event.handler.chain` | `HandlerChain(Manager/Resolver)`, `ProxiedHandlerChain`, definitions |
| `com.lmax.disruptor.event.translator` | one / two / three-arg translators |
| `com.lmax.disruptor.event.factory` / `thread` / `util` | thread factories, `DisruptorWaitStrategy`, `WaitStrategys`, `AntPathMatcher`, `StringUtils` |
| `com.lmax.disruptor.hooks` / `exception` / `config` | `DisruptorShutdownHook`, `EventHandleException`, `Ini`, `EventHandlerDefinition` |

### 4.3 Event Publish Path (CodeGraph)

The publish path has **5 stages**, traced end-to-end from `DisruptorTemplate`
through the LMAX Disruptor ring buffer to the first handler entry point. See the
architecture document under `product-docs/disruptor-extension/` for exact line
references.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                 STAGE 1 — Publishing Entry (3 overloads)                      │
└─────────────────────────────┬────────────────────────────────────────────────┘
                              │
DisruptorTemplate.publishEvent│ 3 public signatures:
  ├─ (DisruptorEvent)         │   • publishEvent(DisruptorEvent)
  ├─ (topic, tag, payload)    │   • publishEvent(topic, tag, payload)
  └─ (topic, ns, tag, payload)│   • publishEvent(topic, namespace, tag, payload)
                              │
                              ▼
          ┌───────────────────────────────────────────────┐
          │ STAGE 2 — Translator slot fill                │
          │ DisruptorEventOneArgTranslator.translateTo    │
          │   • messageId fallback → RingBuffer sequence  │
          │   • copies all 6 fields into pre-allocated    │
          │     DisruptorEvent slot                       │
          └──────────────────┬────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────────────┐
          │ STAGE 3 — LMAX Disruptor native publish       │
          │   dsl.Disruptor.publishEvent(translator, arg) │
          │   → claims sequence + memory-fence publish;   │
          │   → wakes consumers via the WaitStrategy      │
          └──────────────────┬────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────────────┐
          │ STAGE 4 — Consumer-side EventHandler          │
          │ DisruptorEventDispatcher.onEvent              │
          │   (implements EventHandler<DisruptorEvent>)   │
          │   • new ProxiedHandlerChain per event         │
          │   → delegates to this.doHandler(event, chain) │
          └──────────────────┬────────────────────────────┘
                              │
                              ▼
          ┌───────────────────────────────────────────────┐
          │ STAGE 5 — Hand-off to Handler Chain Execution │
          │   → Section 4.4 below                         │
          └───────────────────────────────────────────────┘
```

### 4.4 Handler Chain Execution Path (CodeGraph)

The handler subsystem is built as a **5-level abstract class stack** plus **one
adapter** (Dispatcher). Each level adds exactly **one cross-cutting concern**.
`ProxiedHandlerChain` implements the **nested cursor pattern** that mirrors
`javax.servlet.FilterChain` — each handler calls `chain.doHandler(event)` to
advance the position cursor, or omits it to short-circuit.

**Five-Level Handler Inheritance Stack**

```
DisruptorHandler<T extends DisruptorEvent>              ← [SPI Interface]
    ▲                                                   │ doHandler(T, Chain)
    │
AbstractNameableEventHandler<T>                        ← [L1 · Base]
    ▲                                                   │ injects handler name
    │
AbstractEnabledEventHandler<T>                         ← [L2 · Switch]
    │                                                   │ !enabled → chain.next()
    │                                                   │ ↓ doHandlerInternal()
    ▲
    │
AbstractAdviceEventHandler<T>                          ← [L3 · AOP Advice]
    │                                                   │ pre → execute → post
    │                                                   │ finally { afterCompletion }
    ▲
    │
AbstractPathMatchEventHandler<T>                       ← [L4 · Routing]
    │                                                   │ appliedPaths first-match wins
    ▲
    │
AbstractRouteableEventHandler<T>                       ← [L5 · Resolver]
    │                                                   │ resolver.getChain(event)
    │                                                   │ catch → EventHandleException
    ▲
    │
DisruptorEventDispatcher                                ← [Adapter · Entry]
        implements EventHandler<DisruptorEvent>         │ onEvent() → doHandler()
```

**HandlerChain Proxy Nesting Model**

```
ProxiedHandlerChain<T>
  ├─ Fields
  │    ├── originalChain  : HandlerChain<T>   (nested proxy / tail fallback)
  │    ├── handlers       : List<DisruptorHandler<T>> (this frame's handlers)
  │    └── currentPosition: int               (per-frame cursor)
  │
  └─ doHandler(T event)
       ├─ IF handlers == null OR position == handlers.size
       │      → originalChain.doHandler(event)    (tail pass-through)
       └─ ELSE
              → handlers.get(currentPosition++)
                     .doHandler(event, this)      (dispatch Nth handler)
                                                       │
                                                       ▼
                                            Handler body (business logic)
                                            Chain control: call chain.doHandler(event)
                                            to advance; omit to short-circuit.
```

## 5. Installation

The project is **not yet published to Maven Central**. Snapshots/releases are
distributed through the Aliyun Maven repository and GitHub Releases.

Maven:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>disruptor-extension</artifactId>
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:disruptor-extension:3.0.x.x.20260630-SNAPSHOT'
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

| Branch | JDK | Current SNAPSHOT | Notes |
| :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.20260630-SNAPSHOT` | JDK 8 baseline (`source/target=1.8`, Disruptor 3.4.4), maintained LTS |
| `feature/2.0.x` | 17 | `2.0.x.20260630-SNAPSHOT` | JDK 17 baseline (`release=17`, Disruptor 3.4.4), maintained LTS |
| `feature/3.0.x` | 21 | `3.0.x.20260630-SNAPSHOT` | **Current branch.** JDK 21 baseline (`release=21`, Disruptor 4.0.0), active development line |

Maintenance policy: bug fixes and security patches are merged bottom-up from
1.0.x → 2.0.x → 3.0.x. New JDK-21-era features land first on `feature/3.0.x` and
are selectively back-ported where compatible. Releases go to the Aliyun Maven
repository and GitHub Releases; the project is not yet published to Maven Central.

## 11. Contributing & License

Contributions are welcome — please open issues or pull requests on GitHub.

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).


