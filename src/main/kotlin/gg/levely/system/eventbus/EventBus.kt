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
    private val eventContexts = ConcurrentSkipListSet(
        compareByDescending<EventContext<*>> { it.eventPriority.weight }
            .thenBy { it.hashCode() }
    )

    private val logger by lazy { DebugLogger() }

    /** Root scope for this event bus */
    val rootScope = EventScope(name = "root") { ctx -> eventContexts.remove(ctx) }

    @JvmOverloads
    fun createScope(name: String = UUID.randomUUID().toString()): EventScope = rootScope.createScope(name)

    @JvmOverloads
    inline fun scoped(name: String = UUID.randomUUID().toString(), block: SubscribeContext<T>.() -> Unit): EventScope {
        val scope = createScope(name)
        SubscribeContext(this, scope).apply(block)
        return scope
    }

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

    inline fun <reified E : T> subscribe(
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY,
        listener: EventListener<E>
    ) = subscribe(E::class.java, listener, priority, filter)


    inline fun <reified E : T> subscribe(
        listener: EventListener<E>,
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY
    ) = subscribe(E::class.java, listener, priority, filter)


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

    @JvmOverloads
    inline fun <reified E : T> subscribe(
        scope: EventScope,
        priority: EventPriority = EventPriorities.NORMAL,
        filter: EventFilter = EventFilter.ONLY,
        listener: EventListener<E>
    ): Subscription = subscribe(scope, E::class.java, listener, priority, filter)


    inline fun <reified E : T> unsubscribe(listener: EventListener<E>) {
        unsubscribe(E::class.java, listener)
    }

    fun <E : T> unsubscribe(clazz: Class<E>, listener: EventListener<E>) {
        eventContexts.removeIf { it.eventType == clazz && it.eventListener == listener }
        if (enableLogger) {
            logger.logEvent(EventType.UNSUBSCRIBE, clazz, listener)
        }
    }

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

    private fun matches(subscriberType: Class<*>, eventType: Class<*>, filter: EventFilter): Boolean {
        return when (filter) {
            EventFilter.ONLY -> subscriberType == eventType
            else -> subscriberType.isAssignableFrom(eventType)
        }
    }
}