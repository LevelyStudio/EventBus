package gg.levely.system.eventbus.filter

import java.util.function.Predicate

fun interface EventFilter<E> {

    fun test(event: E, expectedType: Class<E>): Boolean

    infix fun and(other: EventFilter<E>): EventFilter<E> = EventFilter { event, type ->
        this.test(event, type) && other.test(event, type)
    }

    infix fun or(other: EventFilter<E>): EventFilter<E> = EventFilter { event, type ->
        this.test(event, type) || other.test(event, type)
    }

    operator fun not(): EventFilter<E> = negate()

    fun negate(): EventFilter<E> = EventFilter { event, type ->
        !this.test(event, type)
    }

    infix fun and(predicate: Predicate<E>): EventFilter<E> =
        this and EventFilters.filter(predicate)

    infix fun or(predicate: Predicate<E>): EventFilter<E> =
        this or EventFilters.filter(predicate)

}