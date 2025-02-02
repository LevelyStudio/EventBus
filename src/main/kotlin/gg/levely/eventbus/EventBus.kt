package gg.levely.eventbus

import gg.levely.eventbus.context.DefaultEventContext
import gg.levely.eventbus.context.EventContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.util.*
import java.util.concurrent.PriorityBlockingQueue

/**
 * The event bus manages listeners and allows you to trigger your own events.
 * @see [subscribe] to register a listener.
 * @see [publish] to trigger an event.
 * @author Luke and Oleksandr
 */
class EventBus<T> {

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val eventDispatcher = newSingleThreadContext("Event-Dispatcher")
    private val eventContexts: Queue<EventContext<*>> = PriorityBlockingQueue<EventContext<*>>(1, EventComparator())


    /**
     * Register a listener of an event.
     * @param eventClass the event class to listen to
     * @param eventListener your listener
     * @return An [EventContext] to manage listening options
     */
    @JvmOverloads
    fun <E : T> subscribe(
        eventClass: Class<E>, eventListener: EventListener<E>,
        eventPriority: EventPriority = EventPriorities.NORMAL,
        eventFilter: EventFilter = EventFilter.ONLY,
    ) {
        val eventContext = DefaultEventContext(eventClass, eventListener).apply {
            withEventFilter(eventFilter)
            withEventPriority(eventPriority)
        }

        eventContexts.add(eventContext)
    }


    /**
     * Allows you to unsubscribe any registered listener.
     * @param eventClass The event type of the listener
     * @param eventListener The listener to unsubscribe
     */
    fun <E : T> unsubscribe(eventClass: Class<E>, eventListener: EventListener<E>) {
        eventContexts.removeIf { it.eventType == eventClass && it.eventListener == eventListener }
    }


    /**
     * Pass an instance of an event to trigger all subscribed listeners.
     * @param event The event to trigger.
     */
    fun <E : T> publish(event: E) {
        eventContexts
            .filter {
                val eventType = it.eventType
                val eventFilter = it.eventFilter
                val eventClass = event!!::class.java

                if (eventType.isInterface && eventClass.interfaces.isNotEmpty()) {
                    eventClass.interfaces.any { target -> hasMatch(eventType, target, eventFilter) }
                } else {
                    hasMatch(eventType, eventClass, eventFilter)
                }

            }
            .forEach {
                val eventListener = it.eventListener as EventListener<E>
                eventListener.onEvent(event)
            }
    }


    /**
     * Do the same as [publish] but asynchronously.
     */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    fun <E : T> publishAsync(event: E) {
        GlobalScope.launch(eventDispatcher) {
            publish(event)
        }
    }


    fun <E : T> getEventContexts(eventType: Class<E>, eventFilter: EventFilter): List<EventContext<T>> {
        return eventContexts.filter { hasMatch(it.eventType, eventType, eventFilter) }.toList() as List<EventContext<T>>
    }


    /**
     * Check whether from and to are equals or if from is a subtype of to<br>
     * depending on the eventFilter.
     * @param from The event's class
     * @param to The event's class or any sub interface of it
     * @param eventFilter The event's filter
     */
    private fun hasMatch(from: Class<*>, to: Class<*>, eventFilter: EventFilter): Boolean {
        return if (eventFilter === EventFilter.ONLY) from == to else from.isAssignableFrom(to)
    }


    /**
     * A comparator to sort listeners by priority.
     */
    class EventComparator : Comparator<EventContext<*>> {

        override fun compare(o1: EventContext<*>, o2: EventContext<*>) = o1.eventPriority.weight - o2.eventPriority.weight
    }
}