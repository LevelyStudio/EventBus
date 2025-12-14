package gg.levely.system.eventbus

interface EventPriorities {

    companion object {
        @JvmField
        val BY_WEIGHT = mutableMapOf<Int, EventPriority>()
        @JvmField
        val HIGH = EventPriority.from(3000)
        @JvmField
        val NORMAL = EventPriority.from(2000)
        @JvmField
        val LOW = EventPriority.from(1000)


        init {
            values().forEach { priority -> BY_WEIGHT[priority.weight] = priority }
        }


        @JvmStatic
        fun values(): List<EventPriority> = listOf(HIGH, NORMAL, LOW)


        @JvmStatic
        fun byWeight(weight: Int) =
            BY_WEIGHT[weight] ?: throw IllegalArgumentException("No priority with weight $weight")
    }
}