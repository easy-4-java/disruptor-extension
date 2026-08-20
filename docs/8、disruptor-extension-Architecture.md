# disruptor-extension Architecture

> **文档说明**：disruptor-extension 系统架构设计，包含工程定位、物理目录拓扑、事件发布调用链与处理器链执行流的 CodeGraph 语义分析。
>
> **版本**：V3.0.x
> **最后更新**：2026-08-20

---

## Table of Contents

- [1. Engineering Positioning](#1-engineering-positioning)
- [2. Physical Directory Topology](#2-physical-directory-topology)
- [3. Event Publish Path (CodeGraph)](#3-event-publish-path-codegraph)
- [4. Handler Chain Execution Path (CodeGraph)](#4-handler-chain-execution-path-codegraph)
- [5. Appendix: Core File References](#5-appendix-core-file-references)

---

## 1. Engineering Positioning

`disruptor-extension` is an **industrial-grade extension library** built on top of the [LMAX Disruptor 4.0.0](https://github.com/LMAX-Exchange/disruptor) lock-free ring buffer. Architecturally, it draws from **Apache Shiro FilterChain** design philosophy, layering a complete **Event Routing + Chain-of-Responsibility + AOP Advice** processing framework over Disruptor's raw event loop abstraction.

The project is a **pure Java library** (`packaging: jar`): it does **not** bundle Spring Boot auto-configuration and does **not** require any container. It ships 36 core classes / interfaces under `com.lmax.disruptor.*` (a deliberate package alignment with Disruptor's own namespace, enabling access to package-visible members) and pairs with 31 unit test classes for 1:1 coverage.

### 1.1 Core Capability Matrix

| Dimension | Mechanism | Key Classes |
| :--- | :--- | :--- |
| Event Carrier | 3-level routing metadata + arbitrary Payload | [DisruptorEvent.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/DisruptorEvent.java#L52-L67) |
| Event Publishing | Template pattern + 3 Translator overloads | [DisruptorTemplate.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L38-L104) |
| Handler Chain | Chain-of-Responsibility + nested proxy | [ProxiedHandlerChain.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/ProxiedHandlerChain.java#L41-L101) |
| Route Matching | AntPathMatcher + dual configuration (Annotation / INI) | [PathMatchingHandlerChainResolver.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/PathMatchingHandlerChainResolver.java#L42-L151) |
| AOP Advice | `preHandle` / `postHandle` / `afterCompletion` lifecycle | [AbstractAdviceEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractAdviceEventHandler.java#L38-L156) |
| Build Baseline | JDK 21 + Maven 4.1.0 model + JaCoCo 90% gate | [pom.xml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml#L2-L524) |

### 1.2 Scope Boundaries

What this library **is**:

- The reference engine behind the "classified async event processing" pattern: events are published with `{namespace, topic, tag}` metadata and dispatched to per-rule handler chains resolved by Ant-style route matching.
- A drop-in companion for any codebase that already uses LMAX Disruptor and wants to add Shiro-like chain semantics without re-inventing the dispatcher.

What this library is **not**:

- Not the LMAX Disruptor core itself — `com.lmax:disruptor` (4.0.0) is a regular dependency.
- Not a Spring Boot starter — no auto-configuration is bundled; the downstream `disruptor-spring-boot-starter` (if present) owns wiring.
- Not a standalone message broker — all processing stays inside one JVM ring buffer.

### 1.3 Typical Scenarios

| Scenario | Building Blocks Used |
| :--- | :--- |
| Publish events by topic / tag / namespace | `DisruptorTemplate.publishEvent(...)` + one/two/three-arg translators |
| Route events to per-rule handler chains | `@EventRule` + `PathMatchingHandlerChainResolver` + `DisruptorEventDispatcher` |
| Classified consumers (MQ-style consumption) | One `DisruptorHandler` chain per route pattern |
| Fine-tune ring-buffer threads / wait policies | `DisruptorEvent*ThreadFactory` family + `WaitStrategys` |

---

## 2. Physical Directory Topology

```
src/main/java/com/lmax/disruptor/
├── DisruptorTemplate.java                      # [Facade] Publishing template (3 overloads)
├── annotation/
│   └── EventRule.java                           # [Declarative] Class-level Ant route rule (@Inherited)
├── config/
│   ├── EventHandlerDefinition.java              # [Config] Chain-definition DTO
│   └── Ini.java                                 # [Config] INI-format parser (612 LOC)
├── event/
│   ├── DisruptorEvent.java                      # [Model] 3-level routing carrier: ns/topic[/tag]
│   ├── DisruptorEventFactory.java               # [Factory] RingBuffer pre-allocation
│   ├── DisruptorEventPublisher.java             # [SPI] Publishing interface
│   ├── DisruptorEventPublisherAware.java        # [SPI] Publisher injection awareness
│   ├── factory/                                 # → 5 ThreadFactory implementations
│   │   ├── DisruptorEventDaemonThreadFactory.java
│   │   ├── DisruptorEventLoggerThreadFactory.java
│   │   ├── DisruptorEventMaxPriorityThreadFactory.java
│   │   ├── DisruptorEventThreadFactory.java
│   │   └── DisruptorEventWorkerThreadFactory.java
│   ├── handler/                                 # → Handler core (7 abstractions)
│   │   ├── DisruptorHandler.java                # [SPI] Handler interface (L42: doHandler)
│   │   ├── DisruptorEventDispatcher.java        # [Entry] Disruptor → Chain bridge
│   │   ├── AbstractNameableEventHandler.java
│   │   ├── AbstractEnabledEventHandler.java
│   │   ├── AbstractAdviceEventHandler.java     # Advice layer (AOP)
│   │   ├── AbstractPathMatchEventHandler.java  # Path-match layer
│   │   ├── AbstractRouteableEventHandler.java  # Chain-resolver layer
│   │   ├── Nameable.java
│   │   ├── NamedHandlerList.java
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
│   └── translator/                              # → 3 EventTranslator variants
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
    ├── StringUtils.java                         # commons-lang3 + path / pinyin / CJK helpers
    └── WaitStrategys.java
```

### 2.1 Package-to-Responsibility Mapping

| Package | Responsibility | Class Count |
| :--- | :--- | ---: |
| `com.lmax.disruptor` | Root facade — `DisruptorTemplate` | 1 |
| `com.lmax.disruptor.annotation` | Declarative routing — `@EventRule` | 1 |
| `com.lmax.disruptor.config` | INI parser + chain-definition DTO | 2 |
| `com.lmax.disruptor.event` | Event model, factory, publisher SPI | 4 |
| `com.lmax.disruptor.event.factory` | 5 named ThreadFactory variants | 5 |
| `com.lmax.disruptor.event.handler` | 5-level handler abstraction + dispatcher | 10 |
| `com.lmax.disruptor.event.handler.chain` | Chain SPI + proxy implementation | 4 |
| `com.lmax.disruptor.event.handler.chain.def` | Default manager + Ant resolver | 3 |
| `com.lmax.disruptor.event.translator` | 3 arities of EventTranslator | 3 |
| `com.lmax.disruptor.exception` | Runtime exception wrapper | 1 |
| `com.lmax.disruptor.hooks` | JVM shutdown hook | 1 |
| `com.lmax.disruptor.thread` | Thread-factory + Wait-strategy enums | 2 |
| `com.lmax.disruptor.util` | String / Path / Wait-strategy helpers | 4 |
| **Total** | | **37** |

---

## 3. Event Publish Path (CodeGraph)

The publish path has **5 stages**, traced end-to-end from `DisruptorTemplate` through the LMAX Disruptor ring buffer to the first handler entry point. Stage references are linked to the exact source lines.

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                       STAGE 1 — Publishing Entry (3 overloads)                    │
└──────────────────────────────────────┬───────────────────────────────────────────┘
                                       │
[DisruptorTemplate.publishEvent]       │ 3 public overload signatures:
  ├── L64:  (DisruptorEvent event)     │   • publishEvent(DisruptorEvent)
  ├── L76:  (topic, tag, payload)      │   • publishEvent(topic, tag, payload)
  └── L94:  (topic, ns, tag, payload)  │   • publishEvent(topic, namespace, tag, payload)
                                       │
                                       ▼
                    ┌─────────────────────────────────────────────┐
                    │ STAGE 2 — Translator slot fill              │
                    │ DisruptorEventOneArgTranslator              │
                    │   translateTo() [L44-L52]                    │
                    │   • messageId fallback → RingBuffer sequence│
                    │   • copies all 6 fields into pre-allocated   │
                    │     DisruptorEvent slot                      │
                    └──────────────────┬──────────────────────────┘
                                       │
                                       ▼
                    ┌─────────────────────────────────────────────┐
                    │ STAGE 3 — LMAX Disruptor native publish     │
                    │   dsl.Disruptor.publishEvent(translator,    │
                    │                                eventWrapper)│
                    │   → claims sequence;                      │
                    │   → memory-fence publish;                  │
                    │   → wakes consumers via WaitStrategy        │
                    └──────────────────┬──────────────────────────┘
                                       │
                                       ▼
                    ┌─────────────────────────────────────────────┐
                    │ STAGE 4 — Consumer-side EventHandler        │
                    │ DisruptorEventDispatcher.onEvent()          │
                    │   [L64-L72] (implements EventHandler<DE>)   │
                    │   • new ProxiedHandlerChain(original, handlers)
                    │   • this.doHandler(event, chain) call       │
                    └──────────────────┬──────────────────────────┘
                                       │
                                       ▼
                    ┌─────────────────────────────────────────────┐
                    │ STAGE 5 — Hand-off to Chain Execution Path  │
                    │   → Section 4 below                         │
                    └─────────────────────────────────────────────┘
```

### 3.1 Critical Call Sites (clickable references)

| Stage | Call Site | File & Line |
| :--- | :--- | :--- |
| 1 | `publishEvent(DisruptorEvent)` — raw event passthrough | [DisruptorTemplate.java#L56-L72](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L56-L72) |
| 1 | `publishEvent(topic, ns, tag, payload)` — 4-arg convenience that fills `System.currentTimeMillis()` as messageId | [DisruptorTemplate.java#L87-L105](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L87-L105) |
| 2 | `DisruptorEventOneArgTranslator.translateTo` — copies wrapper → pre-allocated slot; sequence used as fallback messageId | [DisruptorEventOneArgTranslator.java#L36-L53](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/translator/DisruptorEventOneArgTranslator.java#L36-L53) |
| 4 | `DisruptorEventDispatcher.onEvent` — Disruptor EventHandler bridge; instantiates ProxiedHandlerChain per event | [DisruptorEventDispatcher.java#L57-L73](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorEventDispatcher.java#L57-L73) |
| 4 | `DisruptorEventDispatcher.doHandler` — delegates down the 5-level handler hierarchy | [DisruptorEventDispatcher.java#L111-L125](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorEventDispatcher.java#L111-L125) |

---

## 4. Handler Chain Execution Path (CodeGraph)

The handler subsystem is built as a **5-level abstract class stack** plus **one adapter** (Dispatcher). Each level adds exactly **one cross-cutting concern**; Liskov substitutability is preserved at every transition. CodeGraph confirms each subclass overrides exactly one `doHandlerInternal`/lifecycle method from its parent and delegates the pre/post work to `super`.

### 4.1 Five-Level Handler Inheritance Stack

```
DisruptorHandler<T extends DisruptorEvent>              ← [SPI Interface]
    ▲                                                   │
    │                                                   └─ doHandler(T event, HandlerChain<T> chain) throws Exception
    │
AbstractNameableEventHandler<T extends DisruptorEvent>  ← [L1 · Base]
    ▲                                                   │ Injects + stores handler name (Nameable impl)
    │
AbstractEnabledEventHandler<T extends DisruptorEvent>   ← [L2 · Switch]
    │                                                   │ doHandler() → if (!enabled) short-circuits to chain
    │                                                   │ ↓ abstract doHandlerInternal(T, Chain)
    ▲
    │
AbstractAdviceEventHandler<T extends DisruptorEvent>    ← [L3 · AOP Advice]
    │                                                   │ doHandlerInternal() wraps sub-call in:
    │                                                   │   preHandle → executeChain → postHandle
    │                                                   │   finally { cleanup → afterCompletion }
    ▲
    │
AbstractPathMatchEventHandler<T extends DisruptorEvent> ← [L4 · Routing]
    │                                                   │ preHandle() iterates appliedPaths;
    │                                                   │   AntPathMatcher first-match wins;
    │                                                   │   → onPreHandle(event) hook on match
    ▲
    │
AbstractRouteableEventHandler<T extends DisruptorEvent> ← [L5 · Chain Resolver]
    │                                                   │ doHandlerInternal() try{…} → wraps in EventHandleException
    │                                                   │ executeChain() → resolver.getChain(event, chain)
    ▲                                                   │
    │
DisruptorEventDispatcher                                ← [Adapter · Entry Point]
        implements EventHandler<DisruptorEvent>         │ onEvent() → doHandler() bootstrap
```

### 4.2 HandlerChain Proxy Nesting Model

`ProxiedHandlerChain` implements the **nested cursor pattern**: it wraps an *original* chain plus a *handler list*, and exposes a position cursor that advances as each handler calls `chain.doHandler(event)`. This mirrors exactly how `javax.servlet.FilterChain` works in the Servlet API.

```
[HandlerChain<T>  interface]
    ▲
    │
[ProxiedHandlerChain<T>]
    │
    ├─ Fields
    │    ├── originalChain  : HandlerChain<T>   (nested proxy / tail fallback)
    │    ├── handlers       : List<DisruptorHandler<T>> (this frame's handlers)
    │    └── currentPosition: int              (per-frame cursor, NOT threadsafe)
    │
    └─ doHandler(T event)
         ├─ IF handlers == null OR position == handlers.size
         │      → originalChain.doHandler(event)     (tail pass-through)
         └─ ELSE
                → handlers.get(currentPosition++)
                       .doHandler(event, this)       (dispatch Nth handler)
                                                         │
                                                         ▼
                                              Handler body: any business logic
                                              Chain control: call chain.doHandler(event)
                                              to advance; omit to short-circuit.
```

### 4.3 Chain Build + Resolve Flow

```
DefaultHandlerChainManager.createChain(chainName, "h1,h2,h3")
  │
  ├─ splitChainDefinition() → StringUtils.splits(",") → ["h1","h2","h3"]
  └─ for token: addToChain(chainName, token)
       ├─ getHandler(token) → throws IAE if not registered
       └─ ensureChain(chainName) → DefaultNamedHandlerList.add(handler)

PathMatchingHandlerChainResolver.getChain(event, originalChain)
  │
  ├─ handlerChainManager.getChainNames() → iterate all registered chain names
  ├─ pathMatches(pattern, event.getRouteExpression()) → AntPathMatcher.match
  └─ FIRST MATCH wins:
       handlerChainManager.proxy(originalChain, pathPattern)
          → configured.proxy(original)
              → new ProxiedHandlerChain(originalChain, handlers)
```

### 4.4 Critical Call Sites (clickable references)

| Level / Concern | Method | File & Line |
| :--- | :--- | :--- |
| SPI Contract | `DisruptorHandler.doHandler(T, HandlerChain<T>)` | [DisruptorHandler.java#L40-L44](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorHandler.java#L40-L44) |
| L2 Enabled-gate | `AbstractEnabledEventHandler.doHandler` — short-circuit logic | [AbstractEnabledEventHandler.java#L61-L79](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractEnabledEventHandler.java#L61-L79) |
| L3 AOP Wrapper | `AbstractAdviceEventHandler.doHandlerInternal` — pre/executeChain/post + finally cleanup | [AbstractAdviceEventHandler.java#L87-L157](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractAdviceEventHandler.java#L87-L157) |
| L4 Ant Routing | `AbstractPathMatchEventHandler.preHandle` — appliedPaths first-match loop | [AbstractPathMatchEventHandler.java#L100-L129](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractPathMatchEventHandler.java#L100-L129) |
| L5 Exception Wrap | `AbstractRouteableEventHandler.doHandlerInternal` — catch → EventHandleException | [AbstractRouteableEventHandler.java#L71-L90](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractRouteableEventHandler.java#L71-L90) |
| Proxy Chain Advance | `ProxiedHandlerChain.doHandler` — cursor-advance + nested fallback | [ProxiedHandlerChain.java#L78-L102](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/ProxiedHandlerChain.java#L78-L102) |
| Chain Resolution | `PathMatchingHandlerChainResolver.getChain` — first-match Ant resolver | [PathMatchingHandlerChainResolver.java#L107-L153](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/PathMatchingHandlerChainResolver.java#L107-L153) |
| Chain Build | `DefaultHandlerChainManager.createChain` — comma-separated tokens → NamedHandlerList | [DefaultHandlerChainManager.java#L139-L198](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/DefaultHandlerChainManager.java#L139-L198) |

---

## 5. Appendix: Core File References

### 5.1 Build & CI

- Build manifest — [pom.xml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml)
- CI workflow (feature/3.0.x, JDK 21 + Temurin) — [.github/workflows/ci.yml](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/.github/workflows/ci.yml)

### 5.2 Top-Level Entry Points

- Publishing Facade — [DisruptorTemplate.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java)
- Disruptor → Framework Adapter — [DisruptorEventDispatcher.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorEventDispatcher.java)
- Route Carriers — [DisruptorEvent.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/DisruptorEvent.java) + [DisruptorEventFactory.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/DisruptorEventFactory.java)

### 5.3 SPI Contracts

- Handler SPI — [DisruptorHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/DisruptorHandler.java)
- Chain SPI — [HandlerChain.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/HandlerChain.java) + [HandlerChainManager.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/HandlerChainManager.java) + [HandlerChainResolver.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/HandlerChainResolver.java)
- Publishing SPI — [DisruptorEventPublisher.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/DisruptorEventPublisher.java)
- Declarative Routing SPI — [EventRule.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/annotation/EventRule.java)

### 5.4 Reference Implementations

- Chain-of-Responsibility Proxy — [ProxiedHandlerChain.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/ProxiedHandlerChain.java)
- Default Chain Manager (LinkedHashMap-ordered) — [DefaultHandlerChainManager.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/DefaultHandlerChainManager.java)
- Ant-style Chain Resolver — [PathMatchingHandlerChainResolver.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/def/PathMatchingHandlerChainResolver.java)
- 5-level Handler Abstractions — [AbstractNameableEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractNameableEventHandler.java), [AbstractEnabledEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractEnabledEventHandler.java), [AbstractAdviceEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractAdviceEventHandler.java), [AbstractPathMatchEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractPathMatchEventHandler.java), [AbstractRouteableEventHandler.java](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/AbstractRouteableEventHandler.java)

### 5.5 Bug Fixes & Design Notes (this release)

This subsection records concrete fixes and design decisions captured after the
CodeGraph semantic audit (see the project README §12 for the full severity
matrix). Items marked **(Doc)** are design constraints that are intentionally
left in the code and surfaced only in documentation.

| # | Title | Status | Notes |
|---|---|---|---|
| 1 | `DisruptorTemplate.publishEvent(...)` — `messageId` timestamp collision under concurrency | ✅ Fixed | Both 3-arg and 4-arg convenience overloads now assign `UUID.randomUUID().toString()`; the 1-arg form that accepts a caller-constructed `DisruptorEvent` is intentionally left untouched so users can preserve an externally-provided ID. See [DisruptorTemplate.java#L82-L110](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/DisruptorTemplate.java#L82-L110). |
| 2 | `EventHandleException(Exception)` — dropped root-cause stack | ✅ Fixed | Constructor now calls `super(msg, e)` so `getCause()` returns the original checked exception. See [EventHandleException.java#L35-L48](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/exception/EventHandleException.java#L35-L48). |
| 3 | `maven-surefire-plugin` — JaCoCo `@{argLine}` not merged (coverage always 0%) | ✅ Fixed | Prepended `@{argLine}` to the existing Surefire arg line so the JaCoCo agent JVM arg is appended when the agent is active. See [pom.xml#L297-L300](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml#L297-L300). |
| 4 | Dangling `commons-lang.version` property (2.x, never used) | ✅ Fixed | Removed; classpath keeps only `commons-lang3.version` (3.20.0). See [pom.xml#L45-L52](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/pom.xml#L45-L52). |
| 5 | Dual ThreadFactory subsystems without business-name prefix | 🗓 Tracked | The enum-based `DisruptorThreadFactory` in `thread/` and the 5 concrete classes in `event/factory/` carry overlapping responsibilities and neither accepts a caller-chosen thread-name prefix. A builder-based refactor (`DisruptorThreadFactory.builder().namePrefix("order-svc").daemon(true).build()`) is scoped for the next minor release; no API change is made in this patch so dependent code is unaffected. |
| 6 | **(Doc)** `ProxiedHandlerChain.currentPosition` — single-consumer assumption | 📌 Documented | The nested cursor in `ProxiedHandlerChain` is a plain mutable `int` field advanced on each `doHandler` invocation; no atomic or `ThreadLocal` guard exists. This is the intentional Disruptor model: the handler chain is driven by the single `EventHandler` thread that the ring-buffer worker pool assigns to each `EventHandler`. **If you re-use one handler chain instance across multiple independent `DisruptorEventDispatcher`s, or share it across multiple `handleEventsWith` registrations on the same Disruptor, the chain cursor becomes unsynchronized and execution order is undefined.** Recommended remedy: always build a fresh `ProxiedHandlerChain` (or a fresh `HandlerChainResolver.getChain(event, chain)` call) per dispatcher. See [ProxiedHandlerChain.java#L60-L102](file:///Users/wandl/workspaces/workspace-github-easy-4-java/disruptor-extension/src/main/java/com/lmax/disruptor/event/handler/chain/ProxiedHandlerChain.java#L60-L102) for the cursor field and its use site. |

---

**文档版本**：V3.0.x  
**创建日期**：2026-08-20  
**最后更新**：2026-08-20  
**文档状态**：✅ 待评审
