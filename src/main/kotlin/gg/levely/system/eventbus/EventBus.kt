package gg.levely.system.eventbus

import gg.levely.system.eventbus.context.DefaultEventContext
import gg.levely.system.eventbus.context.EventContext
import gg.levely.system.eventbus.logger.DebugLogger
import gg.levely.system.eventbus.logger.EventType
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Event bus that manages listeners and allows triggering custom events.
 *
 * @param T The base event type
 * @param enableLogger Enable debug logging for event operations
 */
class EventBus<T>(
    private val enableLogger: Boolean = false
) : BranchCreator<T>, EventBroker<T>() {

    /**
     * The set of registered event contexts.
     */
    private val eventContexts = ConcurrentSkipListSet(
        compareByDescending<EventContext<*>> { it.eventPriority.weight }
            .thenBy { it.hashCode() }
    )

    private val children = mutableListOf<EventBranch<T>>()

    /**
     * The debug logger instance.
     */
    private val logger by lazy { DebugLogger() }

    /**
     * Subscribes to an event of type [E].
     *
     * @param E The event type
     * @param eventType The class of the event type
     * @param listener The event listener
     * @param eventPriority The priority of the event listener
     * @param eventFilter The filter for the event listener
     */
    override fun <E : T> subscribe(
        eventType: Class<E>,
        listener: EventListener<E>,
        eventPriority: EventPriority,
        eventFilter: EventFilter<E>
    ): EventContext<E> {
        val context = DefaultEventContext(eventType, listener).apply {
            withEventFilter(eventFilter)
            withEventPriority(eventPriority)
        }
        eventContexts.add(context)
        if (enableLogger) {
            logger.logEvent(EventType.SUBSCRIBE, eventType, listener)
        }
        return context
    }

    internal fun subscribe(context: EventContext<Any>) {
        eventContexts.add(context)
        if (enableLogger) {
            logger.logEvent(EventType.SUBSCRIBE, context.eventType, context.eventListener)
        }
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
        eventContexts.removeIf { it.eventType == clazz && it.eventListener == listener }
        if (enableLogger) {
            logger.logEvent(EventType.UNSUBSCRIBE, clazz, listener)
        }
    }

    /**
     * Unsubscribes using the provided event context.
     *
     * @param context The event context to unsubscribe
     */
    fun unsubscribe(context: EventContext<Any>) {
        eventContexts.remove(context)
        if (enableLogger) {
            logger.logEvent(EventType.UNSUBSCRIBE, context.eventType, context.eventListener)
        }
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

        eventContexts
            .asSequence()
            .filter { ctx ->
                val normalizedEventFilter = ctx.eventFilter as EventFilter<Any>
                normalizedEventFilter.test(event, ctx.eventType as Class<Any>)
            }
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

    override fun branch(name: String): EventBranch<T> {
        val childBranch = DefaultEventBranch(name, this, null)
        children.add(childBranch)
        return childBranch
    }

}