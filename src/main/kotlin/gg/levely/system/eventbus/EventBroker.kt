package gg.levely.system.eventbus

import gg.levely.system.eventbus.internal.EventContext
import gg.levely.system.eventbus.filter.EventFilter
import gg.levely.system.eventbus.filter.EventFilters

abstract class EventBroker<T> {

    abstract fun <E : T> subscribe(
        eventType: Class<E>,
        listener: EventListener<E>,
        priority: EventPriority,
        filter: EventFilter<E>
    ): EventContext<E>

    fun <E : T> subscribe(eventType: Class<E>, listener: EventListener<E>): EventContext<E> {
        return subscribe(eventType, listener, EventPriority.NORMAL, EventFilters.exact())
    }

    fun <E : T> subscribe(eventType: Class<E>, listener: EventListener<E>, priority: EventPriority): EventContext<E> {
        return subscribe(eventType, listener, priority, EventFilters.exact())
    }

    inline fun <reified E : T> subscribe(
        priority: EventPriority = EventPriority.NORMAL,
        filter: EventFilter<E> = EventFilters.exact(),
        listener: EventListener<E>
    ) = subscribe(E::class.java, listener, priority, filter)

    inline fun <reified E : T> subscribe(
        listener: EventListener<E>,
        priority: EventPriority = EventPriority.NORMAL,
        filter: EventFilter<E> = EventFilters.exact()
    ) = subscribe(E::class.java, listener, priority, filter)

    abstract fun <E : T> publish(event: E): E

}

