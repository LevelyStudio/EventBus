package gg.levely.system.eventbus.internal

import gg.levely.system.eventbus.filter.EventFilter
import gg.levely.system.eventbus.EventListener
import gg.levely.system.eventbus.EventPriority

data class EventContext<E>(
    val eventType: Class<E>,
    val listener: EventListener<E>,
    val priority: EventPriority,
    val filter: EventFilter<E>
)