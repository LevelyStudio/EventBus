package gg.levely.system.eventbus

import java.util.UUID

/**
 * Context for subscribing to events within a specific scope.
 */
class SubscribeContext<T>(
    val eventBus: EventBus<T>,
    val scope: EventScope
) {

    /**
     * Subscribes to an event of type [E] within the current scope.
     *
     * @param E The event type
     * @param clazz The class of the event type
     * @param listener The event listener
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     * @return A [Subscription] that can be used to unsubscribe
     */
    @JvmOverloads
    fun <E : T> subscribe(
        clazz: Class<E>,
        listener: EventListener<E>,
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY
    ): Subscription = eventBus.subscribe(scope, clazz, listener, priority, filter)

    /**
     * Subscribes to an event of type [E] within the current scope.
     *
     * @param E The event type
     * @param listener The event listener
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     * @return A [Subscription] that can be used to unsubscribe
     */
    inline fun <reified E : T> subscribe(
        listener: EventListener<E>,
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY
    ) : Subscription = eventBus.subscribe(scope, E::class.java, listener, priority, filter)

    /**
     * Subscribes to an event of type [E] within the current scope.
     *
     * @param E The event type
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     * @param listener The event listener
     * @return A [Subscription] that can be used to unsubscribe
     */
    inline fun <reified E : T> subscribe(
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY,
        listener: EventListener<E>
    ): Subscription = eventBus.subscribe(scope, E::class.java, listener, priority, filter)

    /**
     * Creates a new child scope within the current scope.
     *
     * @param name Optional name for the new scope
     * @return The newly created [EventScope]
     */
    @JvmOverloads
    fun createScope(name: String = UUID.randomUUID().toString()): EventScope = scope.createScope(name)

    /**
     * DSL-style scoped block within the current scope.
     *
     * @param name Optional name for the new child scope
     * @param block The block to execute within the new scope
     * @return The newly created [EventScope]
     */
    @JvmOverloads
    inline fun scoped(name: String = UUID.randomUUID().toString(), block: SubscribeContext<T>.() -> Unit): EventScope {
        val childScope = scope.createScope(name)
        SubscribeContext(eventBus, childScope).apply(block)
        return childScope
    }

}