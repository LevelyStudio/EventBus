package gg.levely.system.eventbus

import gg.levely.system.eventbus.context.DefaultEventContext
import gg.levely.system.eventbus.context.EventContext
import gg.levely.system.eventbus.logger.DebugLogger
import gg.levely.system.eventbus.logger.EventType
import kotlinx.coroutines.*
import java.io.Closeable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Event bus that manages listeners and allows triggering custom events.
 * Uses KClass for purely Kotlin-based type management.
 *
 * @param T The base event type
 * @param enableLogger Enable debug logging for event operations
 */
class EventBus<T : Any>(
    private val enableLogger: Boolean = false,
) : CoroutineScope, Closeable {

    private val job = SupervisorJob()

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val dispatcher = newSingleThreadContext("Event-Dispatcher")

    @OptIn(ExperimentalCoroutinesApi::class)
    override val coroutineContext: CoroutineContext
        get() = dispatcher + job

    private val eventContexts = ConcurrentSkipListSet(
        compareByDescending<EventContext<*>> { it.eventPriority.weight }
            .thenBy { it.hashCode() }
    )

    private val logger by lazy { DebugLogger() }

    /**
     * Subscribes a listener to a specific event type using reified type parameter.
     *
     * @param E The event type to listen for
     * @param listener The listener callback
     * @param priority The priority of this listener (default: NORMAL)
     * @param filter The event filter strategy (default: ONLY)
     */
    @JvmOverloads
    inline fun <reified E : T> subscribe(
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY,
        listener: EventListener<E>
    ) {
        register(E::class, listener, priority, filter)
    }

    /**
     * Subscribes a listener to a specific event type with default priority and filter.
     *
     * @param E The event type to listen for
     * @param listener The listener callback
     */
    inline fun <reified E : T> simpleSubscribe(
        listener: EventListener<E>
    ) {
        register(E::class, listener, EventPriorities.NORMAL, EventFilter.ONLY)
    }

    /**
     * Registers an event listener with the specified configuration.
     *
     * @param clazz The event class to listen for
     * @param listener The listener callback
     * @param priority The priority of this listener
     * @param filter The event filter strategy
     */
    fun <E : T> register(
        clazz: KClass<E>,
        listener: EventListener<E>,
        priority: EventPriority,
        filter: EventFilter,
    ) {
        val eventContext = DefaultEventContext(clazz, listener).apply {
            withEventFilter(filter)
            withEventPriority(priority)
        }
        eventContexts.add(eventContext)
        if (enableLogger) {
            logger.logEvent(EventType.SUBSCRIBE, clazz, listener)
        }
    }

    /**
     * Unsubscribes a listener using reified type parameter.
     *
     * @param E The event type to unsubscribe from
     * @param listener The listener to remove
     */
    inline fun <reified E : T> unsubscribe(listener: EventListener<E>) {
        unsubscribe(E::class, listener)
    }

    /**
     * Unsubscribes a listener from a specific event type.
     *
     * @param clazz The event class to unsubscribe from
     * @param listener The listener to remove
     */
    fun <E : T> unsubscribe(clazz: KClass<E>, listener: EventListener<E>) {
        eventContexts.removeIf { it.eventType == clazz && it.eventListener == listener }
        if (enableLogger) {
            logger.logEvent(EventType.UNSUBSCRIBE, clazz, listener)
        }
    }

    /**
     * Publishes an event synchronously to all matching listeners.
     * Listeners are executed in priority order.
     *
     * @param event The event to publish
     */
    fun <E : T> publish(event: E) {
        val eventKClass = event::class

        eventContexts
            .asSequence()
            .filter { context -> matches(context.eventType, eventKClass, context.eventFilter) }
//            .sortedByDescending { it.eventPriority.weight }
            .forEach { context ->
                @Suppress("UNCHECKED_CAST")
                val listener = context.eventListener as EventListener<E>

                if (enableLogger) {
                    logger.logEvent(EventType.PUBLISH, eventKClass)
                }

                try {
                    listener.onEvent(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    /**
     * Publishes an event asynchronously to all matching listeners.
     * The event is published on the event bus coroutine scope.
     *
     * @param event The event to publish
     */
    fun <E : T> publishAsync(event: E) {
        launch {
            publish(event)
        }
    }

    /**
     * Determines if an event type matches a subscriber type based on the filter strategy.
     *
     * @param subscriberType The type expected by the listener
     * @param eventType The actual type of the published event
     * @param filter The filter strategy to apply
     * @return true if the event matches the subscriber type
     */
    private fun matches(subscriberType: KClass<*>, eventType: KClass<*>, filter: EventFilter): Boolean {
        return when (filter) {
            EventFilter.ONLY -> subscriberType == eventType
            else -> eventType.isSubclassOf(subscriberType)
        }
    }

    /**
     * Retrieves all event contexts that match the specified event type and filter.
     *
     * @param eventType The event class to filter by
     * @param eventFilter The filter strategy
     * @return List of matching event contexts
     */
    fun <E : T> getEventContexts(eventType: Class<E>, eventFilter: EventFilter): List<EventContext<T>> {
        return eventContexts
            .filter { matches(it.eventType, eventType.kotlin, eventFilter) }
            .map {
                @Suppress("UNCHECKED_CAST")
                it as EventContext<T>
            }
    }

    /**
     * Closes the event bus and releases all resources.
     * Shuts down the coroutine dispatcher and cancels all running coroutines.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun close() {
        job.cancel()
        dispatcher.close()
    }

}