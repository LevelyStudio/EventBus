package gg.levely.system.eventbus.context

import gg.levely.system.eventbus.EventFilter
import gg.levely.system.eventbus.EventListener
import gg.levely.system.eventbus.EventPriority

interface EventContext<E> {

    var eventPriority: EventPriority
    var eventFilter: EventFilter
    var eventType: Class<E>
    var eventListener: EventListener<E>


    fun withEventFilter(eventFilter: EventFilter) : EventContext<E>;
    fun withEventPriority(eventPriority: EventPriority) : EventContext<E>;
}