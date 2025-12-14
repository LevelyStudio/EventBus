package gg.levely.system.eventbus

fun interface EventListener<E> {

    fun onEvent(event: E)
}