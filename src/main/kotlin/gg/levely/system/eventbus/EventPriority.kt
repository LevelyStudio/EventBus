package gg.levely.system.eventbus


class EventPriority(
    val name: String,
    val weight: Int
) : Comparable<EventPriority> {

    override fun compareTo(other: EventPriority): Int = this.weight.compareTo(other.weight)

    operator fun plus(amount: Int): EventPriority = EventPriority(name, weight + amount)

    operator fun minus(amount: Int): EventPriority = EventPriority(name, weight - amount)

    companion object {

        @JvmField
        val LOWEST = of("LOWEST", -1000)

        @JvmField
        val LOW = of("LOW", -500)

        @JvmField
        val NORMAL = of("NORMAL", 0)

        @JvmField
        val HIGH = of("HIGH", 500)

        @JvmField
        val HIGHEST = of("HIGHEST", 1000)

        @JvmStatic
        @JvmOverloads
        fun of(name: String = "Custom", value: Int): EventPriority = EventPriority(name, value)

        @JvmStatic
        fun before(other: EventPriority, gap: Int = 1): EventPriority =
            EventPriority("Before ${other.name}", other.weight - gap)

        @JvmStatic
        fun after(other: EventPriority, gap: Int = 1): EventPriority =
            EventPriority("After ${other.name}", other.weight + gap)

    }
}