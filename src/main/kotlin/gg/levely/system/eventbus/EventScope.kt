package gg.levely.system.eventbus

import gg.levely.system.eventbus.context.EventContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

/**
 * A scope that tracks subscriptions and child scopes.
 * Disposing a scope disposes all its subscriptions and children recursively.
 */
class EventScope(
    val name: String? = null,
    private val parent: EventScope? = null,
    private val onUnsubscribe: (EventContext<*>) -> Unit
) {
    private val contexts = ConcurrentSkipListSet(
        compareByDescending<EventContext<*>> { it.eventPriority.weight }
            .thenBy { it.hashCode() }
    )
    private val children = ConcurrentHashMap<String, EventScope>()

    @Volatile
    var isDisposed: Boolean = false
        private set

    /**
     * Creates a child scope.
     */
    fun createScope(name: String = UUID.randomUUID().toString()): EventScope {
        check(!isDisposed) { "Scope is disposed" }
        val child = EventScope(name, parent = this, onUnsubscribe)
        children[name] = child
        return child
    }

    /**
     * DSL-style scoped block.
     */
    inline fun scoped(name: String = UUID.randomUUID().toString(), block: EventScope.() -> Unit): EventScope {
        return createScope(name).apply(block)
    }

    fun getChild(name: String): EventScope? = children[name]

    internal fun track(context: EventContext<*>): Subscription {
        check(!isDisposed) { "Scope is disposed" }
        contexts.add(context)
        return Subscription {
            contexts.remove(context)
            onUnsubscribe(context)
        }
    }

    /**
     * Disposes this scope and all children recursively.
     */
    fun dispose() {
        if (isDisposed) return
        isDisposed = true

        // Dispose children first
        children.values.forEach { it.dispose() }
        children.clear()

        // Unsubscribe all tracked contexts
        contexts.forEach { onUnsubscribe(it) }
        contexts.clear()

        // Remove from parent
        parent?.children?.values?.remove(this)
    }

    fun getPath(): String {
        val parts = mutableListOf<String>()
        var current: EventScope? = this
        while (current != null) {
            current.name?.let { parts.add(0, it) }
            current = current.parent
        }
        return parts.joinToString("/").ifEmpty { "<root>" }
    }

    val subscriptionCount: Int get() = contexts.size

    val totalSubscriptionCount: Int
        get() = contexts.size + children.values.sumOf { it.totalSubscriptionCount }
}