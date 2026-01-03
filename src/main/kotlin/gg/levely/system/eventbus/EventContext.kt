package gg.levely.system.eventbus

import gg.levely.system.eventbus.filter.EventFilter

data class EventContext<E>(
    val eventType: Class<E>,
    val listener: EventListener<E>,
    val priority: EventPriority,
    val filter: EventFilter<E>
)