package gg.levely.system.eventbus

import gg.levely.system.eventbus.context.DefaultEventContext
import gg.levely.system.eventbus.context.EventContext
import gg.levely.system.eventbus.logger.DebugLogger
import gg.levely.system.eventbus.logger.EventType
import kotlinx.coroutines.*
import java.io.Closeable
import java.util.UUID
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.coroutines.CoroutineContext

/**
 * Event bus that manages listeners and allows triggering custom events.
 *
 * @param T The base event type
 * @param enableLogger Enable debug logging for event operations
 */
class EventBus<T>(
    private val enableLogger: Boolean = false
) {

    /**
     * The set of registered event contexts.
     */
    private val eventContexts = ConcurrentSkipListSet(
        compareByDescending<EventContext<*>> { it.eventPriority.weight }
            .thenBy { it.hashCode() }
    )

    /**
     * The debug logger instance.
     */
    private val logger by lazy { DebugLogger() }

    /**
     * The root event scope.
     */
    private val rootScope = EventScope(name = "root") { ctx -> eventContexts.remove(ctx) }

    /**
     * Creates a new event scope.
     *
     * @param name The name of the scope
     * @return The created [EventScope]
     */
    @JvmOverloads
    fun createScope(name: String = UUID.randomUUID().toString()): EventScope = rootScope.createScope(name)

    /**
     * DSL-style scoped block.
     *
     * @param name The name of the scope
     * @param block The block to execute within the scope
     * @return The created [EventScope]
     */
    @JvmOverloads
    inline fun scoped(name: String = UUID.randomUUID().toString(), block: SubscribeContext<T>.() -> Unit): EventScope {
        val scope = createScope(name)
        SubscribeContext(this, scope).apply(block)
        return scope
    }

    /**
     * Subscribes to an event of type [E].
     *
     * @param E The event type
     * @param clazz The class of the event type
     * @param listener The event listener
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     */
    @JvmOverloads
    fun <E : T> subscribe(
        clazz: Class<E>,
        listener: EventListener<E>,
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY
    ) {
        val context = DefaultEventContext(clazz, listener).apply {
            withEventFilter(filter)
            withEventPriority(priority)
        }
        eventContexts.add(context)
        if (enableLogger) {
            logger.logEvent(EventType.SUBSCRIBE, clazz, listener)
        }
    }

    /**
     * Subscribes to an event of type [E].
     *
     * @param E The event type
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     * @param listener The event listener
     */
    inline fun <reified E : T> subscribe(
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY,
        listener: EventListener<E>
    ) = subscribe(E::class.java, listener, priority, filter)

    /**
     * Subscribes to an event of type [E].
     *
     * @param E The event type
     * @param listener The event listener
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     */
    inline fun <reified E : T> subscribe(
        listener: EventListener<E>,
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY
    ) = subscribe(E::class.java, listener, priority, filter)

    /**
     * Subscribes to an event of type [E] within a specific [scope].
     *
     * @param E The event type
     * @param scope The event scope
     * @param clazz The class of the event type
     * @param listener The event listener
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     * @return A [Subscription] that can be used to unsubscribe
     */
    @JvmOverloads
    fun <E : T> subscribe(
        scope: EventScope,
        clazz: Class<E>,
        listener: EventListener<E>,
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY
    ): Subscription {
        val context = DefaultEventContext(clazz, listener).apply {
            withEventFilter(filter)
            withEventPriority(priority)
        }
        eventContexts.add(context)
        if (enableLogger) {
            logger.logEvent(EventType.SUBSCRIBE, clazz, listener)
        }
        return scope.track(context)
    }

    /**
     * Subscribes to an event of type [E] within a specific [scope].
     *
     * @param E The event type
     * @param scope The event scope
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     * @param listener The event listener
     * @return A [Subscription] that can be used to unsubscribe
     */
    @JvmOverloads
    inline fun <reified E : T> subscribe(
        scope: EventScope,
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY,
        listener: EventListener<E>
    ): Subscription = subscribe(scope, E::class.java, listener, priority, filter)


    /**
     * Unsubscribes from an event of type [E].
     *
     * @param E The event type
     * @param listener The event listener
     */
    inline fun <reified E : T> unsubscribe(listener: EventListener<E>) {
        unsubscribe(E::class.java, listener)
    }

    /**
     * Unsubscribes from an event of type [E].
     *
     * @param E The event type
     * @param clazz The class of the event type
     * @param listener The event listener
     */
    fun <E : T> unsubscribe(clazz: Class<E>, listener: EventListener<E>) {
        eventContexts.removeIf { it.eventType == clazz && it.eventListener == listener }
        if (enableLogger) {
            logger.logEvent(EventType.UNSUBSCRIBE, clazz, listener)
        }
    }

    /**
     * Publishes an event to all matching listeners.
     *
     * @param E The event type
     * @param event The event to publish
     * @return The published event
     */
    fun <E : T> publish(event: E): E {
        val eventClass = event!!::class.java

        eventContexts
            .asSequence()
            .filter { ctx -> matches(ctx.eventType, eventClass, ctx.eventFilter) }
            .forEach { ctx ->
                @Suppress("UNCHECKED_CAST")
                val listener = ctx.eventListener as EventListener<E>

                if (enableLogger) {
                    logger.logEvent(EventType.PUBLISH, eventClass)
                }

                listener.onEvent(event)
            }

        return event
    }

    /**
     * Checks if the subscriber type matches the event type based on the filter.
     *
     * @param subscriberType The subscriber event type
     * @param eventType The published event type
     * @param filter The event filter
     * @return True if it matches, false otherwise
     */
    private fun matches(subscriberType: Class<*>, eventType: Class<*>, filter: EventFilter): Boolean {
        return when (filter) {
            EventFilter.ONLY -> subscriberType == eventType
            else -> subscriberType.isAssignableFrom(eventType)
        }
    }
}