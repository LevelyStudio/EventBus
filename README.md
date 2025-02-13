# EventBus

EventBus is a simple and efficient event system implementation in Kotlin, allowing event management and listeners handling both synchronously and asynchronously.

## 🚀 Features

- Registering and unregistering event listeners
- Managing event priorities
- Synchronous and asynchronous event dispatching
- Filtering events based on their type

---

## 📦 Installation

This library is published on **[GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)**. To use it in your project, follow these steps:

### 🛠️ Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/LevelyStudio/EventBus")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    modImplementation("gg.levely.system:eventbus:1.0.0")
}
```

### 🛠️ Gradle (Groovy DSL)

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/LevelyStudio/EventBus")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    modImplementation "gg.levely.system:eventbus:1.0.0"
}
```

### 🔑 Authentication
GitHub Packages requires authentication. You need to set up your credentials in your `gradle.properties` or as environment variables:

#### **Option 1: Add to `gradle.properties`**
```properties
gpr.user=your-github-username
gpr.token=your-personal-access-token
```

#### **Option 2: Use Environment Variables**
```sh
export GITHUB_USERNAME=your-github-username
export GITHUB_TOKEN=your-personal-access-token
```

> **Note:** The GitHub token must have `read:packages` permission. 

---

## 📌 Usage

### 1️⃣ Creating the Event Bus

```kotlin
val eventBus = EventBus<Any>()
```

### 2️⃣ Creating an Event

```kotlin
class CustomEvent(val message: String)
```

### 3️⃣ Registering a Listener

- **With Lambda**
```kotlin
eventBus.subscribe(CustomEvent::class.java, EventListener { event ->
    println("Event received: ${event.message}")
})
```

- **With an EventListener object**
```kotlin
eventBus.subscribe(CustomEvent::class.java, CustomEventListener())
```

### 4️⃣ Publishing an Event

- **Synchronously**
```kotlin
eventBus.publish(CustomEvent("Hello, EventBus!"))
```

- **Asynchronously**
```kotlin
eventBus.publishAsync(CustomEvent("Hello, EventBus!"))
```

### 5️⃣ Unregistering a Listener

```kotlin
eventBus.unsubscribe(CustomEvent::class.java, EventListener { event ->
    println("Event unsubscribe: ${event.message}")
})
```

### 🎯 Event Priorities

Events can be registered with a specific priority:

```kotlin
eventBus.subscribe(CustomEvent::class.java, eventListener, EventPriorities.HIGH)
```

---

## 🙌 Credits

- [Luke](https://github.com/Azodox)
- [RealAlpha](https://github.com/RealAlphaUA)
- [Toky](https://github.com/TokyFR)
