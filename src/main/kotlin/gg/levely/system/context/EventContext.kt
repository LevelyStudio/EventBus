package gg.levely.system.context

import gg.levely.system.EventFilter
import gg.levely.system.EventListener
import gg.levely.system.EventPriority

interface EventContext<E> {

    var eventPriority: EventPriority
    var eventFilter: EventFilter
    var eventType: Class<E>
    var eventListener: EventListener<E>


    fun withEventFilter(eventFilter: EventFilter) : EventContext<E>;
    fun withEventPriority(eventPriority: EventPriority) : EventContext<E>;
}