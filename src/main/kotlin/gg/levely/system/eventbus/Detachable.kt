package gg.levely.system.eventbus

interface Detachable {

    /**
     * Detaches the implementing entity from its current context, disabling its functionality.
     * Once detached, the entity will not respond to events or perform its designated actions
     * until it is reattached.
     */
    fun detach()

    /**
     * Reattaches the implementing entity to its context, restoring its functionality.
     * After reattachment, the entity will resume responding to events and performing
     * its designated actions as intended.
     */
    fun reattach()

}