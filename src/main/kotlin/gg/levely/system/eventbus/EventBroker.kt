package gg.levely.system.eventbus

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import gg.levely.system.eventbus.filter.EventFilter
import gg.levely.system.eventbus.filter.EventFilters
import org.slf4j.LoggerFactory

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

    fun enableDebugLogging() {
        val logger = LoggerFactory.getLogger(EventBus::class.java)
        if (logger is Logger) {
            logger.level = Level.DEBUG
        }
    }

    fun disableDebugLogging() {
        val logger = LoggerFactory.getLogger(EventBus::class.java)
        if (logger is Logger) {
            logger.level = Level.INFO
        }
    }
}

