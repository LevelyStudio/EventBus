# 🎉 EventBus - Lightweight Event System in Kotlin

**EventBus** is a **lightweight**, **flexible**, and **high-performance** event system built in **Kotlin**. It provides
**synchronous event handling**, **priority-based event dispatching**, **powerful filtering mechanisms**, and **dynamic event branches**
to help manage event-driven architectures efficiently.

---

## 🚀 Features

✔ **Synchronous Event Dispatching**  
✔ **Thread-Safe Event Handling** (using ConcurrentSkipListSet)  
✔ **Priority-Based Listener Execution**  
✔ **Advanced Event Filtering** (`exact()`, `hierarchy()`, custom filters with logical operators)  
✔ **Event Branches** (Detachable/Reattachable listener groups with hierarchy support)  
✔ **Lambda & Reified Type Support** for concise event subscription  
✔ **Integrated Logging System (SLF4J)**  
✔ **Debug Mode for Event Tracking**

---

## 📦 Installation

This library is available on **[GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)**.

### 🛠️ Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/LevelyStudio/EventBus")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USER")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("gg.levely.system:eventbus:2.0.0")
}
```

### 🛠️ Gradle (Groovy DSL)

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/LevelyStudio/EventBus")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USER")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation "gg.levely.system:eventbus:2.0.0"
}
```

---

## 🔑 Authentication

GitHub Packages requires authentication. Set up your credentials in `gradle.properties` or as environment variables.

#### **Option 1: Add to `gradle.properties`**

```properties
gpr.user=your-github-username
gpr.token=your-personal-access-token
```

#### **Option 2: Use Environment Variables**

```sh
export GITHUB_USER=your-github-username
export GITHUB_TOKEN=your-personal-access-token
```

> **Note:** The GitHub token must have `read:packages` permission.

---

## 🚀 Getting Started

### 1️⃣ **Define an Event Interface**

```kotlin
interface GameEvent
```

### 2️⃣ **Create Event Classes**

```kotlin
data class Player(val name: String, val balance: Double)
data class PlayerJoinEvent(val player: Player) : GameEvent
data class PlayerLeaveEvent(val player: Player) : GameEvent
```

### 3️⃣ **Create an Event Bus**

```kotlin
val eventBus = EventBus<GameEvent>()
```

### 4️⃣ **Register a Listener**

Using lambda syntax with reified type:

```kotlin
eventBus.subscribe<PlayerJoinEvent> { event ->
    println("Player joined: ${event.player.name}")
}
```

Or using the traditional class-based approach:

```kotlin
eventBus.subscribe(PlayerJoinEvent::class.java) { event ->
    println("Player joined: ${event.player.name}")
}
```

### 5️⃣ **Publish an Event**

```kotlin
val player = Player("Alice", 2000.0)
eventBus.publish(PlayerJoinEvent(player))
```

---

## 🎯 Event Priorities

Events can be assigned a **priority** to control execution order. Higher priority listeners execute first.

```kotlin
eventBus.subscribe<PlayerJoinEvent>(priority = EventPriority.HIGHEST) { event ->
    println("High priority handler: ${event.player.name}")
}

eventBus.subscribe<PlayerJoinEvent>(priority = EventPriority.LOW) { event ->
    println("Low priority handler: ${event.player.name}")
}
```

### Available Priorities:

| Priority  | Weight | Description         |
|-----------|--------|---------------------|
| `HIGHEST` | 1000   | Executed first      |
| `HIGH`    | 500    | High priority       |
| `NORMAL`  | 0      | Default priority    |
| `LOW`     | -500   | Low priority        |
| `LOWEST`  | -1000  | Executed last       |

### Custom Priorities:

You can create custom priorities with specific weights:

```kotlin
val customPriority = EventPriority.of("CRITICAL", 2000)
val beforeNormal = EventPriority.before(EventPriority.NORMAL, gap = 10)
val afterHigh = EventPriority.after(EventPriority.HIGH, gap = 5)
```

---

## 🎭 Event Filtering

You can **filter events** to control how they are handled using **EventFilters**.

