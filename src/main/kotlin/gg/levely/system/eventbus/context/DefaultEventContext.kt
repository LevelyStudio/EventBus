package gg.levely.system.eventbus.context

import gg.levely.system.eventbus.EventFilter
import gg.levely.system.eventbus.EventListener
import gg.levely.system.eventbus.EventPriorities
import gg.levely.system.eventbus.EventPriority

class DefaultEventContext<E>(
    override var eventType: Class<E>,
    override var eventListener: EventListener<E>
) : EventContext<E> {

    override var eventPriority: EventPriority = EventPriorities.Companion.NORMAL
    override var eventFilter: EventFilter = EventFilter.ONLY


    override fun withEventFilter(eventFilter: EventFilter): EventContext<E> {
        this.eventFilter = eventFilter
        return this
    }


    override fun withEventPriority(eventPriority: EventPriority): EventContext<E> {
        this.eventPriority = eventPriority
        return this
    }


    override fun toString(): String {
        return "DefaultEventContext(eventPriority=$eventPriority, eventFilter=$eventFilter, eventType=$eventType, eventListener=$eventListener)"
    }
}