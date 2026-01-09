package gg.levely.system.eventbus

import gg.levely.system.eventbus.branch.BranchCreator
import gg.levely.system.eventbus.branch.DefaultEventBranch
import gg.levely.system.eventbus.branch.EventBranch
import gg.levely.system.eventbus.filter.EventFilter
import gg.levely.system.eventbus.logging.EventLogger
import gg.levely.system.eventbus.logging.EventType
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Event bus that manages listeners and allows triggering custom events.
 *
 * @param T The base event type
 */
class EventBus<T> : BranchCreator<T>, EventBroker<T>() {

    /**
     * The set of registered event contexts.
     */
    private val eventContexts = ConcurrentSkipListSet(
        compareByDescending<EventContext<*>> { it.priority.weight }
            .thenBy { it.hashCode() }
    )

    private val children = mutableListOf<EventBranch<T>>()

    /**
     * The debug logger instance.
     */
    private val logger by lazy { EventLogger() }

    /**
     * Subscribes to an event of type [E].
     *
     * @param E The event type
     * @param eventType The class of the event type
     * @param listener The event listener
     * @param priority The priority of the event listener
     * @param filter The filter for the event listener
     */
    override fun <E : T> subscribe(
        eventType: Class<E>,
        listener: EventListener<E>,
        priority: EventPriority,
        filter: EventFilter<E>
    ): EventContext<E> {
        val context = EventContext(
            eventType,
            listener,
            priority,
            filter
        )

        subscribe(context as EventContext<Any>)

        return context
    }

    internal fun subscribe(context: EventContext<Any>) {
        eventContexts.add(context)
        logger.logEvent(EventType.SUBSCRIBE, context.eventType, context.listener)
    }

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
        eventContexts.removeIf { it.eventType == clazz && it.listener == listener }
        logger.logEvent(EventType.UNSUBSCRIBE, clazz, listener)
    }

    /**
     * Unsubscribes using the provided event context.
     *
     * @param context The event context to unsubscribe
     */
    fun unsubscribe(context: EventContext<Any>) {
        eventContexts.remove(context)
        logger.logEvent(EventType.UNSUBSCRIBE, context.eventType, context.listener)
    }

    /**
     * Publishes an event to all matching listeners.
     *
     * @param E The event type
     * @param event The event to publish
     * @return The published event
     */
    override fun <E : T> publish(event: E): E {
        val eventClass = event!!::class.java
        val shouldLog = logger.isDebugEnabled()

        eventContexts
            .asSequence()
            .filter { ctx ->
                val normalizedEventFilter = ctx.filter as EventFilter<Any>
                normalizedEventFilter.test(event, ctx.eventType as Class<Any>)
            }
            .forEach { ctx ->
                @Suppress("UNCHECKED_CAST")
                val listener = ctx.listener as EventListener<E>

                if (shouldLog) {
                    logger.logEvent(EventType.PUBLISH, eventClass)
                }

                listener.onEvent(event)
            }

        return event
    }

    override fun branch(name: String): EventBranch<T> {
        val childBranch = DefaultEventBranch(name, this, null)
        children.add(childBranch)
        return childBranch
    }

}