### Using Lambda Filters:

```kotlin
eventBus.subscribe<PlayerJoinEvent> { event ->
    println("Player joined: ${event.player.name}")
}
```

### Advanced Filtering with Custom Conditions:

```kotlin
interface TransactionEvent : GameEvent

data class ProcessTransactionEvent(
    val source: Player,
    val target: Player,
    val amount: Double
) : TransactionEvent

// Filter high-value transactions
val filterHighTransaction = EventFilters.exact<ProcessTransactionEvent>() and EventFilters.filter { event ->
    event.amount > 1_000_000.0
}

eventBus.subscribe<ProcessTransactionEvent>(filter = filterHighTransaction) { event ->
    println("Processing high-value transaction of ${event.amount}")
}
```

### Available Filter Methods:

| Filter Method | Description                                          |
|---------------|------------------------------------------------------|
| `exact()`     | Matches only the exact event type                    |
| `hierarchy()` | Matches the event type and its subclasses            |
| `filter()`    | Custom filter with a predicate                       |
| `all()`       | Matches all events                                   |
| `none()`      | Matches no events                                    |

### Combining Filters:

You can combine filters using logical operators:

```kotlin
val complexFilter = EventFilters.exact<ProcessTransactionEvent>() and EventFilters.filter { event ->
    event.amount > 500.0
}

val orFilter = filter1 or filter2
val notFilter = !someFilter
```

---

## 🌿 Event Branches

**Event Branches** allow you to create isolated groups of event listeners that can be **attached** or **detached** dynamically. This is useful for managing temporary event handlers or modular event systems.

### Creating a Branch:

```kotlin
val customBranch = eventBus.branch() {
    subscribe<PlayerJoinEvent> { event ->
        println("Player joined: ${event.player}")
    }
}
```

### Branch Operations:

```kotlin
// Get the branch path
println("Path: ${customBranch.getPath()}")

// Detach the branch (temporarily disable all its listeners)
customBranch.detach()

// Events published while detached won't be handled by this branch
eventBus.publish(PlayerJoinEvent(player))

// Reattach the branch (re-enable all its listeners)
customBranch.reattach()

// Now events will be handled again
eventBus.publish(PlayerJoinEvent(player))
```

### Named Branches:

```kotlin
val namedBranch = eventBus.branch("my-custom-branch") {
    subscribe<PlayerLeaveEvent> { event ->
        println("Player left: ${event.player.name}")
    }
}
```

### Branch Hierarchy:

Branches can have child branches, creating a tree structure:

```kotlin
val parentBranch = eventBus.branch("parent")
val childBranch = parentBranch.branch("child")

// Get hierarchical path: "parent/child"
println(childBranch.getPath())
```

> **Note**: When a parent branch is detached, all its children are also detached.

---

## 📊 Debug Mode & Logging

**EventBus** includes a built-in **debug mode** that logs event activities such as publishing and subscribing.

### 🔍 **Enable Debug Mode**

You can enable debug logging by passing `enableLogger = true` to the EventBus constructor:

```kotlin
val eventBus = EventBus<GameEvent>(enableLogger = true)
```

### 🔥 **Logging Events**

When debug mode is enabled, EventBus logs every **event registration, dispatch, and unsubscription**.

Example:

```kotlin
val eventBus = EventBus<GameEvent>(enableLogger = true)

eventBus.subscribe<PlayerJoinEvent> { event ->
    println("Player joined: ${event.player.name}")
}

eventBus.publish(PlayerJoinEvent(Player("Alice", 2000.0)))  // Will be logged
```

### 📌 **Logged Event Types**

- `PUBLISH`
- `SUBSCRIBE`
- `UNSUBSCRIBE`

> **Note:** EventBus uses **SLF4J** for logging, so ensure you have an **SLF4J implementation** (e.g., **Logback** or **Log4j**).

---

## 🙌 Credits

- [RealAlpha](https://github.com/RealAlphaUA)
- [Luke](https://github.com/Azodox)
- [Toky](https://github.com/TokyFR)

