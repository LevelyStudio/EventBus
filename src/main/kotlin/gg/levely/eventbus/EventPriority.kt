package gg.levely.eventbus

class EventPriority private constructor(val weight: Int) {

    companion object {
        @JvmStatic
        fun from(weight: Int) = EventPriority(weight)
    }
}