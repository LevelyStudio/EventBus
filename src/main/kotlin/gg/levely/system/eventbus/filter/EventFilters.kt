package gg.levely.system.eventbus.filter

import java.util.function.Predicate

object EventFilters {

    @JvmStatic
    fun <E> hierarchy(): EventFilter<E> = EventFilter { event, type ->
        type.isAssignableFrom(event!!::class.java)
    }

    @JvmStatic
    fun <E> exact(): EventFilter<E> = EventFilter { event, type ->
        event!!::class.java == type
    }

    @JvmStatic
    fun <E> filter(predicate: Predicate<E>): EventFilter<E> = EventFilter { event, _ ->
        predicate.test(event)
    }

    @JvmStatic
    fun <E> all(): EventFilter<E> = EventFilter { _, _ -> true }

    @JvmStatic
    fun <E> none(): EventFilter<E> = EventFilter { _, _ -> false }

}