package gg.levely.system.eventbus

fun interface EventListener<in E> {

    fun onEvent(event: E)
}