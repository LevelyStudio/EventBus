package gg.levely.system.eventbus

import gg.levely.system.eventbus.context.EventContext

class EventPriority private constructor(val weight: Int) : Comparable<EventPriority> {

    companion object {
        @JvmStatic
        fun from(weight: Int) = EventPriority(weight)
    }

    override fun compareTo(other: EventPriority): Int {
        return this.weight.compareTo(other.weight)
    }
}