package gg.levely.system.eventbus

/**
 * Represents a subscription to an event.
 */
fun interface Subscription {

    /**
     * Unsubscribes from the event.
     */
    fun unsubscribe()

}