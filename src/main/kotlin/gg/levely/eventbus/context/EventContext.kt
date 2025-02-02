package gg.levely.eventbus.context

import gg.levely.eventbus.EventFilter
import gg.levely.eventbus.EventListener
import gg.levely.eventbus.EventPriority

interface EventContext<E> {

    var eventPriority: EventPriority
    var eventFilter: EventFilter
    var eventType: Class<E>
    var eventListener: EventListener<E>


    fun withEventFilter(eventFilter: EventFilter) : EventContext<E>;
    fun withEventPriority(eventPriority: EventPriority) : EventContext<E>;
}