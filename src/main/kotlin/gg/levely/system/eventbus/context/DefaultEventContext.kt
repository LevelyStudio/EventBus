package gg.levely.system.eventbus.context

import gg.levely.system.eventbus.EventFilter
import gg.levely.system.eventbus.EventFilters
import gg.levely.system.eventbus.EventListener
import gg.levely.system.eventbus.EventPriority

internal class DefaultEventContext<E>(
    override var eventType: Class<E>,
    override var eventListener: EventListener<E>
) : EventContext<E> {

    override var eventPriority: EventPriority = EventPriority.NORMAL
    override var eventFilter: EventFilter<E> = EventFilters.exact()

    override fun withEventFilter(eventFilter: EventFilter<E>): EventContext<E> {
        this.eventFilter = eventFilter
        return this
    }

    override fun withEventPriority(eventPriority: EventPriority): EventContext<E> {
        this.eventPriority = eventPriority
        return this
    }

}