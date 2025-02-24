# 🎉 EventBus - Lightweight Event System in Kotlin

**EventBus** is a **lightweight**, **flexible**, and **high-performance** event system built in **Kotlin**. It provides **synchronous and asynchronous event handling**, **priority-based event dispatching**, and **powerful filtering mechanisms** to help manage event-driven architectures efficiently.

---

## 🚀 Features

✔ **Register & Unregister Listeners**  
✔ **Synchronous & Asynchronous Event Dispatching**  
✔ **Event Priority Management**  
✔ **Advanced Event Filtering** (`ONLY`, `DERIVE`)  
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
    implementation("gg.levely.system:eventbus:1.0.1")
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
    implementation "gg.levely.system:eventbus:1.0.1"
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
interface EventExample
```

### 2️⃣ **Create an Event**
```kotlin
class TestEvent : EventExample {
    val test = "Hello, EventBus!"
}
```

### 3️⃣ **Create an Event Bus**
```kotlin
val eventBus = EventBus<EventExample>()
```

### 4️⃣ **Register a Listener**
```kotlin
eventBus.subscribe(TestEvent::class.java) { event ->
    println(event.test) // Access event data
}
```

### 5️⃣ **Publish an Event**
```kotlin
eventBus.publish(TestEvent())
```

---

## 🎯 Event Priorities

Events can be assigned a **priority** to control execution order.

```kotlin
eventBus.subscribe(TestEvent::class.java, eventListener, EventPriorities.HIGH)
```

### Available Priorities:
| Priority | Description |
|----------|------------|
| `HIGH`   | Executed first |
| `NORMAL` | Default priority |
| `LOW`    | Executed last |

---

## 🎭 Event Filtering

You can **filter events** to control how they are handled.

```kotlin
eventBus.subscribe(TestEvent::class.java, eventListener, filter = EventFilter.ONLY)
```

### Available Filters:
| Filter      | Description |
|------------|-------------|
| `ONLY`     | Listens only to the exact event type |
| `DERIVE`   | Listens to the event and its subclasses |

---

## 📊 Debug Mode & Logging

**EventBus** includes a built-in **debug mode** that logs event activities such as publishing and subscribing.

### 🔍 **Enable Debug Mode**
You can enable debug logging by setting the `enableLogger` variable to `true` when you want to track events.

```kotlin
eventBus.enableLogger = true
```

### 🔥 **Logging Events**
When debug mode is enabled, EventBus logs every **event registration, dispatch, and unsubscription**.

Example:
```kotlin
eventBus.publish(TestEvent())  // Will be logged if enableLogger = true
```

### 📌 **Logged Event Types**
- `PUBLISH`
- `PUBLISH_ASYNC`
- `SUBSCRIBE`
- `UNSUBSCRIBE`

> **Note:** EventBus uses **SLF4J** for logging, so ensure you have an **SLF4J implementation** (e.g., **Logback** or **Log4j**).

---

## 🙌 Credits

- [RealAlpha](https://github.com/RealAlphaUA)
- [Luke](https://github.com/Azodox)
- [Toky](https://github.com/TokyFR)

